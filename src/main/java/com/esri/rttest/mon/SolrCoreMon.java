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
 * Monitors a Solr Index.
 * Periodically does a count and when count is changing collects samples.
 * After three samples are made outputs rates based on linear regression.
 * After counts stop changing outputs the final rate and last estimated rate.
 *
 * Creator: David Jennings
 */
package com.esri.rttest.mon;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class SolrCoreMon extends Monitor {

    private static final Logger LOG = LogManager.getLogger(SolrCoreMon.class);

    @Override
    public void countEnded() {

    }

    @Override
    public Sample getSample() {

        LOG.info("Checking Count");

        long cnt ;
        long ts;

        try {
            String url = solrSearchUrl + "/select?q=*:*&wt=json&rows=0";
            JSONObject json = httpQuery(url, "", "");

            cnt = json.getInt("count");

        } catch (JSONException e) {
            cnt = -1;
            System.out.println("Index may not exist");
        }

        ts = System.currentTimeMillis();

        return new Sample(cnt, ts);
    }

    public SolrCoreMon() {}

    String solrSearchUrl;
    String username;
    String password;
    int sampleRateSec;

    public SolrCoreMon(String solrSearchUrl, int sampleRateSec, String username, String password) {

        this.solrSearchUrl = solrSearchUrl;
        this.sampleRateSec = sampleRateSec;
        this.username = username;
        this.password = password;
    }


    public static void main(String[] args) {

        SolrCoreMon app = new SolrCoreMon();
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
                .longOpt("solr-core-url")
                .required()
                .hasArg()
                .desc("[Required] Solr Core URL (e.g. http://solr:8933/solr/planes)")
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

        String solrSearchUrl = null;
        if (cmd.hasOption("l")) {
            solrSearchUrl = cmd.getOptionValue("l");
        }
        System.out.println("url: " + solrSearchUrl);

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

        app = new SolrCoreMon(solrSearchUrl, sampleRateSec, username, password);
        app.run();

    }
}
