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
 * Monitors a Solr Index.
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

public class SolrCoreMon extends Monitor {

    private static final Logger LOG = LogManager.getLogger(SolrCoreMon.class);

    @Override
    public void countEnded() {

    }

    @Override
    public Sample getSample() {

        LOG.info("Checking Count");

        long cnt = -1L;
        long ts = 0L;

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

    String solrSearchUrl;
    String user;
    String userpw;
    int sampleRateSec;
    boolean sendStdout;

    public SolrCoreMon(String solrSearchUrl, int sampleRateSec, String user, String userpw) {

        this.solrSearchUrl = solrSearchUrl;
        this.sampleRateSec = sampleRateSec;
        this.user = user;
        this.userpw = userpw;
    }


    public static void main(String[] args) {

        String solrSearchUrl = "";
        String username = "";   // default to empty string
        String password = "";  // default to empty string
        int sampleRateSec = 5; // default to 5 seconds.  

        LOG.info("Entering application.");
        int numargs = args.length;
        if (numargs < 1) {
            System.err.print("Usage: SolrCoreMon [SolrSearchURL] (sampleRateSec) ((username) (password))  \n");
            System.err.println("Example: java -cp target/rttest.jar com.esri.rttest.mon.SolrCoreMon http://localhost:8983/solr/realtime.safegraph 20 user pass");
        } else {
            solrSearchUrl = args[0];

            if (numargs > 1) {
                sampleRateSec = Integer.parseInt(args[1]);
            }

            if (numargs > 2 ) {
                username = args[2];
            }

            if (numargs > 3 ) {
                password = args[3];
            }

            SolrCoreMon t = new SolrCoreMon(solrSearchUrl, sampleRateSec, username, password);
            t.run();

        }

    }
}
