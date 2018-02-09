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
package com.esri.rttest.producers;

import com.esri.rttest.IPPort;
import com.esri.rttest.MarathonInfo;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

/**
 *
 * @author david
 */
public class Tcp {

    private static final String IPADDRESS_PATTERN
            = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    private String server;
    private Integer port;

    private OutputStream[] os;
    private Integer numStream;

    public String getServer() {
        return server;
    }

    public Integer getPort() {
        return port;
    }

    public Tcp(String appNamePattern) {

        try {

            // See if the url contains app(name)
            String appPat = "app\\[(.*)\\]";

            // Test to see if the appName pattern matches app[some-app-name]
            Pattern appPattern = Pattern.compile(appPat);
            Matcher appMatcher = appPattern.matcher(appNamePattern);

            // Test to see if the appName looks like it contains a ip address num.num.num.num
            Pattern IPpattern = Pattern.compile(IPADDRESS_PATTERN);
            Matcher IPmatcher = IPpattern.matcher(appNamePattern);

            ArrayList<IPPort> ipPorts = new ArrayList<>();

            if (appMatcher.find()) {
                // appNamePatter has app pattern
                String appName = appMatcher.group(1);

                int portIndex = 0;
                String appNameParts[] = appName.split(":");
                if (appNameParts.length > 1) {
                    appName = appNameParts[0];
                    portIndex = Integer.parseInt(appNameParts[1]);
                }
                System.out.println("appName:" + appName);
                System.out.println("portIndex:" + portIndex);
                MarathonInfo mi = new MarathonInfo();

                ipPorts = mi.getIPPorts(appName, portIndex);
            } else if (IPmatcher.matches()) {
                // appNamePattern looks like an IP
                String ipPortParts[] = appNamePattern.split(":");
                if (ipPortParts.length != 2) {
                    throw new UnsupportedOperationException("You need to provide IP:port");
                }
                int port = Integer.parseInt(ipPortParts[1]);

                IPPort ipport = new IPPort(ipPortParts[0], port);
                ipPorts.add(ipport);

            } else {
                // Assume it's a DNS-name:port

                String namePortParts[] = appNamePattern.split(":");
                if (namePortParts.length != 2) {
                    throw new UnsupportedOperationException("You need to provide dns-name:port");
                }
                String dnsName = namePortParts[0];
                int port = Integer.parseInt(namePortParts[1]);

                Lookup lookup = new Lookup(dnsName, Type.A);
                lookup.run();
                //System.out.println(lookup.getErrorString());
                if (lookup.getAnswers() == null) {
                    InetAddress addr = InetAddress.getByName(dnsName);

                    IPPort ipport = new IPPort(addr.getHostAddress(), port);
                    ipPorts.add(ipport);
                } else {
                    for (Record ans : lookup.getAnswers()) {
                        String ip = ans.rdataToString();
                        IPPort ipport = new IPPort(ip, port);
                        ipPorts.add(ipport);
                        System.out.println(ipport);

                    }
                }

            }

            for (IPPort ipport : ipPorts) {
                if (ipport.getPort() == -1) {
                    ipPorts.remove(ipport);
                }
            }

            if (ipPorts.size() == 0) {
                throw new UnsupportedOperationException("Could not discover the any ip port combinations.");
            }

            // Use the first ip and port found
            server = ipPorts.get(0).getIp();
            port = ipPorts.get(0).getPort();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void shutdown() {
        int i = 0;
        while (i < numStream) {
            try {
                os[i].close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }

    }

    /**
     *
     * @param filename File with lines of data to be sent.
     * @param rate Rate in lines per second to send.
     * @param numToSend Number of lines to send. If more than number of lines in
     * file will resend from start.
     * @param burstDelay Number of milliseconds to burst at; set to 0 to send
     * one line at a time
     * @param appendTime If set to true system time is appended (assumes csv)
     */
    public void sendFile(String filename, Integer rate, Integer numToSend, Integer burstDelay, boolean appendTime) {
        try {

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
            if (burstDelay == 0) {

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
                        linesIt = lines.iterator();  // Reset Iterator
                    }

                    final long stime = System.nanoTime();

                    if (appendTime) {
                        // assuming CSV
                        line = linesIt.next() + "," + String.valueOf(System.currentTimeMillis()) + "\n";
                    } else {
                        line = linesIt.next() + "\n";
                    }

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
            } else {
                // *********** SEND in bursts every msDelay ms  *********

                // Calculate number of events to send during each burst                 
                Integer numPerBurst = Math.round(rate / 1000 * burstDelay);

                if (numPerBurst < 1) {
                    // Send at least one per burst
                    numPerBurst = 1;
                }

                Integer delay = burstDelay;

                while (cnt < numToSend) {

                    // Adjust delay every burst
                    Double curRate = (double) cnt / (System.currentTimeMillis() - st) * 1000;
                    Double rateDiff = (rate - curRate) / rate;
                    tweak = (long) (rateDiff * rate);
                    delay = delay - Math.round(tweak / 1000.0f);
                    if (delay < 0) {
                        delay = 0;  // delay cannot be negative
                    } else {
                        Thread.sleep(delay);
                    }

                    Integer i = 0;
                    while (i < numPerBurst) {
                        if (cnt % rate == 0 && cnt > 0) {
                            curRate = (double) cnt / (System.currentTimeMillis() - st) * 1000;
                            System.out.println(cnt + "," + String.format("%.0f", curRate));
                        }

                        cnt += 1;

                        i += 1;
                        if (!linesIt.hasNext()) {
                            linesIt = lines.iterator();  // Reset Iterator
                        }

                        if (appendTime) {
                            line = linesIt.next() + "," + String.valueOf(System.currentTimeMillis()) + "\n";
                        } else {
                            line = linesIt.next() + "\n";
                        }

                        i = cnt % numStream;
                        this.os[i].write(line.getBytes());
                        this.os[i].flush();

                        // Break out as soon as numToSend is reached
                        if (cnt >= numToSend) {
                            break;
                        }

                    }

                }

            }

            Double sendRate = (double) cnt / (System.currentTimeMillis() - st) * 1000;

            System.out.println(cnt + "," + String.format("%.0f", sendRate));

        } catch (Exception e) {
            // Could fail on very large files that would fill heap space 
            System.out.println(os.toString());
            e.printStackTrace();

        }
    }

    public static void main(String args[]) {

        // Example Command Line args: localhost 5565 faa-stream.csv 1000 10000
        int numargs = args.length;
        if (numargs != 4 && numargs != 5 && numargs != 6) {
            // append append time option was added to support end-to-end latency; I used it for Trinity testing
            System.err.println("Usage: Tcp <server:port> <file> <rate> <numrecords> (burstDelay) (append-time)");
            System.err.println("server:port: The IP or hostname of server to send events to. Could be ip:port, dns-name:port, or app[marathon-app-name(:portindex)]");
            System.err.println("filename: sends line by line from this file.");
            System.err.println("rate: Attempts to send at this rate.");
            System.err.println("numrecords: Sends this many lines; file is automatically recycled if needed.");
            System.err.println("burstDelay in ms; defaults to 0; messages are sent at constant rate");
            System.err.println("append-time defaults to false; Adds system time as extra parameter to each request. ");
        } else {
            // Initial the Tcp Class with the server and port
            Tcp t = new Tcp(args[0]);

            switch (numargs) {
                case 4:
                    t.sendFile(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), 0, false);
                    break;
                case 5:
                    int burstDelay = Integer.parseInt(args[4]);
                    if (burstDelay < 10 || burstDelay > 1000) {
                        System.err.println("Invalid burstDelay; valid values are 10 to 1000 ms");
                        break;
                    }
                    if (burstDelay > 200) {
                        System.out.println("WARNING: For larger values of burstDelay it can take a while to achieve the requested rate.");
                    }

                    t.sendFile(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), burstDelay, false);
                    break;
                case 6:
                    t.sendFile(args[1], Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Boolean.parseBoolean(args[5]));
                    break;
            }

            t.shutdown();

        }

    }
}
