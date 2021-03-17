/*
 * (C) Copyright 2017 David Jennings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     David Jennings
 */

 /*
 * Sends lines of a text file to a HTTP Server using HTTP Post
 * Lines are sent at a specified rate.
 * 
 * Creator: David Jennings
 */
package com.esri.rttest.send;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.cli.*;

/**
 *
 * @author david
 */
public class Http extends Send {


    @Override
    public long sendBatch(ArrayList<String> lines) {
        Iterator<String> linesIterator = lines.iterator();
        long cnt = 0;
        while (linesIterator.hasNext()) {
            String line = linesIterator.next();
            try {
                lbq.put(line);
                cnt += 1;
            } catch (Exception ignored) {

            }

        }

        // Check threads
        int cntThreadsRunnings = 0;
        for (HttpThread thread : threads) {
            if (thread.isRunning()) {
                cntThreadsRunnings +=1;
            }
        }

        if (cntThreadsRunnings == 0) {
            // Threads have all died
            System.err.println("Threads have all failed");
            System.exit(1);

        }


        return cnt;
    }

    @Override
    public void sendDone() {
        // Terminate Threads
        for (HttpThread thread : threads) {
            thread.terminate();
        }

//        long cnts = 0;
//        long cntErr = 0;
        long et = System.currentTimeMillis();

        for (HttpThread thread : threads) {
//            cnts += thread.getCnt();
//            cntErr += thread.getCntErr();
            if (thread.getLastUpdate() > et) et = thread.getLastUpdate();
        }
    }

    public Http() {
    }


    public void run(String url, String filename, Integer desiredRatePerSec, Long numToSend, String contentType, int numThreads, boolean reuseFile, String username, String password, String xOriginalUrlHeader) {

        this.url = url;

        // Part of Abstract Class Send
        this.desiredRatePerSec = desiredRatePerSec;
        this.numToSend = numToSend;
        this.filename = filename;
        this.reuseFile = reuseFile;

        // Turn url into ip(s)
        //IPPorts ipp = new IPPorts(url);
        //ArrayList<IPPort> ipPorts = ipp.getIPPorts();

        // Create the HttpThread
        threads = new HttpThread[numThreads];

        try {
            for (int i = 0; i < threads.length; i++) {

                // Rotating through ip's create threads requested
                //IPPort ipport = ipPorts.get(i % ipPorts.size());
                //String thdURL = ipp.getProtocol() + "://" + ipport.getIp() + ":" + ipport.getPort() + ipp.getPath();
                //System.out.println(thdURL);
                //threads[i] = new HttpThread(lbq, thdURL, contentType, username, password);
                threads[i] = new HttpThread(lbq, this.url, contentType, username, password, xOriginalUrlHeader);

                threads[i].start();
            }

        } catch (Exception e ) {
            // Trouble creating threads
        }

        sendFiles();

        try {
            for (HttpThread thread : threads) thread.terminate();

        } catch (Exception e ) {
            e.printStackTrace();
        }

        System.exit(0);

    }

    LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();

    String url;
    HttpThread[] threads;

    public static void main(String[] args)  {
        Http app = new Http();
        String appName = app.getClass().getSimpleName();

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);
        formatter.setLeftPadding(1);

        Option helpOp = Option.builder()
                .longOpt("help")
                .desc("display help and exit")
                .build();

        Option urlOp = Option.builder("l")
                .longOpt("url")
                .required()
                .hasArg()
                .desc("[Required] Post Messages to this URL")
                .build();

        Option fileOp = Option.builder("f")
                .longOpt("file")
                .required()
                .hasArg()
                .desc("[Required] File with lines of text to send; if a folder is specified all files in the folder are sent one at a time alphabetically")
                .build();

        Option rateOp = Option.builder("r")
                .longOpt("rate")
                .required()
                .hasArg()
                .desc("[Required] Desired Rate. The tool will try to send at this rate if possible")
                .build();

        Option numToSendOp = Option.builder("n")
                .longOpt("number-to-send")
                .required()
                .hasArg()
                .desc("[Required] Number of lines to send")
                .build();

        Option contentTypeOp = Option.builder("c")
                .longOpt("content-type")
                .hasArg()
                .desc("Set header content type; defaults to text/plain")
                .build();

        Option numThreadsOp = Option.builder("t")
                .longOpt("num-threads")
                .hasArg()
                .desc("Number of threads to use for sending; default 1")
                .build();

        Option onetimeOp = Option.builder("o")
                .longOpt("one-time")
                .desc("Send lines only one time. Stop when all lines have been sent.")
                .build();

        Option usernameOp = Option.builder("u")
                .longOpt("username")
                .hasArg()
                .desc("Mqtt Server Username; default no username")
                .build();

        Option passwordOp = Option.builder("p")
                .longOpt("password")
                .hasArg()
                .desc("Mqtt Server Password; default no password")
                .build();

        Option optionXOrigin = Option.builder("x")
                .longOpt("x-origin")
                .hasArg()
                .desc("Add header for x-original-url")
                .build();

        options.addOption(helpOp);
        options.addOption(urlOp);
        options.addOption(fileOp);
        options.addOption(rateOp);
        options.addOption(numToSendOp);
        options.addOption(contentTypeOp);
        options.addOption(numThreadsOp);
        options.addOption(onetimeOp);
        options.addOption(usernameOp);
        options.addOption(passwordOp);
        options.addOption(optionXOrigin);


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.err.println();
            formatter.printHelp(appName, options);
            System.exit(1);
        }

        if (cmd.hasOption("--help")) {
            System.out.println("Send lines from a file to an Http Server");
            System.out.println();
            formatter.printHelp(appName, options);
            System.exit(0);
        }

        String url = null;
        if (cmd.hasOption("l")) {
            url = cmd.getOptionValue("l");
        }
        System.out.println("url: " + url);

        String file = null;
        if(cmd.hasOption("f")) {
            file = cmd.getOptionValue("f");
        }
        System.out.println("file: " + file);

        Integer desiredRatePerSec = null;
        if(cmd.hasOption("r")) {
            try {
                desiredRatePerSec = Integer.parseInt(cmd.getOptionValue("r"));
            } catch (NumberFormatException e ) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid value for rate (r).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("desiredRatePerSec: " + desiredRatePerSec);

        Long numToSend = null;
        if(cmd.hasOption("n")) {
            try {
                numToSend = Long.parseLong(cmd.getOptionValue("n"));
            } catch (NumberFormatException e) {
                System.out.println();
                System.out.println("Invalid value for num-to-send (n). Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("numToSend: " + numToSend);

        String contentType = "text/plain";
        if(cmd.hasOption("c")) {
            contentType = cmd.getOptionValue("c");
        }
        System.out.println("contentType: " + contentType);

        int numThreads = 1;
        if(cmd.hasOption("t")) {
            try {
                String tmpStr = cmd.getOptionValue("t");
                numThreads = Integer.parseInt(tmpStr);
            } catch (NumberFormatException e) {
                System.out.println();
                System.out.println("Invalid value for num-threads (t). Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("numThreads: " + numThreads);


        boolean reuseFile = true;
        if(cmd.hasOption("o")) {
            reuseFile = false;
        }
        System.out.println("reuseFile : " + reuseFile);

        String username = "";
        if(cmd.hasOption("u")) {
            username = cmd.getOptionValue("u");
        }
        System.out.println("username: " + username);


        String password = "";
        if(cmd.hasOption("p")) {
            password = cmd.getOptionValue("p");
        }
        System.out.println("password: " + password);

        String xOriginalUrlHeader = "";
        if(cmd.hasOption("p")) {
            xOriginalUrlHeader = cmd.getOptionValue("x");
        }
        System.out.println("xOriginalUrlHeader: " + xOriginalUrlHeader);

        app.run(url, file, desiredRatePerSec, numToSend, contentType, numThreads, reuseFile, username, password, xOriginalUrlHeader);
    }
}
