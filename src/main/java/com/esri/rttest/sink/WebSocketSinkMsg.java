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
 * Used by WebSocketSink2
 *
 * Creator: David Jennings
 */
package com.esri.rttest.sink;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 *
 * @author david
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class WebSocketSinkMsg {

    private static final Logger LOG = LogManager.getLogger(WebSocketSinkMsg.class);

    boolean printMessages;
    int sampleEvery;
    Integer numSamples;
    long cnt;
    SimpleRegression regression;

    public Long getCnt() {
        return cnt;
    }

    private final CountDownLatch closeLatch;
    @SuppressWarnings("unused")
    private Session session;

    public WebSocketSinkMsg(boolean printMessages) {
        this.closeLatch = new CountDownLatch(1);
        this.printMessages = printMessages;
        this.cnt = 0L;
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        this.session = null;
        this.closeLatch.countDown(); // trigger latch
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        //System.out.printf("Got connect: %s%n", session);
        this.session = session;

    }

    @OnWebSocketMessage
    public void onMessage(String msg) {
        this.cnt++;

        if (printMessages) {
            System.out.println(msg);
        }

    }
}
