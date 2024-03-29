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

package com.esri.rttest;

import com.esri.rttest.mon.TcpSinkServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author david
 */
public class MarathonInfo {
    
    private static final Logger LOG = LogManager.getLogger(TcpSinkServer.class);
    

    /**
     *
     * @param kafkaName
     * @return comma separated list of brokers
     *
     * Uses the Marathon rest api to get the brokers for the specified KafkaName
     *
     * Assumes you have mesos dns installed and configured. So you should be
     * able to ping marathon.mesos and <hub-name>.marathon.mesos from the server
     * you run this on
     *
     */
    public String getBrokers(String kafkaName) {
        String brokers = "";
        try {

            // Since no port was specified assume this is a hub name
            String url = "http://marathon.mesos:8080/v2/apps/" + kafkaName;
            System.out.println(url);

            // Support for https
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

            HttpGet request = new HttpGet(url);

            HttpResponse response = httpclient.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            //System.out.println(result);
            JSONObject json = new JSONObject(result.toString());
            JSONObject app = json.getJSONObject("app");
            JSONArray tasks = app.getJSONArray("tasks");
            JSONObject task = tasks.getJSONObject(0);
            JSONArray ports = task.getJSONArray("ports");

            int k = 0;
            brokers = "";

            while (k < ports.length() && brokers.isEmpty()) {
                try {

                    Integer port = ports.getInt(k);
                    //System.out.println(port);

                    k++;

                    // Now get brokers from service
                    //url = "http://" + kafkaName + ".marathon.mesos:" + String.valueOf(port) + "/v1/connection";
                    url = "http://" + kafkaName + ".marathon.mesos:" + String.valueOf(port) + "/v1/endpoints/broker";
                    
                    System.out.println(url);
                    request = new HttpGet(url);

                    response = httpclient.execute(request);
                    rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));

                    result = new StringBuffer();
                    line = "";
                    while ((line = rd.readLine()) != null) {
                        result.append(line);
                    }

                    //System.out.println(result);
                    json = new JSONObject(result.toString());

                    JSONArray addresses = json.getJSONArray("address");

                    for (int i = 0; i < addresses.length(); i++) {
                        if (i > 0) {
                            brokers += ",";
                        }
                        brokers += addresses.getString(i);
                    }

                } catch (IOException | UnsupportedOperationException | JSONException e) {
                    brokers = "";
                }

            }

        } catch (IOException | UnsupportedOperationException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | JSONException e) {

            brokers = "Could not find brokers.";
        }

        System.out.println("brokers: " + brokers);
        return brokers;
    }

    public String getElasticSearchTransportAddresses(String esFrameworkName) {
        // Get the Transport Addresses for given Elasticsearch Framework Name (e.g. elasticsearch by default)
        String addresses = "";

        try {
            // Since no port was specified assume this is service name
            //String url = "http://leader.mesos/service/" + esFrameworkName + "/v1/tasks";
            String url = "http://marathon.mesos:8080/v2/apps/" + esFrameworkName;
            //System.out.println(url);

            // Support for https
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

            HttpGet request = new HttpGet(url);

            HttpResponse response = httpclient.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            //System.out.println(result);
            JSONObject json = new JSONObject(result.toString());
            JSONObject app = json.getJSONObject("app");
            JSONArray tasks = app.getJSONArray("tasks");
            JSONObject task = tasks.getJSONObject(0);
            JSONArray ports = task.getJSONArray("ports");

            int port = ports.getInt(0);
            String eip = task.getString("host");

            url = "http://" + eip + ":" + port + "/v1/tasks";

            request = new HttpGet(url);

            response = httpclient.execute(request);
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            JSONArray jsonArray = new JSONArray(result.toString());

            int i = 0;
            while (i < jsonArray.length()) {
                JSONObject item = jsonArray.getJSONObject(i);
                String ta = item.getString("transport_address");
                //System.out.println(ta);
                if (i > 0) {
                    addresses += ",";
                }
                addresses += ta;
                i++;
            }

        } catch (IOException | UnsupportedOperationException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | JSONException e) {
            addresses = "Could not find elasticsearch transports.";
        }

        System.out.println("elastic transports: " + addresses);
        return addresses;
    }

    public String getElasticSearchHttpAddresses(String esAppName) {
        // Get the Http Addresses for given Elasticsearch Framework Name (e.g. elasticsearch by default)

        String addresses = "";

        try {
            // Since no port was specified assume this is a hub name
            //String url = "http://leader.mesos/service/" + esAppName + "/v1/tasks";
            String url = "http://marathon.mesos:8080/v2/apps/" + esAppName;
            //System.out.println(url);

            // Support for https
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

            HttpGet request = new HttpGet(url);

            HttpResponse response = httpclient.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            //System.out.println(result);
            JSONObject json = new JSONObject(result.toString());
            JSONObject app = json.getJSONObject("app");
            JSONArray tasks = app.getJSONArray("tasks");
            JSONObject task = tasks.getJSONObject(0);
            JSONArray ports = task.getJSONArray("ports");

            int port = ports.getInt(0);
            String eip = task.getString("host");

            url = "http://" + eip + ":" + port + "/v1/tasks";;

            request = new HttpGet(url);

            response = httpclient.execute(request);
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            JSONArray jsonArray = new JSONArray(result.toString());

            int i = 0;
            while (i < jsonArray.length()) {
                JSONObject item = jsonArray.getJSONObject(i);
                String ta = item.getString("http_address");
                //System.out.println(ta);
                if (i > 0) {
                    addresses += ",";
                }
                addresses += ta;
                i++;
            }

        } catch (IOException | UnsupportedOperationException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | JSONException e) {
            addresses = "Could not find elasticsearch web addresses.";
        }

        System.out.println("elastic web: " + addresses);
        return addresses;
    }

    public String getElasticSearchClusterName(String esAppName) {
        // Get the Cluster Name for given Elasticsearch Framework Name (e.g. elasticsearch by default)

        String clusterName = "";

        try {
            // Since no port was specified assume this is a hub name
            //String url = "http://leader.mesos/service/" + esAppName + "/v1/cluster";
            String url = "http://marathon.mesos:8080/v2/apps/" + esAppName;
            //System.out.println(url);

            // Support for https
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

            HttpGet request = new HttpGet(url);

            HttpResponse response = httpclient.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            //System.out.println(result);
            JSONObject json = new JSONObject(result.toString());
            JSONObject app = json.getJSONObject("app");
            JSONArray tasks = app.getJSONArray("tasks");
            JSONObject task = tasks.getJSONObject(0);
            JSONArray ports = task.getJSONArray("ports");

            int port = ports.getInt(0);
            String eip = task.getString("host");

            url = "http://" + eip + ":" + port + "/v1/cluster";

            request = new HttpGet(url);

            response = httpclient.execute(request);
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            json = new JSONObject(result.toString());
            JSONObject config = json.getJSONObject("configuration");
            clusterName = config.getString("ElasticsearchClusterName");

        } catch (IOException | UnsupportedOperationException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | JSONException e) {
            clusterName = "Could not find elasticsearch cluster name.";
        }

        System.out.println("elastic cluster name: " + clusterName);
        return clusterName;

    }

    public String getElastic5Info(String esAppName, String username, String password) {


        JSONObject returnJson = new JSONObject();

        try {
            // Get the application endoint 
            String url = "http://marathon.mesos:8080/v2/apps/" + esAppName;

            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

            HttpGet request = new HttpGet(url);

            HttpResponse response = httpclient.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            //System.out.println(result);
            JSONObject json = new JSONObject(result.toString());
            JSONObject app = json.getJSONObject("app");
            JSONArray tasks = app.getJSONArray("tasks");
            JSONObject task = tasks.getJSONObject(0);
            JSONArray ports = task.getJSONArray("ports");

            int port = ports.getInt(0);
            String eip = task.getString("host");

            returnJson.put("satApp", eip + ":" + port);

            url = "http://" + eip + ":" + port + "/v1/endpoints/data";

            request = new HttpGet(url);

            response = httpclient.execute(request);
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            result = new StringBuffer();
            line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            json = new JSONObject(result.toString());
            String address = json.getJSONArray("address").getString(0);
            String vip = json.getString("vip");

            returnJson.put("address", address);
            returnJson.put("vip", vip);

            url = "http://" + address;

            SSLContext sslContext = SSLContext.getInstance("SSL");

            CredentialsProvider provider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials
                    = new UsernamePasswordCredentials(username, password);
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

            httpclient = HttpClients
                    .custom()
                    .setDefaultCredentialsProvider(provider)
                    .setSSLContext(sslContext)
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .build();

            request = new HttpGet(url);
            response = httpclient.execute(request);
            rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

//            Header contentType = response.getEntity().getContentType();
//            String ct = contentType.getValue().split(";")[0];
//
//            int responseCode = response.getStatusLine().getStatusCode();

            result = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            json = new JSONObject(result.toString());

            String clusterName = json.getString("cluster_name");

            returnJson.put("cluster_name", clusterName);

            // Get Endpoints 172.17.2.5:17962/v1/endpoints/data  (vip name)
            // Use address to get Cluster Name
        } catch (IOException | UnsupportedOperationException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | JSONException e) {
            returnJson.put("error", e.getMessage());
        }

        System.out.println(returnJson);
        
        return returnJson.toString();

    }
    
    public ArrayList<IPPort> getIPPorts(String appName, int portIndex) {
        
        
        ArrayList<IPPort> ipPorts = new ArrayList<>();
       
        try {
            // Get the application endoint 
            String url = "http://marathon.mesos:8080/v2/apps/" + appName;

            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(
                    sslsf).build();

            HttpGet request = new HttpGet(url);

            HttpResponse response = httpclient.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            //System.out.println(result);
            JSONObject json = new JSONObject(result.toString());
            JSONObject app = json.getJSONObject("app");
            JSONArray tasks = app.getJSONArray("tasks");
            
            int i = 0;
                while (i < tasks.length()) {
                JSONObject task = tasks.getJSONObject(i);
                String eip = task.getString("host");
                
                JSONArray ports = task.getJSONArray("ports");
                int port = -1;
                try {
                    port = ports.getInt(portIndex);
                } catch (JSONException e) {
                    // ok to ignore
                }
                
                
                IPPort ipport = new IPPort(eip, port);
                                
                ipPorts.add(ipport);
                
                i++;
                
                //System.out.println(ipport);
                
            }
            


            // Get Endpoints 172.17.2.5:17962/v1/endpoints/data  (vip name)
            // Use address to get Cluster Name
        } catch (IOException | UnsupportedOperationException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | JSONException e) {
            LOG.error("ERROR",e);
        }

                
        
        return ipPorts;
    }

    

    public static void main(String[] args) {

        int numargs = args.length;

        if (numargs != 2 && numargs != 4) {
            System.err.print("Usage: MarathonInfo <kafka|elastic|other> <framework-name<:port index>> (<username> <password>)\n");
        } else {

            MarathonInfo t = new MarathonInfo();

            String typ = args[0];

            if (typ.equalsIgnoreCase("other")) {
                String appParts[] = args[1].split(":");
                String appName = appParts[0];
                int portIndex = 0;
                if (appParts.length > 1) {
                    portIndex = Integer.parseInt(appParts[1]);
                }
                
                ArrayList<IPPort> ipports = t.getIPPorts(appName, portIndex);
                for (IPPort ipport: ipports) {
                    System.out.println(ipport);
                }
            } else if (typ.equalsIgnoreCase("kafka")) {
                t.getBrokers(args[1]);
            } else if (typ.equalsIgnoreCase("elastic")) {
                t.getElasticSearchClusterName(args[1]);
                t.getElasticSearchHttpAddresses(args[1]);
                t.getElasticSearchTransportAddresses(args[1]);
            } else if (typ.equalsIgnoreCase("elastic5")) {
                if (numargs == 2) {
                    t.getElastic5Info(args[1], "elastic", "changeme");
                } else {
                    t.getElastic5Info(args[1], args[2], args[3]);
                }

            } else {
                System.err.println("First parameter should be <kafka|elastic>");
                System.err.print("Usage: MarathonInfo <kafka|elastic> <framework-name>\n");
            }

        }

    }

}
