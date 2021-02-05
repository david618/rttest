package com.esri.rttest.mon;

import com.sun.net.httpserver.HttpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

public class HttpSink extends  Monitor {

    private static final Logger LOG = LogManager.getLogger(TcpSink.class);

    @Override
    public Sample getSample() {

        long cnt = -1L;
        long ts = 0L;

        cnt = httpSinkHandler.getCnt();

        ts = System.currentTimeMillis();


        return new Sample(cnt, ts);
    }

    @Override
    public void countEnded() {

    }

    HttpSinkHandler httpSinkHandler;

    public HttpSink(int port, Integer sampleRateSec, Integer numSampleEqualBeforeExit, Boolean displayMessage) {

        this.port = port;

        // For Monitor
        this.sampleRateSec = sampleRateSec;
        this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;

        //this.displayMessages = displayMessages;

        try {

            System.out.println("After starting this; create or restart the sending service.");
            System.out.println("Once connected you see a 'Thread Started' message for each connection.");

            httpSinkHandler = new HttpSinkHandler();
            HttpServer inserver = HttpServer.create(new InetSocketAddress(port), 0);
            inserver.createContext("/", httpSinkHandler);
            inserver.start();


            // Start the Timer
            run();




        } catch (Exception e) {
            LOG.error("ERROR",e);
            e.printStackTrace();
        }


    }

    int port;
    boolean displayMessages;

    public static void main(String[] args) {

        int numargs = args.length;

        if (numargs < 1 || numargs > 5) {
            System.err.println("Usage: HttpSink <portToListenOn> (sampleRateSec=5) (numSampleEqualBeforeExit=1)");
            //System.err.println("Usage: HttpSink <portToListenOn> (sampleRateSec=5) (numSampleEqualBeforeExit=1) (displayMesages=false)");
            System.err.println("");
            System.err.println("portToListenOn: The port to listen on");
            System.err.println("sampleRateSec: Will gather a sample every N seconds for linear regression and estimation of rate.");
            System.err.println("numSampleEqualBeforeExit: Number of samples that are equal before exit");
            //System.err.println("display-messages: true or false default to false. If true messages are displayed counts ignored. Useful for low rates and validating messages.");
            System.err.println("");

        } else {

            int port = Integer.parseInt(args[0]);

            int sampleRateSec = 10;
            if (numargs > 1) {
                sampleRateSec = Integer.parseInt(args[1]);
            }

            int numSampleEqualBeforeExit = 1;
            if (numargs > 2) {
                numSampleEqualBeforeExit = Integer.parseInt(args[2]);
            }

            boolean displayMessages = false;
            if (numargs > 3) {
                displayMessages = Boolean.parseBoolean(args[3]);
            }

            new HttpSink(port, sampleRateSec, numSampleEqualBeforeExit, displayMessages);


        }

    }

}
