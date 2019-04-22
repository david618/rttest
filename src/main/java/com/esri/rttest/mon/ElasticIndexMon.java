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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.http.Header;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author david
 */
public class ElasticIndexMon {

    private static final Logger LOG = LogManager.getLogger(ElasticIndexMon.class);

    class CheckCount extends TimerTask {

        long cnt0 = -1;  // current sample
        long cnt1 = -1;  // previous sample
        long cnt2 = -1;  // previous previous sample
        long t0 = 0L;
        long t1 = 0L;
        long t2 = 0L;
        long startCount = 0;
        long firstSampleTime = 0;
        long firstSampleCount = 0;
        int numSamples = 0;
        SimpleRegression regression;

        public CheckCount() {
            cnt0 = -1;
            cnt1 = -1;
            cnt2 = -1;
            t0 = 0L;
            t1 = 0L;
            t2 = 0L;
            startCount = 0;
            firstSampleTime = 0;
            firstSampleCount = 0;
            numSamples = 0;
            regression = new SimpleRegression();
        }

        boolean inCounting() {
            if (cnt2 > 0) {
                return true;
            } else {
                return false;
            }
        }

        long getStartCount() {
            return startCount;
        }


        private void resetStart() {
            startCount = cnt0;

            regression = new SimpleRegression();
            numSamples = 0;
            
            firstSampleCount = 0;
            firstSampleTime = 0;

            System.out.println("Start Count: " + startCount);
            System.out.println();
            System.out.println("Watching for changes in count...  Use Ctrl-C to Exit.");
            System.out.println("|Sample Number|Epoch|Count|Linear Regression Rate| Rate from Previous Sample|Rate from First Sample|");
            System.out.println("|-------------|-----|-----|----------------------|--------------------------|----------------------|");

            cnt1 = cnt0;
            t1 = t0;
        }

        @Override
        public void run() {
            try {

                LOG.info("Checking Count");

                String url = elasticSearchUrl + "/_count";

                try {
                    // index/type
                    SSLContext sslContext = SSLContext.getInstance("SSL");

                    CredentialsProvider provider = new BasicCredentialsProvider();
                    UsernamePasswordCredentials credentials
                            = new UsernamePasswordCredentials(user, userpw);
                    provider.setCredentials(AuthScope.ANY, credentials);

                    sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            System.out.println("getAcceptedIssuers =============");
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs,
                                String authType) {
                            System.out.println("checkClientTrusted =============");
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs,
                                String authType) {
                            System.out.println("checkServerTrusted =============");
                        }
                    }}, new SecureRandom());

                    CloseableHttpClient httpclient = HttpClients
                            .custom()
                            .setDefaultCredentialsProvider(provider)
                            .setSSLContext(sslContext)
                            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                            .build();

                    JSONObject json = new JSONObject();

                    HttpGet request = new HttpGet(url);
                    CloseableHttpResponse response = httpclient.execute(request);
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));

                    Header contentType = response.getEntity().getContentType();
                    String ct = contentType.getValue().split(";")[0];

                    response.getStatusLine().getStatusCode();

                    String line;
                    StringBuilder result = new StringBuilder();
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }

                    json = new JSONObject(result.toString());
                    request.abort();
                    response.close();

                    cnt0 = json.getInt("count");

                } catch (KeyManagementException | NoSuchAlgorithmException e) {

                } catch (IOException e) {
                    cnt0 = -1;
                    System.out.println("Elasticsearch call failed");
                } catch (JSONException e) {
                    cnt0 = -1;
                    System.out.println("Index may not exist");
                }

                t0 = System.currentTimeMillis();

                if (cnt0 == -1) {
                    // skip; elasticsearch or index failed to return (message about error printed)
                } else {
                    // if cnt2 == -1 then; set startCount and startTime; reset; and output header
                    if (cnt1 == -1) {
                        // Set start counts (hits this one time on startup) 
                        resetStart();
                    } else {

                        if (cnt0 > cnt1) {
                            // The count increased from last sample
                            numSamples += 1;

                            if (numSamples == 1) {
                                firstSampleCount = cnt0;
                                firstSampleTime = t0;
                            }
                            
                            // Add to Linear Regression
                            regression.addData(t0, cnt0);

                            if (numSamples >= 2) {
                                double LinearRegressionRate = regression.getSlope() * 1000;
                                double avgRateFromLast = (double) (cnt0 - cnt1) / (double) (t0 - t1) * 1000.0;
                                double avgRateFromFirst = (double) (cnt0 - firstSampleCount) / (double) (t0 - firstSampleTime) * 1000.0;
                                System.out.println("| " + numSamples + " | " + t0 + " | " + (cnt0 - startCount) + " | "
                                        + String.format("%.0f", LinearRegressionRate) + " | " + String.format("%.0f", avgRateFromLast) + " |" + String.format("%.0f", avgRateFromFirst) + " |");
                            } else {
                                System.out.println("| " + numSamples + " | " + t0 + " | " + (cnt0 - startCount) + " |             |              |              |");
                            }

                            cnt2 = cnt1;
                            t2 = t1;
                            cnt1 = cnt0;
                            t1 = t0;

                        } else if (cnt0 <= cnt1) {
                            // The count hasn't incrased from last sample or has decreased

                            if (cnt0 == cnt1) {
                                if (numSamples > 0) {
                                    System.out.println("Count is no longer increasing...");
                                    numSamples -= 1;
                                    // Remove the previous sample from regression calculation
                                    regression.removeData(t1, cnt1);

                                    System.out.println("Removing sample: " + t1 + "|" + (cnt1 - startCount));

                                    // Output Results 
                                    long totalCnt = cnt1 - startCount;  // Total count ignoring last sample
                                    double linearRegressionRate = regression.getSlope() * 1000;  // converting from ms to seconds

                                    if (numSamples >= 2) {

                                        double avgRate = (double) (cnt2 - firstSampleCount) / (double) (t2 - firstSampleTime) * 1000.0;

                                        System.out.format("Total Count: %,d | Linear Regression Rate:  %,.0f | Average Rate: %,.0f\n\n", totalCnt, linearRegressionRate, avgRate);
                                    } else {
                                        System.out.format("Total Count: %,d | Not enough samples Rate calculations. \n\n", totalCnt);
                                    }
                                    resetStart();

                                }

                            } else {
                                System.out.println("Count decreased...");
                                resetStart();
                            }

                            cnt2 = -1;
                            t2 = 0;

                        }

                    }

                }

            } catch (UnsupportedOperationException e) {
                LOG.error("ERROR", e);

            }

        }

    }

    Timer timer;
    String elasticSearchUrl;
    String indexType;
    String user;
    String userpw;
    int sampleRateSec;

    public ElasticIndexMon(String elasticSearchUrl, int sampleRateSec, String user, String userpw) {

        this.elasticSearchUrl = elasticSearchUrl;
        this.sampleRateSec = sampleRateSec;
        this.user = user;
        this.userpw = userpw;
    }

    public void run() {
        try {

            timer = new Timer();
            timer.schedule(new ElasticIndexMon.CheckCount(), 0, sampleRateSec * 1000);

        } catch (Exception e) {
            LOG.error("ERROR", e);
        }

    }

    public static void main(String[] args) {

        String elasticSearchUrl = "";
        String username = "";   // default to empty string
        String password = "";  // default to empty string
        int sampleRateSec = 10; // default to 5 seconds.  

        LOG.info("Entering application.");
        int numargs = args.length;
        if (numargs != 1 && numargs != 2 && numargs != 4) {
            System.err.print("Usage: ElasticIndexMon [ElasticsearchUrl] (sampleRateSec) ((username) (password))  \n");
            System.err.println("Example: java -cp target/rttest.jar com.esri.rttest.mon.ElasticIndexMon http://coordinator.sats-ds01.l4lb.thisdcos.directory:9200/planes/planes 20 elasic changeme");
        } else {
            elasticSearchUrl = args[0];

            if (numargs >= 2) {
                sampleRateSec = Integer.parseInt(args[1]);
            }

            if (numargs == 4) {
                username = args[2];
                password = args[3];
            }

            ElasticIndexMon t = new ElasticIndexMon(elasticSearchUrl, sampleRateSec, username, password);
            t.run();

        }

    }
}
