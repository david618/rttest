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
 * Sends lines of a text file to a TCP Server 
 * Lines are sent at a specified rate.
 * While sending the send rates are adjusted to try to overcome hardware differences.
 * The maximum possible rate depends on hardware and network.
 * You can use Java options (-Xms2048m -Xmx2048m) to set the Heap Size available.
 * 
 * 30 Aug 2017: Updated to renable support to append time; modified code to adjust rate
 *     dynamically to more closely achieve the requested rate.
 *     Testing from 10,000 to 180,000
 *         BurstDelay = 0: Rate with 1% of requested rate
 *         BurstDelay = 100: Rate with 3% of requested rate
 *         Peak around 150,000/s on my computer i7 with 32GB RAM
 * 
 * 7 Sep 2017: Added Tcp: If a DNS name is provide a lookup is done and a socket is opened to each
 *      ip associated with the name.  tcp-kafka.marathon.mesos might have 4 ip; each ip gets a socket.
 *      With this change I could get rates with 4 instances up to around 500,000/s on Azure.
 *
 * 26 Jan 2018: Modified to used a linked block queue and threads.
 *      Combined parameters server port into one server:port.
 *      The server:port can be ip:port, dns-name:port, app[marathon-app-name], or app[marathon-app-name:portindex]
 *      Uses Marathon-Info to lookup ip and ports for marathon-app-name if needed.
 *      Uses DNS lookup and InetAddress to find ip's for a given name.
 *      Each thread is assigned a ip:port in a round robin fashion. 
 *      These changes increased max send rate from 140k/s to close to 600k/s
 * 
 * Creator: David Jennings
 */
package com.esri.rttest.send;

import com.esri.rttest.IPPort;
import com.esri.rttest.IPPorts;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author david
 */
public class Tcp2 {
    
    private static final Logger LOG = LogManager.getLogger(Tcp2.class);
    

    LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();

    /**
     *
     * @param appNamePattern
     * ip:port/app[marathon-app-name(:index)]/dns-name:port
     * @param filename File with lines of data to be sent.
     * @param rate Rate in lines per second to send.
     * @param numToSend Number of lines to send. If more than number of lines in
     * file will resend from start.
     * @param numThreads
     */
    public void sendFile(String appNamePattern, String filename, Integer rate, Integer numToSend, Integer numThreads) {
        try {

            IPPorts ipp = new IPPorts(appNamePattern);
            ArrayList<IPPort> ipPorts = ipp.getIPPorts();

            if (ipPorts.isEmpty()) {
                throw new UnsupportedOperationException("Could not discover the any ip port combinations.");
            }

            // Read File
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            // Load Array with Lines from File
            ArrayList<String> lines = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            br.close();
            fr.close();

            // Create Iterator from Array
            Iterator<String> linesIt = lines.iterator();

            // Get the System Time as st (Start Time)            
            Long st = System.currentTimeMillis();

            // Count of Records Sent
            Integer cnt = 0;

            // Create the TcpSenderThreads
            TcpSenderThread[] threads = new TcpSenderThread[numThreads];

            for (int i = 0; i < numThreads; i++) {
                // Use modulo to get one of the ipport's 0
                IPPort ipPort = ipPorts.get((i + 1) % ipPorts.size());
                System.out.println(ipPort);
                threads[i] = new TcpSenderThread(lbq, ipPort.getIp(), ipPort.getPort());
                threads[i].start();

            }

            Long timeLastDisplayedRate = System.currentTimeMillis();
            Long timeStartedBatch = System.currentTimeMillis();

            while (cnt < numToSend) {

                if (System.currentTimeMillis() - timeLastDisplayedRate > 5000) {
                    // Calculate rate and output every 5000ms 
                    timeLastDisplayedRate = System.currentTimeMillis();

                    int cnts = 0;
                    int cntErr = 0;

                    // Get Counts from Threads
                    for (TcpSenderThread thread : threads) {
                        cnts += thread.getCnt();
                        cntErr += thread.getCntErr();
                    }

                    Double curRate = (double) cnts / (System.currentTimeMillis() - st) * 1000;

                    System.out.println(cnts + "," + cntErr + "," + String.format("%.0f", curRate));

                }

                if (!linesIt.hasNext()) {
                    linesIt = lines.iterator();  // Reset Iterator
                }

                line = linesIt.next() + "\n";

                lbq.put(line);

                cnt += 1;

                if (cnt % rate == 0) {
                    // Wait until one second before queuing next rate events into lbq
                    // Send rate events wait until 1 second is up
                    long timeToWait = 1000 - (System.currentTimeMillis() - timeStartedBatch);
                    if (timeToWait > 0) {
                        Thread.sleep(timeToWait);
                    }
                    timeStartedBatch = System.currentTimeMillis();
                }
            }

            int cnts = 0;
            int cntErr = 0;
            int prevCnts = 0;

            while (true) {
                if (System.currentTimeMillis() - timeLastDisplayedRate > 5000) {
                    // Calculate rate and output every 5000ms 
                    timeLastDisplayedRate = System.currentTimeMillis();

                    // Get Counts from Threads
                    for (TcpSenderThread thread : threads) {
                        cnts += thread.getCnt();
                        cntErr += thread.getCntErr();
                    }

                    Double curRate = (double) cnts / (System.currentTimeMillis() - st) * 1000;

                    System.out.println(cnts + "," + cntErr + "," + String.format("%.0f", curRate));

                    // End if the lbq is empty
                    if (lbq.size() == 0) {
                        System.out.println("Queue Empty");
                        break;
                    }

                    // End if the cnts from threads match what was sent
                    if (cnts >= numToSend) {
                        System.out.println("Count Sent >= Number Requested");
                        break;
                    }

                    // End if cnts is changing 
                    if (cnts == prevCnts) {
                        System.out.println("Counts are not changing.");
                        break;
                    }

                    cnts = 0;
                    cntErr = 0;
                    prevCnts = cnts;

                }
            }

            // Terminate Threads
            for (TcpSenderThread thread : threads) {
                thread.terminate();
            }

            cnts = 0;
            cntErr = 0;

            for (TcpSenderThread thread : threads) {
                cnts += thread.getCnt();
                cntErr += thread.getCntErr();
            }

            Double sendRate = (double) cnts / (System.currentTimeMillis() - st) * 1000;

            System.out.println(cnts + "," + cntErr + "," + String.format("%.0f", sendRate));

            System.exit(0);

        } catch (IOException | InterruptedException | UnsupportedOperationException e) {

            LOG.error("ERROR", e);

        }
    }

    public static void main(String args[]) {

        // Example Command Line args: localhost 5565 faa-stream.csv 1000 10000
        int numargs = args.length;
        if (numargs < 4 || numargs > 5) {
            // append append time option was added to support end-to-end latency; I used it for Trinity testing
            System.err.println("Usage: Tcp2 <server:port> <file> <rate> <numrecords> (numThreads=1)");
            System.err.println("server:port: The IP or hostname of server to send events to. Could be ip:port, dns-name:port, or app[marathon-app-name(:portindex)]");
            System.err.println("filename: sends line by line from this file.");
            System.err.println("rate: Attempts to send at this rate.");
            System.err.println("numrecords: Sends this many lines; file is automatically recycled if needed.");
            System.err.println("numThread: Number of threads defaults to 1");
        } else {
            // Initial the Tcp Class with the server and port

            String serverPort = args[0];
            String filename = args[1];
            Integer rate = Integer.parseInt(args[2]);
            Integer numrecords = Integer.parseInt(args[3]);
            Integer numThreads = 1;

            switch (numargs) {
                case 5:
                    numThreads = Integer.parseInt(args[4]);
                    break;
            }

            Tcp2 t = new Tcp2();
            t.sendFile(serverPort, filename, rate, numrecords, numThreads);

        }

    }
}
