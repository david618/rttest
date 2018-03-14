///*
// * (C) Copyright 2017 David Jennings
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// * Contributors:
// *     David Jennings
// */
///**
// * Listens on on Web Socket for messages.
// * Counts Messages based on value of sampleEveryMessages adds a point to the linear regresssion
// * After collecting three samples it will output the rate.
// * After 10 second pause the count and regression are reset.
// *
// * Creator: David Jennings
// */
//package com.esri.rttest.sink;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.util.concurrent.TimeUnit;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.eclipse.jetty.util.ssl.SslContextFactory;
//import org.eclipse.jetty.websocket.WebSocket;
//import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
//import org.eclipse.jetty.websocket.client.WebSocketClient;
//
///**
// *
// * @author david
// */
//public class WebSocketSink2 {
//
//    private static final Logger LOG = LogManager.getLogger(WebSocketSink2.class);
//
//    final int MAX_MESSAGE_SIZE = 1000000;
//
//    public void connectWebsocket(String url, int sampleEveryN, boolean showMessages) {
//
//        SslContextFactory sslContextFactory = new SslContextFactory();
//        sslContextFactory.setTrustAll(true);
//        sslContextFactory.setValidateCerts(false);
//
//        WebSocketClient client = new WebSocketClient(sslContextFactory);
//        WebSocketSinkMsg2 socket = new WebSocketSinkMsg2(sampleEveryN, showMessages);
//        try {
//            URI uri = new URI(url);
//            ClientUpgradeRequest request = new ClientUpgradeRequest();
//            client.connect(socket, uri, request);
//
//            // wait for closed socket connection
//            socket.awaitClose(3600, TimeUnit.SECONDS);
//
//        } catch (IOException | InterruptedException | URISyntaxException e) {
//            LOG.error("ERROR", e);
//        } 
//
//    }
//
//    public static void main(String[] args) {
//
//        int numargs = args.length;
//
//        if (numargs < 1 || numargs > 3) {
//            System.err.print("Usage: WebSocketSink <ws-url> (<sample-every-N-records/1000>) (<display-messages/false>)\n");
//            System.err.print("NOTE: For GeoEvent Stream Service append /subscribe to the Web Socket URL\n");
//        } else {
//            WebSocketSink2 a = new WebSocketSink2();
//
//            switch (numargs) {
//                case 1:
//                    a.connectWebsocket(args[0], 1000, false);
//                    break;
//                case 2:
//                    a.connectWebsocket(args[0], Integer.parseInt(args[1]), false);
//                    break;
//                default:
//                    a.connectWebsocket(args[0], Integer.parseInt(args[1]), Boolean.parseBoolean(args[2]));
//                    break;
//            }
//
//        }
//    }
//
//}
