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
 *
 * Listens on TCP Port for messages.
 * Counts Messages based on value of sampleEveryMessages adds a point to the linear regresssion
 * After collecting three samples it will output the rate.
 * After 10 second pause the count and regression are reset.
 *
 * Creator: David Jennings
 */
package com.esri.rttest.mon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author david
 */
public class TcpSink extends Monitor {
    
    private static final Logger LOG = LogManager.getLogger(TcpSink.class);

    @Override
    public Sample getSample() {

        long cnt = -1L;
        long ts;

        for (TcpSinkServer tcpSinkServer : listTcpSinkServers) {
            cnt += tcpSinkServer.getCnt();
        }

        ts = System.currentTimeMillis();


        return new Sample(cnt, ts);
    }

    @Override
    public void countEnded() {
        for (TcpSinkServer tss : listTcpSinkServers) {
            if (autoTerminate) {
                tss.terminate();
            } else {
                tss.reset();
            }
        }
    }

    public TcpSink() {}

    Integer port;
    Boolean autoTerminate;
    Boolean displayMessages;
    ArrayList<TcpSinkServer> listTcpSinkServers = new ArrayList<>();
	ServerSocket ss;

    
    public TcpSink(Integer port, Integer sampleEveryNSecs, Integer numSampleEqualBeforeExit, Boolean autoTerminate, Boolean displayMessages) {

        this.port = port;

        // For Monitor
        this.sampleRateSec = sampleEveryNSecs;
        this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;

        this.autoTerminate = autoTerminate;
        this.displayMessages = displayMessages;

        try {
            ss = new ServerSocket(port);

            System.out.println("After starting this; create or restart the sending service.");
            System.out.println("Once connected you see a 'Thread Started' message for each connection.");


            // Start the Timer
            run();

            while (true) {
                Socket cs = ss.accept();
                TcpSinkServer tcpSinkServer = new TcpSinkServer(cs, displayMessages);
                tcpSinkServer.start();
                listTcpSinkServers.add(tcpSinkServer);
            }

        } catch (IOException e) {
            LOG.error("ERROR",e);
        }


    }

    public static void main(String[] args) {

        TcpSink app = new TcpSink();
        String appName = app.getClass().getSimpleName();

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);
        formatter.setLeftPadding(1);

        Option helpOp = Option.builder()
                .longOpt("help")
                .desc("display help and exit")
                .build();

        Option brokersOp = Option.builder("p")
                .longOpt("port")
                .required()
                .hasArg()
                .desc("[Required] The port to listen on)")
                .build();

        Option sampleRateSecOp = Option.builder("r")
                .longOpt("sample-rate-sec")
                .hasArg()
                .desc("Sample Rate Seconds; defaults to 10")
                .build();

        Option resetCountOp = Option.builder("n")
                .longOpt("num-samples-no-change")
                .hasArg()
                .desc("Reset after number of this number of samples of no change in count; defaults to 1")
                .build();

        Option printMessagesOp = Option.builder("o")
                .longOpt("print-messages")
                .desc("Print Messages to stdout")
                .build();

        Option autoTerminateOp = Option.builder("a")
                .longOpt("auto-terminate")
                .desc("If count stops increasing the socket is closed; for GeoEvent use false.")
                .build();

        options.addOption(helpOp);
        options.addOption(brokersOp);
        options.addOption(sampleRateSecOp);
        options.addOption(resetCountOp);
        options.addOption(printMessagesOp);
        options.addOption(autoTerminateOp);

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

        Integer port = null;
        if (cmd.hasOption("p")) {
            try {
                port = Integer.parseInt(cmd.getOptionValue("p"));
            } catch (NumberFormatException e ) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid port (p).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("port: " + port);

        int sampleRateSec = 10;
        if(cmd.hasOption("r")) {
            try {
                sampleRateSec = Integer.parseInt(cmd.getOptionValue("r"));
            } catch (NumberFormatException e ) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid sample-rate-sec (r).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("sampleRateSec: " + sampleRateSec);

        int numSampleEqualBeforeExit = 1;
        if(cmd.hasOption("n")) {
            try {
                numSampleEqualBeforeExit = Integer.parseInt(cmd.getOptionValue("n"));
            } catch (NumberFormatException e ) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid num-samples-no-change (s).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("numSampleEqualBeforeExit: " + numSampleEqualBeforeExit);

        boolean printMessages = false;
        if(cmd.hasOption("o")) {
            printMessages = true;
        }
        System.out.println("printMessages : " + printMessages);

        boolean autoTerminate = false;
        if(cmd.hasOption("o")) {
            autoTerminate = true;
        }
        System.out.println("autoTerminate : " + autoTerminate);


        app =  new TcpSink(port, sampleRateSec, numSampleEqualBeforeExit, autoTerminate, printMessages);
        app.run();

    }

}
