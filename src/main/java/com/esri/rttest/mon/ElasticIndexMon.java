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
 *
 */
package com.esri.rttest.mon;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author david
 */
public class ElasticIndexMon extends Monitor {

    private static final Logger LOG = LogManager.getLogger(ElasticIndexMon.class);

    @Override
    public void countEnded() {

    }

    @Override
    public Sample getSample() {
        long cnt ;
        long ts;

        LOG.info("Checking Count");

        String url = elasticSearchUrl + "/_count";

        try {

            //System.out.println(url);
            JSONObject json = httpQuery(url, username, password);

            cnt = json.getInt("count");

        } catch (JSONException e) {
            cnt = -1;
            System.out.println("Index may not exist");
        }

        ts = System.currentTimeMillis();

        return new Sample(cnt, ts);

    }


    public ElasticIndexMon() {}

    String elasticSearchUrl;
    String username;
    String password;

    public ElasticIndexMon(String elasticSearchUrl, int sampleRateSec, int numSampleEqualBeforeExit, String username, String password) {


        this.elasticSearchUrl = elasticSearchUrl;

        this.sampleRateSec = sampleRateSec;
        this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;

        this.username = username;
        this.password = password;
    }


    public static void main(String[] args) {

        ElasticIndexMon app = new ElasticIndexMon();
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
                .longOpt("elastic-index-url")
                .required()
                .hasArg()
                .desc("[Required] Elastic Index URL (e.g. http://es:9200/planes)")
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

        Option usernameOp = Option.builder("u")
                .longOpt("username")
                .hasArg()
                .desc("Mqtt Server Username; default no username")
                .build();

        Option passwordOp = Option.builder("p")
                .longOpt("password")
                .hasArg()
                .desc("Mqtt Server Password; default no password")
                .build();

        options.addOption(helpOp);
        options.addOption(urlOp);
        options.addOption(sampleRateSecOp);
        options.addOption(resetCountOp);
        options.addOption(usernameOp);
        options.addOption(passwordOp);

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
            System.out.println("Send lines from a file to an Elastic Server");
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
        if (cmd.hasOption("r")) {
            try {
                sampleRateSec = Integer.parseInt(cmd.getOptionValue("r"));
            } catch (NumberFormatException e) {
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
        if (cmd.hasOption("n")) {
            try {
                numSampleEqualBeforeExit = Integer.parseInt(cmd.getOptionValue("n"));
            } catch (NumberFormatException e) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid num-samples-no-change (s).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("numSampleEqualBeforeExit: " + numSampleEqualBeforeExit);

        String username = "";
        if (cmd.hasOption("u")) {
            username = cmd.getOptionValue("u");
        }
        System.out.println("username: " + username);


        String password = "";
        if (cmd.hasOption("p")) {
            password = cmd.getOptionValue("p");
        }
        System.out.println("password: " + password);

        app = new ElasticIndexMon(url, sampleRateSec, numSampleEqualBeforeExit, username, password);
        app.run();
    }
}

