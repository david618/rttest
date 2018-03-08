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
 * Listens on TCP Port for messages.
 * Counts Messages based on value of sampleEveryMessages adds a point to the linear regresssion
 * After collecting three samples it will output the rate.
 * After 10 second pause the count and regression are reset.
 *
 * Creator: David Jennings
 */
package com.esri.rttest.sink;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author david
 */
public class TcpSink {
    
    private static final Logger LOG = LogManager.getLogger(TcpSink.class);
    

    private class GetCounts extends TimerTask {

        ArrayList<TcpSinkServer> tssList;
        Boolean autoTerminate;

        long st = 0L;
        long currentCnt = 0L;
        long prevCnt = 0L;
        SimpleRegression regression;
        int numSamples = 0;
        long tm;

        public GetCounts(ArrayList<TcpSinkServer> tssList, Boolean autoTerminate) {
            this.tssList = tssList;
            this.autoTerminate = autoTerminate;
            st = 0L;
            currentCnt = 0L;
            prevCnt = 0L;
            regression = new SimpleRegression();
            tm = 0L;
        }

        @Override
        public void run() {
            currentCnt = 0L;
            
            for (TcpSinkServer tss : tssList) {
                currentCnt += tss.getCnt();
            }

            if (currentCnt > prevCnt) {

                if (prevCnt == 0) {
                    st = System.currentTimeMillis();

                    for (TcpSinkServer tss : tssList) {
                        long tssSt = tss.getFirstTime();
                        if (tssSt < st && tssSt > 0) {
                            st = tss.getFirstTime();
                        }
                    }

                }
                numSamples += 1;
                tm = System.currentTimeMillis();
                regression.addData(tm, currentCnt);

                if (numSamples > 2) {
                    double rcvRate = regression.getSlope() * 1000;
                    System.out.println(numSamples + "," + tm + "," + currentCnt + "," + String.format("%.0f", rcvRate));
                } else {
                    System.out.println(numSamples + "," + tm + "," + currentCnt);
                }

            } else {
                if (currentCnt > 0) {
                    //System.out.println("Done");
                    long et = 0;
                    for (TcpSinkServer tss : tssList) {
                        if (tss.lastTime > et) {
                            et = tss.lastTime;
                        }
                      
                        if (autoTerminate) {
                            tss.terminate();
                            
                        } else {
                            tss.reset();
                        }
                        
                        

                    }

                    numSamples -= 1;
                    // Remove the last sample
                    regression.removeData(tm, currentCnt);
                    System.out.println("Removing: " + tm + "," + currentCnt);
                    // Output Results
                    double rcvRate = regression.getSlope() * 1000;  // converting from ms to seco                    

                    if (numSamples > 4) {
                        double rateStdErr = regression.getSlopeStdErr();
                        System.out.format("%d , %.0f, %.4f\n", currentCnt, rcvRate, rateStdErr);
                    } else if (numSamples >= 2) {
                        System.out.format("%d , %.0f\n", currentCnt, rcvRate);
                    } else {
                        System.out.println("Not enough samples to calculate rate. ");
                    }

                    Double rate = ((double) currentCnt / (double) (et - st) * 1000.0);
                    System.out.println("Total Count: " + currentCnt);
                    System.out.format("Average Rate: %.0f\n",rate);
                    currentCnt = 0L;
                    prevCnt = 0L;
                    regression = new SimpleRegression();
                    tm = 0L;
                    numSamples = 0;
                }

            }
            prevCnt = currentCnt;

        }

    }

    private void listenForConnections() {
        try {
            ServerSocket ss = new ServerSocket(port);

            System.out.println("After starting this; create or restart the GeoEvent service.");
            System.out.println("Once connected you see a 'Thread Started' message for each connection.");

            ArrayList<TcpSinkServer> tssList = new ArrayList<>();

            // Setup Timer to Get Counts
            Timer timer = new Timer(true);
            timer.scheduleAtFixedRate(new GetCounts(tssList, autoTerminate), 0, this.sampleEveryNSecs * 1000);

            while (true) {
                Socket cs = ss.accept();
                TcpSinkServer ts = new TcpSinkServer(cs, displayMessages);
                ts.start();
                tssList.add(ts);
            }

        } catch (IOException e) {
            LOG.error("ERROR",e);
        }
    }

    Integer port;
    Integer sampleEveryNSecs;
    Boolean autoTerminate;
    Boolean displayMessages;
       
    
    public TcpSink(Integer port, Integer sampleEveryNSecs, Boolean autoTerminate, Boolean displayMessages) {
        this.port = port;
        this.sampleEveryNSecs = sampleEveryNSecs;
        this.autoTerminate = autoTerminate;
        this.displayMessages = displayMessages;
        listenForConnections();

    }

    public static void main(String[] args) {

        // Command Line Arg is port you want to listen on. 5565         
        /*
        NOTE: For latency calculations ensure all servers including the server running simulation
        are using time chrnonized.

        Run this command simulatneously on machines to compare time
        $ date +%s

        NTP command force update now:  $ sudo ntpdate -s time.nist.gov
        CHRONYD check status: $ chronyc tracking

         */
        int numargs = args.length;

        if (numargs < 1 || numargs > 4) {
            System.err.println("Usage: TcpSink <port-to-listen-on> (<sample-every-N-seconds/5>) (<auto-terminate/true>) (<display-messages/false>)\n");
            System.err.println("port-to-listen-on: The port to listen on");
            System.err.println("samples-every-N-seconds: Will gather a sample every N seconds for linear regression and estimation of rate.");
            System.err.println("auto-terminate: true or false defaults to true. If true when count stops increasing the socket is closed; GeoEvent expects socket to stay open.");
            System.err.println("display-messages: true or false default to false. If true messages are displayed counts ignored. Useful for low rates and validating messages.");
            
        } else {

            
            switch (numargs) {
                case 1:
                    new TcpSink(Integer.parseInt(args[0]), 5, true, false);
                    break;
                case 2:
                    new TcpSink(Integer.parseInt(args[0]), Integer.parseInt(args[1]), true, false);
                    break;
                case 3:
                    new TcpSink(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Boolean.parseBoolean(args[2]), false);
                    break;
                default:
                    new TcpSink(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Boolean.parseBoolean(args[2]), Boolean.parseBoolean(args[3]));
                    break;
            }

        }

    }

}
