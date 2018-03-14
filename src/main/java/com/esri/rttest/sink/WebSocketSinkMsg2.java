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
// * Used by WebSocketSink2
// *
// * Creator: David Jennings
// */
//package com.esri.rttest.sink;
//
//import java.util.Timer;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.Future;
//import java.util.concurrent.TimeUnit;
//import org.apache.commons.math3.stat.regression.SimpleRegression;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.eclipse.jetty.websocket.api.Session;
//import org.eclipse.jetty.websocket.api.StatusCode;
//import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
//import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
//import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
//import org.eclipse.jetty.websocket.api.annotations.WebSocket;
//
///**
// *
// * @author david
// */
//@WebSocket (maxTextMessageSize = 64 * 1024)
//public class WebSocketSinkMsg2 {
//    
//    private final CountDownLatch closeLatch;
//    @SuppressWarnings("unused")
//    private Session session;
//
//    private static final Logger LOG = LogManager.getLogger(WebSocketSinkMsg.class);
//
//    boolean printmessages;
//    int sampleEvery;
//    
//    Timer timer;
//    int numSamples;
//    SimpleRegression regression;
//    private Integer cnt = 0;    
//
//    public WebSocketSinkMsg2(int sampleEvery, boolean printmessages) {
//        this.printmessages = printmessages;
//        this.sampleEvery = sampleEvery;
//        this.numSamples = 0;
//        this.cnt = 0;
//        this.timer = new Timer();
//        this.regression = new SimpleRegression();
//        this.closeLatch = new CountDownLatch(1);
//
//    }    
//    
//    public Integer getCnt() {
//        return cnt;
//    }    
//    
//   
//
//    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException
//    {
//        return this.closeLatch.await(duration,unit);
//    }
//    
//    
//
//    @OnWebSocketClose
//    public void onClose(int statusCode, String reason)
//    {
//        this.session = null;
//        double rcvRate = 0.0;
//        if (numSamples >= 3) {
//            rcvRate = regression.getSlope() * 1000;
//        }
//        if (numSamples > 5) {
//            double rateStdErr = regression.getSlopeStdErr();
//            System.out.println("Number of Samples,Count,Rate,StdErr");
//            System.out.format("%d, %d , %.2f, %.4f\n", numSamples, cnt, rcvRate, rateStdErr);
//        } else if (numSamples >= 3) {
//            System.out.println("Number of Samples,Count,Rate,StdErr");
//            System.out.format("%d, %d , %.2f\n", numSamples, cnt, rcvRate);
//        }
//    }
//
//    @OnWebSocketConnect
//    public void onConnect(Session session)
//    {
//        this.session = session;
//        if (cnt > 0) {
//            System.out.println("Websocket connected");
//            numSamples = 0;
//            cnt = 0;
//            timer = new Timer();
//            regression = new SimpleRegression();
//
//        } else {
//            System.out.println("Listening");
//        }
//    }
//
//    
//    @OnWebSocketMessage
//    public void onMessage(String msg)
//    {
//        cnt++;
//
//        if (printmessages) {
//            System.out.println(msg);
//        } else if (cnt % sampleEvery == 0) {
//            long t = System.currentTimeMillis();
//            regression.addData(t, cnt);
//            numSamples += 1;
//            if (numSamples > 2) {
//                double rcvRate = regression.getSlope() * 1000;
//                System.out.println(numSamples + "," + t + "," + cnt + "," + rcvRate);
//            } else {
//                System.out.println(numSamples + "," + t + "," + cnt);
//            }
//        }
//    }    
//}
