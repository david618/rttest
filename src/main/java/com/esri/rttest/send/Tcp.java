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
 * 
 * Creator: David Jennings
 */
package com.esri.rttest.send;

import com.esri.rttest.IPPort;
import com.esri.rttest.IPPorts;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author david
 */
public class Tcp {

    private static final Logger LOG = LogManager.getLogger(Tcp.class);
    

    private OutputStream[] os;
    private Integer numStream;

    public Tcp(String appNamePattern) {

        try {
             
            IPPorts ipp = new IPPorts(appNamePattern);
            ArrayList<IPPort> ipPorts = ipp.getIPPorts();

            if (ipPorts.isEmpty()) {
                throw new UnsupportedOperationException("Could not discover the any ip port combinations.");
            }

            numStream = ipPorts.size();
            this.os = new OutputStream[numStream];
            int i = 0;
            for (IPPort ipport : ipPorts) {
                Socket skt = new Socket(ipport.getIp(), ipport.getPort());
                this.os[i] = skt.getOutputStream();
                i++;
            }

        } catch (IOException | NumberFormatException | UnsupportedOperationException e) {
            LOG.error("ERROR",e);
            
        }

    }

    public void shutdown() {
        int i = 0;
        while (i < numStream) {
            try {
                os[i].close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            i++;
        }

    }

    /**
     *
     * @param filename File with lines of data to be sent.
     * @param rate Rate in lines per second to send.
     * @param numLines
     */
    public void sendFile(String filename, Integer rate, Integer numLines) {
        try {

            Boolean recycle = true;
            Integer numToSend = numLines;

            if (numLines == 0) {
                // Send the file one time and stop
                numToSend = Integer.MAX_VALUE;
                recycle = false;
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

            // Tweak used to adjust delays to try and get requested rate
            Long tweak = 0L;

            // Delay between each send in nano seconds            
            Double ns_delay = 1000000000.0 / (double) rate;

            long ns = ns_delay.longValue() - tweak;
            if (ns < 0) {
                ns = 0;  // can't be less than 0 
            }

            // *********** If burstDelay = 0 then send Constant Rate using nanosecond delay *********
            while (cnt < numToSend) {

                if (cnt % rate == 0 && cnt > 0) {
                    // Calculate rate and adjust as needed
                    Double curRate = (double) cnt / (System.currentTimeMillis() - st) * 1000;

                    System.out.println(cnt + "," + String.format("%.0f", curRate));
                }

                if (cnt % 1000 == 0 && cnt > 0) {
                    // Calculate rate and adjust as needed
                    Double curRate = (double) cnt / (System.currentTimeMillis() - st) * 1000;

                    // rate difference as percentage 
                    Double rateDiff = (rate - curRate) / rate;

                    // Add or subracts up to 100ns 
                    tweak = (long) (rateDiff * rate);

                    // By adding some to the delay you can fine tune to achieve the desired output
                    ns = ns - tweak;
                    if (ns < 0) {
                        ns = 0;  // can't be less than 0 
                    }

                }

                cnt += 1;

                if (!linesIt.hasNext()) {
                    if (recycle) {
                        linesIt = lines.iterator();  // Reset Iterator
                    } else {
                        // Entire contents of file send or Integer.MAX_VALUE
                        break;
                    }
                }

                final long stime = System.nanoTime();

                line = linesIt.next() + "\n";

                int i = cnt % numStream;
                this.os[i].write(line.getBytes());
                this.os[i].flush();

                long etime = 0;
                do {
                    // This approach uses a lot of CPU                    
                    etime = System.nanoTime();
                    // Adding the following sleep for a few microsecond reduces system load; however,
                    // it also negatively effects the throughput
                    //Thread.sleep(0,100);  
                } while (stime + ns >= etime);

            }

            Double sendRate = (double) cnt / (System.currentTimeMillis() - st) * 1000;

            System.out.println(cnt + "," + String.format("%.0f", sendRate));

        } catch (IOException e) {
            // Could fail on very large files that would fill heap space 
            LOG.error("ERROR",e);

        }
    }

    public static void main(String args[]) {

        // Example Command Line args: localhost 5565 faa-stream.csv 1000 10000
        int numargs = args.length;
        if (numargs != 4) {
            // append append time option was added to support end-to-end latency; I used it for Trinity testing
            System.err.println("Usage: Tcp (server:port) (file) (rate) (numlines)");
            System.err.println("");
            System.err.println("server:port: The IP or hostname of server to send events to. Could be ip:port, dns-name:port, or app[marathon-app-name[:portindex]]");
            System.err.println("filename: Send line by line from this file.");
            System.err.println("rate: Attempts to send at this rate. Lines/seconds.");
            System.err.println("numlines: Send numLine lines (reuse file if needed); if 0 then send all lines in file once.");
        } else {
            // Initial the Tcp Class with the server and port
            String serverPort = args[0];
            String filename = args[1];
            Integer rate = Integer.parseInt(args[2]);
            Integer numlines = Integer.parseInt(args[3]);

            Tcp t = new Tcp(serverPort);

            t.sendFile(filename, rate, numlines);

            t.shutdown();

        }

    }
}
