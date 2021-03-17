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
 * Listens on on Web Socket for messages.
 * Updated; based on https://www.eclipse.org/jetty/documentation/9.4.x/jetty-websocket-client-api.html
 *
 * Creator: David Jennings
 */
package com.esri.rttest.mon;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

/**
 *
 * @author david
 */
public class WebSocketSink extends Monitor {


    @Override
    public Sample getSample() {

        long cnt;
        long ts;

        cnt = socket.getCnt();
        ts = System.currentTimeMillis();


        return new Sample(cnt, ts);
    }

    @Override
    public void countEnded() {
        startClient();

    }

    WebSocketSinkMsg socket;

    String destUri;

    /**
     * Moved out of constructor; called after countEnded to resume next count
     */
    private void startClient() {
        SslContextFactory sslContextFactory = new SslContextFactory.Client();
        sslContextFactory.setTrustAll(true);

        WebSocketClient client = new WebSocketClient(sslContextFactory);
        client.setMaxIdleTimeout(900000); // 15 mins
        try {
            client.start();

            URI echoUri = new URI(destUri);
            ClientUpgradeRequest request = new ClientUpgradeRequest();            
            client.connect(socket, echoUri, request);
            //System.out.printf("Connecting to : %s%n", echoUri);

            // wait for closed socket connection.
            //socket.awaitClose();
            socket.awaitClose(20, TimeUnit.MINUTES);

        } catch (Exception t) {
            t.printStackTrace();
        } finally {
            try {
                client.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public WebSocketSink() {}

    public WebSocketSink(String url, Integer sampleRateSec, Integer numSampleEqualBeforeExit, boolean printMessages) {

        this.destUri = url;

        this.sampleRateSec = sampleRateSec;
        this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;

        //String destUri = "ws://echo.websocket.org";

        socket = new WebSocketSinkMsg(printMessages);

        // Start Monitor Timer
        run();

        startClient();


    }


    public static void main(String[] args) {

        WebSocketSink app = new WebSocketSink();
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
                .longOpt("websocket-url")
                .required()
                .hasArg()
                .desc("[Required] Websocket URL (e.g. ws://websats.westus2.cloudapp.azure.com/websats/SatStream/subscribe)")
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

        options.addOption(helpOp);
        options.addOption(urlOp);
        options.addOption(sampleRateSecOp);
        options.addOption(resetCountOp);
        options.addOption(printMessagesOp);

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
            System.out.println("Send lines from a file to an Elastic Server");
            System.out.println();
            formatter.printHelp(appName, options);
            System.exit(0);
        }

        String websockerurl = null;
        if (cmd.hasOption("l")) {
            websockerurl = cmd.getOptionValue("l");
        }
        System.out.println("websockerurl: " + websockerurl);

        int sampleRateSec = 10;
        if (cmd.hasOption("r")) {
            try {
                sampleRateSec = Integer.parseInt(cmd.getOptionValue("r"));
            } catch (NumberFormatException e) {
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
        if (cmd.hasOption("n")) {
            try {
                numSampleEqualBeforeExit = Integer.parseInt(cmd.getOptionValue("n"));
            } catch (NumberFormatException e) {
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

        app = new WebSocketSink(websockerurl, sampleRateSec, numSampleEqualBeforeExit, printMessages);
        app.run();
    }

}
