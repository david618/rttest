package com.esri.rttest.send;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

abstract public class Send {
    private static final Logger LOG = LogManager.getLogger(Send.class);

    Integer desiredRatePerSec;  // Desired Rate
    Long numToSend;  // Number of lines to send
    String filename; // File to send
    boolean reuseFile; // Set to true; file is used over and over (default); otherwise file send once.
    int groupFieldIndex;  // index in delimited file of field for grouped batch sending

    ArrayList<String> lines = new ArrayList<>();

    abstract public long sendBatch(ArrayList<String> batchLines);

    abstract public void sendDone();

    long numberSent;
    long batchNumber;
    long startTime;


    protected void sendFiles() {
        try {

            File inputPath = new File(filename);

            System.out.println("Start Send");
            System.out.println("Use Ctrl-C to Abort.");
            System.out.println();
            System.out.println("|Number Sent         |Current Rate Per Sec|Overall Rate Per Sec|");
            System.out.println("|--------------------|--------------------|--------------------|");

            numberSent = 0;
            batchNumber = 0;
            startTime = System.currentTimeMillis();

            if (inputPath.isDirectory()) {

                File[] listOfFiles = inputPath.listFiles();
                Arrays.sort(listOfFiles);

                long count = 0;

                int numFiles = listOfFiles.length;

                int fileNumber = 0;

                boolean resuseFiles = reuseFile;  // Set reuseFiles
                reuseFile = false; // Don't send same file over and over

                while (fileNumber < numFiles) {

                    System.out.println("Sending : " + listOfFiles[fileNumber].getAbsolutePath());
                    count += sendFile(listOfFiles[fileNumber].getAbsolutePath());

                    if (count >= numToSend && numToSend != -1) break;
                    // The numToSend has been reached and it's not -1; used to send forever

                    fileNumber += 1;

                    if (fileNumber == numFiles) {
                        // if reusefile then go back to file 0
                        if (resuseFiles) fileNumber = 0;
                    }
                }

            } else {
                if (groupFieldIndex == -1) {
                    sendFile(inputPath.getAbsolutePath());
                } else {
                    sendGroupedFile(inputPath.getAbsolutePath(), groupFieldIndex);
                }
            }

            sendDone();
            System.out.println("Done");
            System.out.println();

        } catch (Exception e) {
            // Could fail on very large files that would fill heap space

            LOG.error("ERROR", e);
            System.err.println(e.getMessage());

        }
    }


    private long sendFile(String filename) {

        this.filename = filename;

        long numberSentThisFile = 0;

        try {

            long msToWait = 1000;  // Start at 1000 ms

            loadFile(filename);

            Iterator<String> linesIterator = lines.iterator();

            while (numberSent < numToSend || numToSend == -1) {

                batchNumber +=1;

                long numberSentThisBatch = 0;


                ArrayList<String> batchLines = new ArrayList<>();
                // Create a batch of lines ArrayList<String> to send; up to desiredRatePerSec; stop sooner if numToSend met
                while (numberSentThisBatch < desiredRatePerSec && (numberSent < numToSend || numToSend == -1)) {
                    // Reset Iterator
                    if (!linesIterator.hasNext() && reuseFile) {
                        linesIterator = lines.iterator();
                    }

                    if (linesIterator.hasNext()) {
                        batchLines.add(linesIterator.next());
                        numberSentThisBatch += 1;
                    } else {
                        // File has been exhausted and is not set for reuse
                        break;
                    }
                }

                if (batchLines.isEmpty()) break;

                long startBatchTime = System.currentTimeMillis();
                if (batchNumber == 1) {
                    startTime = startBatchTime;
                }


                numberSentThisBatch = sendBatch(batchLines);
                numberSent += numberSentThisBatch;
                numberSentThisFile += numberSentThisBatch;


                Long remainingMilliseconds = msToWait - (System.currentTimeMillis() - startBatchTime);

                // Sleep by remainder of second
                if (remainingMilliseconds > 0) {
                    Thread.sleep(remainingMilliseconds);
                }

                double currentRatePerSec =  (double) numberSentThisBatch / (double) (System.currentTimeMillis() - startBatchTime) * 1000.0;
                double overallRatePerSec =  (double) numberSent / (double) (System.currentTimeMillis() - startTime) * 1000.0;

                printline(numberSent, currentRatePerSec, overallRatePerSec);

                double rateRatio = ((double) overallRatePerSec)/(double)desiredRatePerSec;

                // Adjust the msToWait
                if (rateRatio < 1.00) {
                    msToWait -= 100*(1-rateRatio);
                } else {
                    msToWait = 1000;
                }

                // Min msToWait 0
                if (msToWait < 0) {
                    msToWait = 0;
                }


            }
        } catch (InterruptedException e ) {
            // From Sleep
            LOG.debug("Error", e);
            System.out.println(e.getMessage());

        }

        return numberSentThisFile;



    }


    /**
     *
     * For sending lines from a file based on common value of designated field
     *
     * @param filename name of file to send lines from
     * @param fieldIndex index of field for grouped sending
     * @return number of lines sent this
     */
    private long sendGroupedFile(String filename, int fieldIndex) {

        this.filename = filename;

        long numberSentThisFile = 0;

        try {

            long msToWait = 1000;  // Start at 1000 ms

            loadFile(filename);

            Iterator<String> linesIterator = lines.iterator();

            //setup lines and values
            String currentLine = "";
            String currentFieldValue = "";
            String previousLine = "";
            String previousFieldValue = ""; // value of element of group field

            //get first value
            if (linesIterator.hasNext()) {
                currentLine = linesIterator.next();  // gets next line
                // prob make this a private helper method
                String[] splitLine = currentLine.split(","); // split string by comma
                currentFieldValue = splitLine[fieldIndex]; // value of element of group field
            } else {
                //File is empty
                System.out.println("The file is empty");
            }

            // run until number to send has been met, or indefinitely if -1
            while (numberSent < numToSend || numToSend == -1) {

                batchNumber +=1;

                //set up batch of lines to send
                long numberSentThisBatch = 0;
                ArrayList<String> batchLines = new ArrayList<>();


                //adding line
                batchLines.add(currentLine); // Add the line line from above to batch
                numberSentThisBatch += 1;

                //done with that line, set to previous
                previousLine = currentLine;
                previousFieldValue = currentFieldValue;


                // Reset Iterator if no lines and reusing file
                if (!linesIterator.hasNext() && reuseFile) {
                    linesIterator = lines.iterator();
                }

                //check if have next and set currentLine and currentFieldValue to it
                if (linesIterator.hasNext()) {
                    currentLine = linesIterator.next();  // gets next line
                    // prob make this a private helper method
                    String[] splitLine = currentLine.split(","); // split string by comma
                    currentFieldValue = splitLine[fieldIndex]; // value of element of group field
                } else {
                    break;
                }

                // Continue building batch of lines ArrayList<String> to send if field values are equal
                while (currentFieldValue.equals(previousFieldValue) && (numberSent < numToSend || numToSend == -1)) {

                    //add the current line
                    batchLines.add(currentLine);
                    numberSentThisBatch += 1;

                    //done with that line, set to previous
                    previousLine = currentLine;
                    previousFieldValue = currentFieldValue;

                    // Reset Iterator if no lines and reusing file
                    if (!linesIterator.hasNext() && reuseFile) {
                        linesIterator = lines.iterator();
                    }

                    if (linesIterator.hasNext()) {
                        currentLine = linesIterator.next();  // gets next line
                        // prob make this a private helper method
                        String[] splitLine = currentLine.split(","); // split string by comma
                        currentFieldValue = splitLine[fieldIndex]; // value of element of group field
                    } else {
                        break;
                    }

                }

                if (batchLines.isEmpty()) break;

                long startBatchTime = System.currentTimeMillis();
                if (batchNumber == 1) {
                    startTime = startBatchTime;
                }


                numberSentThisBatch = sendBatch(batchLines);
                numberSent += numberSentThisBatch;
                numberSentThisFile += numberSentThisBatch;


                Long remainingMilliseconds = msToWait - (System.currentTimeMillis() - startBatchTime);

                // Sleep by remainder of second
                if (remainingMilliseconds > 0) {
                    Thread.sleep(remainingMilliseconds);
                }

                double currentRatePerSec =  (double) numberSentThisBatch / (double) (System.currentTimeMillis() - startBatchTime) * 1000.0;
                double overallRatePerSec =  (double) numberSent / (double) (System.currentTimeMillis() - startTime) * 1000.0;

                printline(numberSent, currentRatePerSec, overallRatePerSec);

                double rateRatio = ((double) overallRatePerSec)/(double)desiredRatePerSec;

                // Adjust the msToWait
                if (rateRatio < 1.00) {
                    msToWait -= 100*(1-rateRatio);
                } else {
                    msToWait = 1000;
                }

                // Min msToWait 0
                if (msToWait < 0) {
                    msToWait = 0;
                }


            }
        } catch (InterruptedException e ) {
            // From Sleep
            LOG.debug("Error", e);
            System.out.println(e.getMessage());

        }

        return numberSentThisFile;



    }

    private String padLeft(String s, int n) {
        return String.format("%" + n + "s", s);
    }

    private void printline(long numSent, Double currentRate, Double overallRate) {

        String strNumSent = Long.toString(numSent);

        String strCurrentRate = "";
        if (currentRate != null) {
            strCurrentRate = String.format("%,.0f", currentRate);
        }

        String strOverallRate = "";
        if (overallRate != null) {
            strOverallRate = String.format("%,.0f", overallRate);
        }


        System.out.println("|" + padLeft(strNumSent, 19)
                + " |" + padLeft(strCurrentRate, 19)
                + " |" + padLeft(strOverallRate, 19) + " |");
    }

    public void loadFile(String filename) {

        lines = new ArrayList<>();

        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            br.close();
            fr.close();

        } catch (IOException e) {
            LOG.debug("ERROR", e);

            System.err.println(e.getMessage());
        }

    }
}
