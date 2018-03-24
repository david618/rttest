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
 * Monitors an Feature/Layer
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
import java.util.Timer;
import java.util.TimerTask;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
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
public class FeatureLayerMon {

    private static final Logger LOG = LogManager.getLogger(FeatureLayerMon.class);

    class CheckCount extends TimerTask {

        long cnt1;
        long cnt2;
        long startCount;
        long endCount; 
        int numSamples;
        HashMap<Long, Long> samples;
        long t1;
        long t2;
        SimpleRegression regression;

        public CheckCount() {
            regression = new SimpleRegression();
            cnt1 = 0;
            cnt2 = -1;
            startCount = 0;
            endCount = 0;
            numSamples = 0;
            t1 = 0L;
            t2 = 0L;
            samples = null;

        }

        boolean inCounting() {
            if (cnt1 > 0) 
                return true;
            else 
                return false;
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
                
                String url = featureLayerURL + "/query?where=1%3D1&returnCountOnly=true&f=json";
                SSLContext sslContext = SSLContext.getInstance("SSL");

                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        if (sendStdout) System.out.println("getAcceptedIssuers =============");
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs,
                            String authType) {
                        if (sendStdout) System.out.println("checkClientTrusted =============");
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs,
                            String authType) {
                        if (sendStdout) System.out.println("checkServerTrusted =============");
                    }
                }}, new SecureRandom());

                CloseableHttpClient httpclient = HttpClients
                        .custom()
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

                //System.out.println(result);
                JSONObject json = new JSONObject(result.toString());
                request.abort();
                response.close();

                cnt1 = json.getInt("count");
                t1 = System.currentTimeMillis();

                if (cnt2 == -1 || cnt1 < cnt2) {
                    // If first count or count has gone down
                    cnt2 = cnt1;
                    startCount = cnt1;
                    endCount = cnt1;                    
                    regression = new SimpleRegression();
                    samples = new HashMap<>();
                    numSamples = 0;

                } else if (cnt1 > cnt2) {
                    // Add to Linear Regression
                    regression.addData(t1, cnt1);

                    samples.put(t1, cnt1);
                    // Increase number of samples
                    numSamples += 1;
                    if (numSamples > 2) {
                        double rcvRate = regression.getSlope() * 1000;
                        if (sendStdout) System.out.format("%d,%d,%d,%.0f\n", numSamples, t1, cnt1, rcvRate);
                    } else {
                        //System.out.println(numSamples + "," + t1 + "," + cnt1);
                        if (sendStdout) System.out.format("%d,%d,%d\n", numSamples, t1, cnt1);
                    }

                } else if (cnt1 == cnt2 && numSamples > 0) {
                    endCount = cnt1;
                    
                    numSamples -= 1;
                    // Remove the last sample
                    regression.removeData(t2, cnt2);
                    samples.remove(t2, cnt2);
                    if (sendStdout) System.out.println("Removing: " + t2 + "," + cnt2);
                    // Output Results
                    long cnt = cnt2 - startCount;
                    double rcvRate = regression.getSlope() * 1000;  // converting from ms to seconds

                    if (numSamples > 5) {
                        double rateStdErr = regression.getSlopeStdErr();
                        if (sendStdout) System.out.format("%d , %.2f, %.4f\n", cnt, rcvRate, rateStdErr);
                    } else if (numSamples >= 2) {
                        if (sendStdout) System.out.format("%d , %.2f\n", cnt, rcvRate);
                    } else {
                        if (sendStdout) System.out.println("Not enough samples to calculate rate. ");
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
    String featureLayerURL;
    int sampleRateSec;
    boolean sendStdout; 

    public FeatureLayerMon(String featureLayerURL, int sampleRateSec, boolean sendStdout) {
        this.featureLayerURL = featureLayerURL;
        this.sampleRateSec = sampleRateSec;
        this.sendStdout = sendStdout;

    }
    
    public void run() {
        try {

            timer = new Timer();
            timer.schedule(new FeatureLayerMon.CheckCount(), 0, sampleRateSec * 1000);

        } catch (Exception e) {
            LOG.error("ERROR", e);
        }

    }     

    public static void main(String[] args) {

        String url = "";
        int sampleRateSec = 5; // default to 5 seconds.    
        Boolean sendStdout = true;
        
        
        LOG.info("Entering application.");
        int numargs = args.length;
        if (numargs != 1 && numargs != 2) {
            System.err.print("Usage: FeatureLayerMon [Feature-Layer] (sampleRateSec) \n");
            System.err.println("Example: java -cp target/rttest.jar com.esri.rttest.mon.FeatureLayerMon http://p1/2b2ed39f-7656-463b-9df9-e7ce0d04ecbe/arcgis/rest/services/planes-bat/FeatureServer/0 30");          
            
            
        } else {
            url = args[0];
            if (numargs == 2) {
                sampleRateSec = Integer.parseInt(args[1]);
            }
            FeatureLayerMon t = new FeatureLayerMon(url, sampleRateSec, sendStdout);
            t.run();
        }   

    }
}
