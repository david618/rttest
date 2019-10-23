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
package com.esri.rttest.send;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.LinkedBlockingQueue;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author david
 */
public class HttpThread extends Thread {

    private static final Logger LOG = LogManager.getLogger(HttpThread.class);


    LinkedBlockingQueue<String> lbq;
    private boolean running = true;

    public boolean isRunning() {
        return running;
    }

    private final String url;
    // Tell the server I'm Firefox
    private final String USER_AGENT = "Mozilla/5.0";

    private CloseableHttpClient httpClient;

    private HttpPost httpPost;

    SSLContext sslContext;

    private long lastUpdate;
    private long cntErr;
    private long cnt;

    public long getCntErr() {
        return cntErr;
    }

    public long getCnt() {
        return cnt;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    HttpThread(LinkedBlockingQueue<String> lbq, String url, String contentType) throws Exception {
        this.lbq = lbq;
        this.url = url;


        sslContext = SSLContext.getInstance("SSL");


        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                //System.out.println("getAcceptedIssuers =============");
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs,
                                           String authType) {
                //System.out.println("checkClientTrusted =============");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs,
                                           String authType) {
                //System.out.println("checkServerTrusted =============");
            }
        }}, new SecureRandom());


        httpClient = HttpClients
                .custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();

        httpPost = new HttpPost(url);
        httpPost.setHeader("Content-type", contentType);

        running = true;

        cntErr = 0;
    }

    public void terminate() {
        running = false;
    }

    @Override
    public void run() {
        try {
            while (running) {
                String line = lbq.take();
                if (line == null) {
                    break;
                }
                StringEntity postingString = new StringEntity(line);


                httpPost.setEntity(postingString);


                HttpResponse resp = httpClient.execute(httpPost);

                if (resp.getStatusLine().getStatusCode() > 299 || resp.getStatusLine().getStatusCode() < 200) {
                    cntErr += 1;
                }

                httpPost.releaseConnection();

                cnt += 1;
                lastUpdate = System.currentTimeMillis();


            }

        } catch (Exception e) {
            LOG.debug("ERROR", e);
            System.err.println(e.getClass() + ": " + e.getMessage());
            terminate();

        }
    }
}
