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

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import com.esri.rttest.IPPort;
import com.esri.rttest.IPPorts;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author david
 */
public class Tcp extends Send {

    private static final Logger LOG = LogManager.getLogger(Tcp.class);

    @Override
    public long sendBatch(ArrayList<String> lines) {
        Iterator<String> linesIterator = lines.iterator();
        int cnt = 0;
        while (linesIterator.hasNext()) {
            String line = linesIterator.next() + "\n";  // For TCP you need to end with line feed
            try {
                int i = cnt % numStream;
                os[i].write(line.getBytes());
                os[i].flush();
                cnt += 1;
            } catch (Exception e) {
                LOG.error("ERROR",e);

            }

        }


        return cnt;
    }

    @Override
    public void sendDone() {
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

    public Tcp() {}

    private OutputStream[] os;
    private Integer numStream;

    public void run(String serverPort,String filename, Integer desiredRatePerSec, Long numToSend, boolean reuseFile) {

        try {

            IPPorts ipp = new IPPorts(serverPort);
            ArrayList<IPPort> ipPorts = ipp.getIPPorts();

            if (ipPorts.isEmpty()) {
                throw new UnsupportedOperationException("Could not discover the any ip port combinations.");
            }

            numStream = ipPorts.size();
            os = new OutputStream[numStream];
            int i = 0;
            for (IPPort ipport : ipPorts) {
                Socket skt = new Socket(ipport.getIp(), ipport.getPort());
                os[i] = skt.getOutputStream();
                i++;
            }

            // Part of Abstract Class Send
            this.desiredRatePerSec = desiredRatePerSec;
            this.numToSend = numToSend;
            this.filename = filename;
            this.reuseFile = reuseFile;

            sendFiles();


        } catch (IOException | NumberFormatException | UnsupportedOperationException e) {
            LOG.error("ERROR",e);
            
        }

    }



    public static void main(String[] args) {


        Tcp app = new Tcp();
        String appName = app.getClass().getSimpleName();

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);
        formatter.setLeftPadding(1);

        Option helpOp = Option.builder()
                .longOpt("help")
                .desc("display help and exit")
                .build();

        Option brokersOp = Option.builder("h")
                .longOpt("server-port")
                .required()
                .hasArg()
                .desc("[Required] TCP Server:Port")
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


        Option onetimeOp = Option.builder("o")
                .longOpt("one-time")
                .desc("Send lines only one time. Stop when all lines have been sent.")
                .build();

        options.addOption(helpOp);
        options.addOption(brokersOp);
        options.addOption(fileOp);
        options.addOption(rateOp);
        options.addOption(numToSendOp);
        options.addOption(onetimeOp);

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

        String serverPort = null;
        if (cmd.hasOption("h")) {
            serverPort = cmd.getOptionValue("h");
        }
        System.out.println("serverPort: " + serverPort);

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

        boolean reuseFile = true;
        if(cmd.hasOption("o")) {
            reuseFile = false;
        }
        System.out.println("reuseFile : " + reuseFile);


        app.run(serverPort, file, desiredRatePerSec, numToSend, reuseFile);

    }
}
