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
package com.esri.rttest.mon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
        long ts = 0L;

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


    Integer port;
    Boolean autoTerminate;
    Boolean displayMessages;
    ArrayList<TcpSinkServer> listTcpSinkServers = new ArrayList<>();
	private ServerSocket ss;

    
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

        int numargs = args.length;

        if (numargs < 1 || numargs > 5) {
            System.err.println("Usage: TcpSink <portToListenOn> (sampleRateSec=5) (numSampleEqualBeforeExit=1) (autoTerminate=true)");
            //System.err.println("Usage: TcpSink <portToListenOn> (sampleRateSec=5) (numSampleEqualBeforeExit=1) (autoTerminate=true) (displayMesages=false)");
            System.err.println("");
            System.err.println("portToListenOn: The port to listen on");
            System.err.println("sampleRateSecs: How many seconds to wait between samples.");
            System.err.println("numSampleEqualBeforeExit: Summarize and reset after this many samples where count does not change.");
            System.err.println("auto-terminate: true or false; If true when count stops increasing the socket is closed; for GeoEvent use false.");
            //System.err.println("display-messages: true or false; If true messages are displayed counts ignored. Useful for low rates and validating messages.");
            System.err.println("");
            
        } else {

            int port = Integer.parseInt(args[0]);

            int sampleEveryNSecs = 10;
            if (numargs > 1) {
                sampleEveryNSecs = Integer.parseInt(args[1]);
            }

            int numSampleEqualBeforeExit = 1;
            if (numargs > 2) {
                numSampleEqualBeforeExit = Integer.parseInt(args[2]);
            }


            boolean autoTerminate = true;
            if (numargs > 3) {
                autoTerminate = Boolean.parseBoolean(args[3]);
            }

            boolean displayMessages = false;
            if (numargs > 4) {
                displayMessages = Boolean.parseBoolean(args[4]);
            }

            new TcpSink(port, sampleEveryNSecs, numSampleEqualBeforeExit, autoTerminate, displayMessages);


        }

    }

}
