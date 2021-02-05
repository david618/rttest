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
/**
 * Listens on on Web Socket for messages.
 * Updated; based on https://www.eclipse.org/jetty/documentation/9.4.x/jetty-websocket-client-api.html
 *
 * Creator: David Jennings
 */
package com.esri.rttest.mon;

import java.net.URI;
import java.util.concurrent.TimeUnit;

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

        long cnt = -1L;
        long ts = 0L;

        cnt = socket.getCnt();
        ts = System.currentTimeMillis();


        return new Sample(cnt, ts);
    }

    @Override
    public void countEnded() {
        startClient();

    }

    WebSocketSinkMsg socket;


    final int MAX_MESSAGE_SIZE = 1000000;
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

        int numargs = args.length;

        if (numargs < 1) {
            System.err.println("Usage: WebSocketSink [ws-url] (sampleRateSec=10) (numSampleEqualBeforeExit=1)]");
            //System.err.println("Usage: WebSocketSink [ws-url] (sampleRateSec=10) (numSampleEqualBeforeExit=1) (printMmessages=false)]");
            System.err.println("NOTE: For GeoEvent Stream Service append /subscribe to the Web Socket URL.");
            System.err.println("Example: WebSocketSink ws://websats.westus2.cloudapp.azure.com/websats/SatStream/subscribe");
            System.err.println("");
            System.err.println("ws-url: Web Socket URL to consume");
            System.err.println("sampleRateSecs: How many seconds to wait between samples.");
            System.err.println("numSampleEqualBeforeExit: Summarize and reset after this many samples where count does not change.");
            //System.err.println("display-messages: true or false; If true messages are displayed counts ignored. Useful for low rates and validating messages.");
            System.err.println("");
        } else {

            String websockerurl = args[0];

            Integer sampleRateSec = 10;
            if (numargs > 1) {
                sampleRateSec = Integer.parseInt(args[1]);
            }

            Integer numSampleEqualBeforeExit = 1;
            if (numargs > 2) {
                numSampleEqualBeforeExit = Integer.parseInt(args[2]);
            }

            Boolean printMessages = false;
            if (numargs > 3) {
                printMessages = Boolean.parseBoolean(args[3]);

            }

            WebSocketSink webSocketSink = new WebSocketSink(websockerurl, sampleRateSec, numSampleEqualBeforeExit, printMessages);
            webSocketSink.run();

        }
    }

}
