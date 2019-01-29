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
import java.util.HashMap;
import java.util.Map;
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

        long cnt1 = 0;
        long cnt2 = -1;
        long startCount = 0;
        long endCount = 0;
        int numSamples = 0;
        HashMap<Long, Long> samples;
        long t1 = 0L;
        long t2 = 0L;
        SimpleRegression regression;

        public CheckCount() {
            regression = new SimpleRegression();
            cnt1 = 0;
            cnt2 = -1;
            startCount = 0;
            numSamples = 0;
            t1 = 0L;
            t2 = 0L;
            samples = null;
        }

        boolean inCounting() {
            if (cnt1 > 0) {
                return true;
            } else {
                return false;
            }
        }

        HashMap<Long, Long> getSamples() {
            return samples;
        }

        long getStartCount() {
            return startCount;
        }

        long getEndCount() {
            return endCount;
        }

        @Override
        public void run() {
            try {

                LOG.info("Checking Count");

                // index/type
                String url = elasticSearchUrl + "/_count";
                SSLContext sslContext = SSLContext.getInstance("SSL");

                CredentialsProvider provider = new BasicCredentialsProvider();
                UsernamePasswordCredentials credentials
                        = new UsernamePasswordCredentials(user, userpw);
                provider.setCredentials(AuthScope.ANY, credentials);

                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        if (sendStdout) {
                            System.out.println("getAcceptedIssuers =============");
                        }
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs,
                            String authType) {
                        if (sendStdout) {
                            System.out.println("checkClientTrusted =============");
                        }
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs,
                            String authType) {
                        if (sendStdout) {
                            System.out.println("checkServerTrusted =============");
                        }
                    }
                }}, new SecureRandom());

                CloseableHttpClient httpclient = HttpClients
                        .custom()
                        .setDefaultCredentialsProvider(provider)
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build();

                HttpGet request = new HttpGet(url);
                CloseableHttpResponse response = httpclient.execute(request);
                BufferedReader rd = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));

                Header contentType = response.getEntity().getContentType();
                String ct = contentType.getValue().split(";")[0];

                int responseCode = response.getStatusLine().getStatusCode();

                String line;
                StringBuilder result = new StringBuilder();
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

                JSONObject json = new JSONObject(result.toString());
                request.abort();
                response.close();

                cnt1 = json.getInt("count");

                t1 = System.currentTimeMillis();
                
                if (cnt2 == -1) {
                    System.out.println("Watching for changes in count...  Use Ctrl-C to Exit.");
                    System.out.println("|Sample Number|Epoch|Count|Linear Regression Rate|Approx. Instantaneous Rate|");
                    System.out.println("|-------------|-----|-----|----------------------|--------------------------|");
                }                 

                if (cnt2 == -1 || cnt1 < cnt2) {
                    cnt2 = cnt1;
                    startCount = cnt1;
                    endCount = cnt1;
                    regression = new SimpleRegression();
                    samples = new HashMap<>();
                    numSamples = 0;

                } else if (cnt1 > cnt2) {
                    // Increase number of samples
                    numSamples += 1;

                    // Add to Linear Regression
                    regression.addData(t1, cnt1);
                    samples.put(t1, cnt1);

                    if (numSamples >= 2) {
                        double regRate = regression.getSlope() * 1000;
                        double iRate = (double) (cnt1 - cnt2) / (double) (t1 - t2) * 1000.0;
                        if (sendStdout) {
                            System.out.println("| " + numSamples + " | " + t1 + " | " + (cnt1 - startCount) + " | " + String.format("%.0f", regRate) + " | " + String.format("%.0f", iRate) + " |");
                        }
                    } else {
                        System.out.println("| " + numSamples + " | " + t1 + " | " + (cnt1 - startCount) + " |           |           |");
                    }

                } else if (cnt1 == cnt2 && numSamples > 0) {
                    
                    System.out.println("Count is no longer increasing...");
                    
                    endCount = cnt1;
                                        
                    numSamples -= 1;
                    // Remove the last sample
                    regression.removeData(t2, cnt2);
                    samples.remove(t2, cnt2);
                    
                    // Calculate Average Rate
                    long minTime = Long.MAX_VALUE;
                    long maxTime = Long.MIN_VALUE;
                    long minCount = Long.MAX_VALUE;
                    long maxCount = Long.MIN_VALUE;
                    for (Map.Entry pair : samples.entrySet()) {
                        long time = (long) pair.getKey();
                        long count = (long) pair.getValue();
                        if (time < minTime) {
                            minTime = time;
                        }
                        if (time > maxTime) {
                            maxTime = time;
                        }
                        if (count < minCount) {
                            minCount = count;
                        }
                        if (count > maxCount) {
                            maxCount = count;
                        }
                    }
                    double avgRate = (double) (maxCount - minCount) / (double) (maxTime - minTime) * 1000.0;                    
                    
                    if (sendStdout) {
                        System.out.println("Removing sample: " + t2 + "|" + (cnt2 - startCount));
                    }
                    
                    // Output Results
                    long cnt = cnt2 - startCount;
                    double rcvRate = regression.getSlope() * 1000;  // converting from ms to seconds

                    double regRate = regression.getSlope() * 1000;  // converting from ms to seconds

                    if (numSamples >= 2) {
                        if (sendStdout) {
                            System.out.format("Total Count: %,d | Linear Regression Rate:  %,.0f | Average Rate: %,.0f\n\n", cnt, regRate, avgRate);
                        }
                    } else {
                        if (sendStdout) {
                            System.out.format("Total Count: %,d | Not enough samples Rate calculations. \n\n", cnt);
                        }
                    }

                    // Reset 
                    cnt1 = -1;
                    cnt2 = -1;
                    t1 = 0L;
                    t2 = 0L;

                }

                cnt2 = cnt1;
                t2 = t1;

            } catch (IOException | UnsupportedOperationException | KeyManagementException | NoSuchAlgorithmException | JSONException e) {
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
    boolean sendStdout;

    public ElasticIndexMon(String elasticSearchUrl, int sampleRateSec, String user, String userpw, boolean sendStdout) {

//        esServer = "ags:9220";
//        index = "FAA-Stream/FAA-Stream";
//        user = "els_ynrqqnh";
//        userpw = "8jychjwcgn";
        this.elasticSearchUrl = elasticSearchUrl;
        this.sampleRateSec = sampleRateSec;
        this.user = user;
        this.userpw = userpw;
        this.sendStdout = sendStdout;
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
        int sampleRateSec = 5; // default to 5 seconds.  
        Boolean sendStdout = true;

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

            ElasticIndexMon t = new ElasticIndexMon(elasticSearchUrl, sampleRateSec, username, password, sendStdout);
            t.run();

        }

    }
}
