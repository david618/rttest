package com.esri.rttest.mon;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class FeatureLayerMon extends  Monitor {
    private static final Logger LOG = LogManager.getLogger(FeatureLayerMon.class);


    String featureLayerURL;
    String token;

    public FeatureLayerMon() {}

    public FeatureLayerMon(String featureLayerURL, int sampleRateSec, int numSampleEqualBeforeExit, String token) {
        this.featureLayerURL = featureLayerURL;

        this.sampleRateSec = sampleRateSec;
        this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;

        this.token = token;

    }

    @Override
    public void countEnded() {

    }

    @Override
    public Sample getSample() {

        LOG.info("Checking Count");

        long cnt;
        long ts;
        JSONObject json = new JSONObject();

        try {

            String url = featureLayerURL + "/query?where=1%3D1&returnCountOnly=true&f=json";
            if ( !this.token.isEmpty()) {
                url = url + "&token=" + this.token;
                //System.out.println(url);
            }
            //System.out.println(url);
            json = httpQuery(url, "", "");


            cnt = json.getInt("count");

        } catch (JSONException e) {
            cnt = -1;
            try {
                String errorMessage = json.getJSONObject("error").getString("message");
                System.out.println(errorMessage);
            } catch (Exception e2 ) {
                System.out.println("feature layer may not exist");
            }
        }

        ts = System.currentTimeMillis();

        return new Sample(cnt, ts);
    }


    public static void main(String[] args) {

        FeatureLayerMon app = new FeatureLayerMon();
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
                .longOpt("feature-layer-url")
                .required()
                .hasArg()
                .desc("[Required] Feature Layer URL")
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

        Option tokenOp = Option.builder("t")
                .longOpt("token")
                .hasArg()
                .desc("Esri Token; defaults to empty string")
                .build();

        options.addOption(helpOp);
        options.addOption(urlOp);
        options.addOption(sampleRateSecOp);
        options.addOption(resetCountOp);
        options.addOption(tokenOp);

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

        String url = null;
        if (cmd.hasOption("l")) {
            url = cmd.getOptionValue("l");
        }
        System.out.println("url: " + url);

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

        String token = "";
        if(cmd.hasOption("t")) {
            token = cmd.getOptionValue("t");
        }
        System.out.println("token: " + token);

        app =  new FeatureLayerMon(url, sampleRateSec, numSampleEqualBeforeExit, token);
        app.run();

    }

}
