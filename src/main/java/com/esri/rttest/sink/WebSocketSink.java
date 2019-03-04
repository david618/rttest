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
package com.esri.rttest.sink;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

/**
 *
 * @author david
 */
public class WebSocketSink {

    private static final Logger LOG = LogManager.getLogger(WebSocketSink.class);

    WebSocketSinkMsg socket;

    // ******************* TimerTask Class ******************************
    class CheckCount extends TimerTask {

        long cnt1 = 0;
        long cnt2 = -1;
        long startCount = 0;
        long endCount = 0;
        int numSamples = 0;
        HashMap<Long, Long> samples;
        long t1 = 0L;
        long t2 = 0L;
        SimpleRegression regression;

        public CheckCount() {
            regression = new SimpleRegression();
            cnt1 = 0;
            cnt2 = -1;
            startCount = 0;
            numSamples = 0;
            t1 = 0L;
            t2 = 0L;

        }

        boolean inCounting() {
            if (cnt1 > 0) {
                return true;
            } else {
                return false;
            }
        }

        HashMap<Long, Long> getSamples() {
            return samples;
        }

        long getStartCount() {
            return startCount;
        }

        long getEndCount() {
            return endCount;
        }

        @Override
        public void run() {

            try {
                LOG.info("Checking Count");

                cnt1 = socket.getCnt();
                t1 = System.currentTimeMillis();

                if (cnt2 == -1) {
                    if (sendStdout) {
                        System.out.println("Watching for changes in count...  Use Ctrl-C to Exit.");
                        System.out.println("|Sample Number|Epoch|Count|Linear Regression Rate|Approx. Instantaneous Rate|");
                        System.out.println("|-------------|-----|-----|----------------------|--------------------------|");
                    }
                }

                if (cnt2 == -1 || cnt1 < cnt2) {
                    cnt2 = cnt1;
                    startCount = cnt1;
                    endCount = cnt1;
                    regression = new SimpleRegression();
                    samples = new HashMap<>();
                    numSamples = 0;

                } else if (cnt1 > cnt2) {
                    // Increase number of samples
                    numSamples += 1;

                    // Add to Linear Regression
                    regression.addData(t1, cnt1);
                    samples.put(t1, cnt1);

                    if (numSamples >= 2) {
                        double regRate = regression.getSlope() * 1000;
                        double iRate = (double) (cnt1 - cnt2) / (double) (t1 - t2) * 1000.0;
                        if (sendStdout) {
                            System.out.println("| " + numSamples + " | " + t1 + " | " + (cnt1 - startCount) + " | " + String.format("%.0f", regRate) + " | " + String.format("%.0f", iRate) + " |");
                        }
                    } else {
                        if (sendStdout) {
                            System.out.println("| " + numSamples + " | " + t1 + " | " + (cnt1 - startCount) + " |           |           |");
                        }
                    }

                } else if (cnt1 == cnt2 && numSamples > 0) {

                    if (sendStdout) {
                        System.out.println("Count is no longer increasing...");
                    }

                    endCount = cnt1;

                    numSamples -= 1;
                    // Remove the last sample
                    regression.removeData(t2, cnt2);
                    samples.remove(t2, cnt2);

                    // Calculate Average Rate
                    long minTime = Long.MAX_VALUE;
                    long maxTime = Long.MIN_VALUE;
                    long minCount = Long.MAX_VALUE;
                    long maxCount = Long.MIN_VALUE;
                    for (Map.Entry pair : samples.entrySet()) {
                        long time = (long) pair.getKey();
                        long count = (long) pair.getValue();
                        if (time < minTime) {
                            minTime = time;
                        }
                        if (time > maxTime) {
                            maxTime = time;
                        }
                        if (count < minCount) {
                            minCount = count;
                        }
                        if (count > maxCount) {
                            maxCount = count;
                        }
                    }
                    double avgRate = (double) (maxCount - minCount) / (double) (maxTime - minTime) * 1000.0;

                    if (sendStdout) {
                        System.out.println("Removing sample: " + t2 + "|" + (cnt2 - startCount));
                    }

                    // Output Results
                    long cnt = cnt2 - startCount;

                    double regRate = regression.getSlope() * 1000;  // converting from ms to seconds

                    //if (numSamples > 5) {
                    //    double rateStdErr = regression.getSlopeStdErr();
                    //    if (sendStdout) {
                    //      System.out.format("%d , %.0f, %.4f\n", cnt, regRate, rateStdErr);
                    //        System.out.format("%d , %.0f\n", cnt, regRate);
                    //    }
                    //} else if (numSamples >= 2) {
                    if (numSamples >= 2) {
                        if (sendStdout) {
                            System.out.format("Total Count: %,d | Linear Regression Rate:  %,.0f | Average Rate: %,.0f\n\n", cnt, regRate, avgRate);
                        }
                    } else {
                        if (sendStdout) {
                            System.out.format("Total Count: %,d | Not enough samples Rate calculations. \n\n", cnt);
                        }
                    }

                    // Reset 
                    cnt1 = -1;
                    cnt2 = -1;
                    startCount = 0;
                    numSamples = 0;
                    t1 = 0L;
                    t2 = 0L;
                    regression = new SimpleRegression();

                }

                cnt2 = cnt1;
                t2 = t1;
            } catch (Exception e) {
                LOG.error("ERROR", e);

            }

        }

    }
    // *****************************************************************

    final int MAX_MESSAGE_SIZE = 1000000;
    boolean sendStdout;
    Timer timer;
    long sampleRate;
    String destUri;

    public WebSocketSink(String url, Integer sampleRateSec, boolean printMessages, boolean sendStdout) {

        this.sendStdout = sendStdout;
        this.sampleRate = sampleRateSec;

        this.destUri = url;
        //String destUri = "ws://echo.websocket.org";

        socket = new WebSocketSinkMsg(printMessages);

    }

    public void run() {
        try {

            timer = new Timer();
            timer.schedule(new WebSocketSink.CheckCount(), 0, sampleRate * 1000);

            WebSocketClient client = new WebSocketClient();
            try {
                client.start();

                URI echoUri = new URI(destUri);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                client.connect(socket, echoUri, request);
                //System.out.printf("Connecting to : %s%n", echoUri);

                // wait for closed socket connection.
                socket.awaitClose(5, TimeUnit.DAYS);
            } catch (Exception t) {
                t.printStackTrace();
            } finally {
                try {
                    client.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            LOG.error("ERROR", e);
        }

    }

    public static void main(String[] args) {

        int numargs = args.length;

        if (numargs < 1) {
            System.err.println("Usage: WebSocketSink (ws-url) [(sample-rate-sec) (print-messages=false)]");
            System.err.println("NOTE: For GeoEvent Stream Service append /subscribe to the Web Socket URL.");
        } else {

            String websockerurl = args[0];
            Integer sampleRateSec = 5;
            Boolean printMessages = false;
            Boolean sendStdout = true;

            LOG.info("Entering application");

            if (numargs > 1) {
                sampleRateSec = Integer.parseInt(args[1]);
            }
            if (numargs > 2) {
                printMessages = Boolean.parseBoolean(args[2]);
                if (printMessages) {
                    sendStdout = false;
                }
            }

            WebSocketSink webSocketSink = new WebSocketSink(websockerurl, sampleRateSec, printMessages, sendStdout);
            webSocketSink.run();
        }
    }

}
