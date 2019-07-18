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
 * Monitors an Elasticsearch Index/Type.
 * Periodically does a count and when count is changing collects samples.
 * After three samples are made outputs rates based on linear regression.
 * After counts stop changing outputs the final rate and last estimated rate.
 *
 * Creator: David Jennings
 */
package com.esri.rttest.mon;

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
        long cnt = -1L;
        long ts = 0L;

            LOG.info("Checking Count");

            String url = elasticSearchUrl + "/_count";

            try {

                //System.out.println(url);
                JSONObject json = httpQuery(url, user, userpw);

                cnt = json.getInt("count");

            } catch (JSONException e) {
                cnt = -1;
                System.out.println("Index may not exist");
            }

            ts = System.currentTimeMillis();

            return new Sample(cnt, ts);

        }



    String elasticSearchUrl;
    String user;
    String userpw;

    public ElasticIndexMon(String elasticSearchUrl, int sampleRateSec, int numSampleEqualBeforeExit, String user, String userpw) {


        this.elasticSearchUrl = elasticSearchUrl;

        this.sampleRateSec = sampleRateSec;
        this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;

        this.user = user;
        this.userpw = userpw;
    }



    public static void main(String[] args) {

        String elasticSearchUrl = "";
        String username = "";   // default to empty string
        String password = "";  // default to empty string
        int sampleRateSec = 10; // default to 10 seconds.
        int numSampleEqualBeforeExit = 1;

        LOG.info("Entering application.");
        int numargs = args.length;
        if (numargs < 1) {
            System.err.println("Usage: ElasticIndexMon [elasticsearchUrl] (sampleRateSec=10) (numSampleEqualBeforeExit=1) (username=\"\") (password=\"\")");
            System.err.println("Example: ElasticIndexMon http://es:9200/planes 20 elasic changeme");
            System.err.println("");
            System.err.println("elasticsearchUrl: Elasticsearch Index URL");
            System.err.println("sampleRateSecs: How many seconds to wait between samples.");
            System.err.println("numSampleEqualBeforeExit: Summarize and reset after this many samples where count does not change.");
            System.err.println("username: Elasticsearch username");
            System.err.println("password: Elasticsearch password");
            System.err.println("");
        } else {
            elasticSearchUrl = args[0];

            if (numargs > 1) {
                sampleRateSec = Integer.parseInt(args[1]);
                if (sampleRateSec < 1) {
                    System.err.println("SampleRateSec must be greater than 0");
                    System.exit(1);
                }
            }

            if (numargs > 2) {
                numSampleEqualBeforeExit = Integer.parseInt(args[2]);
                if (numSampleEqualBeforeExit < 1) {
                    System.err.println("numSampleEqualBeforeExit must be greater than 1");
                    System.exit(2);
                }

            }

            if (numargs > 3) {
                username = args[3];
            }

            if (numargs > 4) {
                password = args[4];
            }

            ElasticIndexMon t = new ElasticIndexMon(elasticSearchUrl, sampleRateSec, numSampleEqualBeforeExit, username, password);
            t.run();

        }

    }
}

