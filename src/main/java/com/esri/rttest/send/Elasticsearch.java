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
package com.esri.rttest.send;


import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.cli.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author david
 */
public class Elasticsearch extends Send {

    private static final Logger LOG = LogManager.getLogger(Elasticsearch.class);

    @Override
    public long sendBatch(ArrayList<String> lines) {

        Iterator<String> linesIterator = lines.iterator();

        long cnt = 0;

        // Assemble Lines into Post Body
        StringBuilder lineToSend = new StringBuilder();
        while (linesIterator.hasNext()) {
            lineToSend.append("{\"index\": {}}\n");
            lineToSend.append(linesIterator.next()).append("\n");
            cnt += 1;
        }

        // Post the line
        try {
            postLine(lineToSend.toString());
        } catch (Exception e) {
            cnt = 0;
        }

        return cnt;

    }

    @Override
    public void sendDone() {

    }

    public Elasticsearch() {}

    //private final String USER_AGENT = "Mozilla/5.0";
    private HttpClient httpClient;
    private HttpPost httpPost;

    String strURL;

    /**
     *
     * @param indexUrl Elastic Index
     */
    public void run(String indexUrl, String filename, Integer desiredRatePerSec, Long numToSend, boolean reuseFile) {

        try {

            if (indexUrl.endsWith("/")) {
                this.strURL = indexUrl + "_bulk";
            } else {
                this.strURL = indexUrl + "/_bulk";
            }

            httpClient = HttpClientBuilder.create().build();

            httpPost = new HttpPost(this.strURL);

            this.desiredRatePerSec = desiredRatePerSec;
            this.numToSend = numToSend;
            this.filename = filename;
            this.reuseFile = reuseFile;

            sendFiles();

        } catch (Exception e) {
            LOG.error("ERROR",e);
        }
    }

    private void postLine(String data) throws Exception {

        StringEntity postingString = new StringEntity(data);

        httpPost.setEntity(postingString);
        httpPost.setHeader("Content-type", "application/json");

        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        httpClient.execute(httpPost, responseHandler);

        //JSONObject jsonResp = new JSONObject(resp);
        //System.out.println(jsonResp);
        httpPost.releaseConnection();
    }


    public static void main(String[] args) {
        Elasticsearch app = new Elasticsearch();
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
                .longOpt("url")
                .required()
                .hasArg()
                .desc("[Required] Elasticsearch Index URL")
                .build();

        Option fileOp = Option.builder("f")
                .longOpt("file")
                .required()
                .hasArg()
                .desc("[Required] File with lines of text to send; if a folder is specified all files in the folder are sent one at a time alphabetically")
                .build();

        Option rateOp = Option.builder("r")
                .longOpt("rate")
                .required()
                .hasArg()
                .desc("[Required] Desired Rate. The tool will try to send at this rate if possible")
                .build();

        Option numToSendOp = Option.builder("n")
                .longOpt("number-to-send")
                .required()
                .hasArg()
                .desc("[Required] Number of lines to send")
                .build();

        Option onetimeOp = Option.builder("o")
                .longOpt("one-time")
                .desc("Send lines only one time. Stop when all lines have been sent.")
                .build();

        options.addOption(helpOp);
        options.addOption(urlOp);
        options.addOption(fileOp);
        options.addOption(rateOp);
        options.addOption(numToSendOp);
        options.addOption(onetimeOp);

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

        String indexUrl = null;
        if (cmd.hasOption("l")) {
            indexUrl = cmd.getOptionValue("l");
        }
        System.out.println("indexUrl: " + indexUrl);

        String file = null;
        if(cmd.hasOption("f")) {
            file = cmd.getOptionValue("f");
        }
        System.out.println("file: " + file);

        Integer desiredRatePerSec = null;
        if(cmd.hasOption("r")) {
            try {
                desiredRatePerSec = Integer.parseInt(cmd.getOptionValue("r"));
            } catch (NumberFormatException e ) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid value for rate (r).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("desiredRatePerSec: " + desiredRatePerSec);

        Long numToSend = null;
        if(cmd.hasOption("n")) {
            try {
                numToSend = Long.parseLong(cmd.getOptionValue("n"));
            } catch (NumberFormatException e) {
                System.out.println();
                System.out.println("Invalid value for num-to-send (n). Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("numToSend: " + numToSend);

        String contentType = "text/plain";
        if(cmd.hasOption("c")) {
            contentType = cmd.getOptionValue("c");
        }
        System.out.println("contentType: " + contentType);

        int numThreads = 1;
        if(cmd.hasOption("t")) {
            try {
                String tmpStr = cmd.getOptionValue("t");
                numThreads = Integer.parseInt(tmpStr);
            } catch (NumberFormatException e) {
                System.out.println();
                System.out.println("Invalid value for num-threads (t). Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("numThreads: " + numThreads);


        boolean reuseFile = true;
        if(cmd.hasOption("o")) {
            reuseFile = false;
        }
        System.out.println("reuseFile : " + reuseFile);


        app.run(indexUrl, file, desiredRatePerSec, numToSend, reuseFile);

    }

}
