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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 *
 * @author david
 */
public class Http extends Send {

    private static final Logger LOG = LogManager.getLogger(Http.class);

    @Override
    public long sendBatch(ArrayList<String> lines) {
        Iterator<String> linesIterator = lines.iterator();
        long cnt = 0;
        while (linesIterator.hasNext()) {
            String line = linesIterator.next();
            try {
                lbq.put(line);
                cnt += 1;
            } catch (Exception e) {

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

        long cnts = 0;
        long cntErr = 0;
        long et = System.currentTimeMillis();

        for (HttpThread thread : threads) {
            cnts += thread.getCnt();
            cntErr += thread.getCntErr();
            if (thread.getLastUpdate() > et) et = thread.getLastUpdate();
        }
    }



    public Http(String url, String filename, Integer desiredRatePerSec, Long numToSend, String contentType, int numThreads, boolean reuseFile) {

        this.url = url;

        // Part of Abstract Class Send
        this.desiredRatePerSec = desiredRatePerSec;
        this.numToSend = numToSend;
        this.filename = filename;
        this.reuseFile = reuseFile;

        // Create the HttpThread
        threads = new HttpThread[numThreads];

        try {
            for (int i = 0; i < threads.length; i++) {

                System.out.println(url);
                threads[i] = new HttpThread(lbq, url, contentType);

                threads[i].start();
            }

        } catch (Exception e ) {
            // Trouble creating threads
        }

        sendFiles();


    }

    private static final String IPADDRESS_PATTERN
            = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();


    String url;
    HttpThread[] threads;

    public static void main(String args[]) throws Exception {

        int numargs = args.length;
        if (numargs < 4 ) {
            System.err.print("Usage: Http [url] [file] [desiredRatePerSec] [numToSend] (contentType=text/plain) (numThreads=1) (reuseFile=true) \n");
            System.err.println("");
            System.err.println("url: URL to Post Messages to");
            System.err.println("file: file with lines of text to send to Elasticsearch; if folder than all files in the folder are sent and reuseFile is set to false.");
            System.err.println("desiredRatePerSec: Desired Rate. The tool will try to send at this rate if possible");
            System.err.println("numToSend: Number of lines to send");
            System.err.println("resueFile: true or false; if true the file is reused as needed to if numToSend is greater than number of lines in the file");
            System.err.println("");
        } else {

            String url = args[0];
            String file = args[1];
            Integer desiredRatePerSec = Integer.parseInt(args[2]);
            Long numToSend = Long.parseLong(args[3]);


            String contentType = "text/plain";
            if (numargs > 4) {
                contentType = args[4];
            }

            int numThreads = 1;
            if (numargs > 5) {
                numThreads = Integer.parseInt(args[5]);
            }

            boolean reuseFile = true;
            if (numargs > 6) {
                reuseFile = Boolean.parseBoolean(args[6]);
            }

            Http t = new Http(url, file, desiredRatePerSec, numToSend, contentType, numThreads, reuseFile);

        }


    }
}
