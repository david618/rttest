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
 * Test Class
 *
 * Try using HTTP Post /bulk api
 * vip: data.elastic.l4lb.thisdcos.directory:9200 http
 * vip: data.elastic.l4lb.thisdcos.directory:9300 transport
 *
 * Having issues with DCOS Elastic framework and Transport Client
 * The Transport Client works fine with Elasticsearch 5 installed outside of DCOS.
 *
 * David Jennings
 *
 * 13 Nov 2017
 * NOTE: Based on testing using sparktest; I suspect if I hyper-threaded this like I did for tcp I could get faster rates.
 * In a Round-robin fashion send requests to each of the elasticsearch nodes.
 *
 */
package com.esri.rttest.send;


import java.util.ArrayList;
import java.util.Iterator;

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

        // Assemley Lines into Post Body
        StringBuilder lineToSend = new StringBuilder();
        while (linesIterator.hasNext()) {
            lineToSend.append("{\"index\": {}}\n");
            lineToSend.append(linesIterator.next() + "\n");
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

    //private final String USER_AGENT = "Mozilla/5.0";
    private HttpClient httpClient;
    private HttpPost httpPost;

    private String strURL;  // http://data.sats-sat03.l4lb.thisdcos.directory:9200/index/type

    /**
     *
     * @param indexUrl
     */
    public Elasticsearch(String indexUrl, String filename, Integer desiredRatePerSec, Long numToSend, boolean reuseFile) {

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
        String resp = httpClient.execute(httpPost, responseHandler);

        //JSONObject jsonResp = new JSONObject(resp);
        //System.out.println(jsonResp);
        httpPost.releaseConnection();
    }


    public static void main(String[] args) {

        int numargs = args.length;
        if (numargs < 4 ) {
            System.err.print("Usage: Elasticsearch [indexURL] [file] [desiredRatePerSec] [numToSend] (reuseFile=true) \n");
        } else {

            String indexUrl = args[0];
            String file = args[1];
            Integer desiredRatePerSec = Integer.parseInt(args[2]);
            Long numToSend = Long.parseLong(args[3]);

            boolean reuseFile = true;
            if (numargs > 4) {
                reuseFile = Boolean.parseBoolean(args[4]);
            }

            Elasticsearch t = new Elasticsearch(indexUrl, file, desiredRatePerSec, numToSend, reuseFile);

        }


    }

}
