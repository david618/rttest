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
package com.esri.rttest.producers;

import com.esri.rttest.monitors.KafkaTopicMon;
import com.esri.rttest.MarathonInfo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Sends lines of a text file to a Kafka Topic 
 * Lines are sent at a specified rate.
 * 
 * @author david
 */
public class KafkaUsingThreads {

    private static final Logger log = LogManager.getLogger(KafkaTopicMon.class);

    LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();

    /**
     *
     * @param brokers
     * @param topic
     * @param filename File with lines of data to be sent.
     * @param rate Rate in lines per second to send.
     * @param numToSend Number of lines to send. If more than number of lines in
     * file will resend from start.
     * @param numThreads
     */
    public void sendFile(String brokers, String topic, String filename, Integer rate, Integer numToSend, Integer numThreads) {
        try {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            // Read the file into an array
            ArrayList<String> lines = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            br.close();
            fr.close();

            Iterator<String> linesIt = lines.iterator();

            // Get the System Time
            Long st = System.currentTimeMillis();

            Integer cnt = 0;

            KafkaSenderThread[] threads = new KafkaSenderThread[numThreads];

            
            for (int i = 0; i< numThreads; i++) {

                threads[i] = new KafkaSenderThread(lbq, brokers, topic);
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
                    for (KafkaSenderThread thread : threads) {
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
                    for (KafkaSenderThread thread : threads) {
                        cnts += thread.getCnt();
                        cntErr += thread.getCntErr();
                    }

                    Double curRate = (double) cnts / (System.currentTimeMillis() - st) * 1000;

                    System.out.println(cnts + "," + cntErr + "," + String.format("%.0f", curRate));

                    // End if the lbq is empty
                    if (lbq.isEmpty()) {
                        System.out.println("Queue Empty");
                        break;
                    }

                    // End if the cnts from threads match what was sent
                    if (cnts >= numToSend) {
                        System.out.println("Count Sent >= Number Requested");
                        break;
                    }

                    // End if cnts are not changing 
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
            for (KafkaSenderThread thread : threads) {
                thread.terminate();
            }

            cnts = 0;
            cntErr = 0;

            for (KafkaSenderThread thread : threads) {
                cnts += thread.getCnt();
                cntErr += thread.getCntErr();
            }

            Double sendRate = (double) cnts / (System.currentTimeMillis() - st) * 1000;

            System.out.println(cnts + "," + cntErr + "," + String.format("%.0f", sendRate));

            System.exit(0); 

        } catch (IOException | InterruptedException e) {
            // Could fail on very large files that would fill heap space 

            log.error("ERROR", e);

        }
    }

    public static void main(String args[]) throws Exception {

        int numArgs = args.length;
        
        // Command Line d1.trinity.dev:9092 simFile simFile_1000_10s.dat 1000 10000
        if (numArgs != 5 && numArgs != 6) {
            System.err.print("Usage: Kafka <broker> <topic> <file> <rate> <numrecords> (<numThreads>)\n");
        } else {

            
            String brokers = args[0];            

            String brokerSplit[] = brokers.split(":");

            if (brokerSplit.length == 1) {
                // Try hub name. Name cannot have a ':' and brokers must have it.
                brokers = new MarathonInfo().getBrokers(brokers);
            }   // Otherwise assume it's brokers 

            String topic = args[1];            
            String file = args[2];
            Integer rate = Integer.parseInt(args[3]);
            Integer numToSend = Integer.parseInt(args[4]);
            Integer numThreads = 1;
            
            if (numArgs == 6) {
                numThreads = Integer.parseInt(args[5]);
            }
                    
            
            
            KafkaUsingThreads t = new KafkaUsingThreads();
            t.sendFile(brokers,topic,file,rate,numToSend,numThreads);

        }

    }
}
