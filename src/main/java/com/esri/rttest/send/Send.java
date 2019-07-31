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


                sendFile(inputPath.getAbsolutePath());
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
                    // Reset Interator
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
