package com.esri.rttest.mon;

import com.sun.net.httpserver.HttpServer;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;

public class HttpSink extends  Monitor {

    private static final Logger LOG = LogManager.getLogger(TcpSink.class);

    @Override
    public Sample getSample() {

        long cnt;
        long ts;

        cnt = httpSinkHandler.getCnt();

        ts = System.currentTimeMillis();


        return new Sample(cnt, ts);
    }

    @Override
    public void countEnded() {

    }

    public HttpSink() {}

    HttpSinkHandler httpSinkHandler;

    public HttpSink(Integer port, Integer sampleRateSec, Integer numSampleEqualBeforeExit, Boolean printMessages) {

        this.port = port;

        // For Monitor
        this.sampleRateSec = sampleRateSec;
        this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;

        this.printMessages = printMessages;

        try {

            System.out.println("After starting this; create or restart the sending service.");
            System.out.println("Once connected you see a 'Thread Started' message for each connection.");

            httpSinkHandler = new HttpSinkHandler(printMessages);
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
    boolean printMessages;

    public static void main(String[] args) {


        HttpSink app = new HttpSink();
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

        options.addOption(helpOp);
        options.addOption(brokersOp);
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

        app =  new HttpSink(port, sampleRateSec, numSampleEqualBeforeExit, printMessages);
        app.run();

    }

}
