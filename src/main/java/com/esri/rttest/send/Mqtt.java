/*
 * (C) Copyright 2020 David Jennings
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

 * Creator: David Jennings
 */
package com.esri.rttest.send;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 *
 * @author davi5017
 */
public class Mqtt extends Send {

    @Override
    public long sendBatch(ArrayList<String> batchLines) {
        Iterator<String> linesIterator = batchLines.iterator();

        long cnt = 0;

        while (linesIterator.hasNext()) {
            String line = linesIterator.next();
            MqttMessage message = new MqttMessage(line.getBytes());
            message.setQos(this.qos);
            try {
                this.mqttClient.publish(this.topic, message);
                cnt += 1;
            } catch (MqttException me) {
                System.out.println("reason " + me.getReasonCode());
                System.out.println("msg " + me.getMessage());
                System.out.println("loc " + me.getLocalizedMessage());
                System.out.println("cause " + me.getCause());
                System.out.println("excep " + me);
                me.printStackTrace();
            }
        }
        return cnt;

    }

    @Override
    public void sendDone() {
        try {
            this.mqttClient.disconnect();
        } catch (MqttException me) {
            // Ok to ignore
        }
    }

    class TrustEveryoneManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    TrustManager[] myTrustManagerArray = new TrustManager[]{new TrustEveryoneManager()};
        

    MqttClient mqttClient;
    String topic;
    Integer qos;

    public Mqtt(String host, String topic, String filename, Integer desiredRatePerSec, Long numToSend, String username, String password, boolean reuseFile, String clientId, Integer qos) {

        if (clientId == null) {
            UUID uuid = UUID.randomUUID();
            clientId = uuid.toString();
        }

        this.qos = 0;
        if (qos != null) {
            this.qos = qos;
        }

        this.topic = topic;

        try {
            
            this.mqttClient = new MqttClient(host, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            if (username != null) {
                connOpts.setUserName(username);
            }
            if (password != null) {
                connOpts.setPassword(password.toCharArray());
            }

            String proto = host.split(":")[0];
            if ( proto.equalsIgnoreCase("ssl") ) {
                System.out.println("SSL");
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, myTrustManagerArray, new java.security.SecureRandom());            
                connOpts.setSocketFactory(sc.getSocketFactory());            
                connOpts.setHttpsHostnameVerificationEnabled(false);
            }
            
            this.mqttClient.connect(connOpts);
            System.out.println("Connected");

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
            System.exit(1);
        } catch (KeyManagementException e) {
            System.out.println("KeyManagementException");
            System.err.println(e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException");
            System.err.println(e.getMessage());
        }

        // Part of Abstract Class Send
        this.desiredRatePerSec = desiredRatePerSec;
        this.numToSend = numToSend;
        this.filename = filename;
        this.reuseFile = reuseFile;

        sendFiles();

    }

    public static void main(String[] args) {
        int numargs = args.length;
        if (numargs < 5) {
            System.err.println("Usage: Mqqt [host] [topic] [file] [desiredRatePerSec] [numToSend] (username=null) (password=null) (reuseFile=true) (clientId=randomGuid) (qos=0) \n");
            System.err.println("java -cp target/rttest.jar com.esri.rttest.send.Mqtt tcp://52.191.131.159:1883 test /Users/davi5017/Downloads/safegraph_time_grouped.txt 10000 1000000 username password");
            System.err.println("");
            System.err.println("hostr: mqtt host (e.g. tcp://mqtt.eclipse.org:1883");
            System.err.println("topic: Mqtt Topic");
            System.err.println("file: file with lines of text to send to Elasticsearch; if folder then all files in the folder are sent one at a time alphabetically");
            System.err.println("desiredRatePerSec: Desired Rate. The tool will try to send at this rate if possible");
            System.err.println("numToSend: Number of lines to send");
            System.err.println("resueFile: true or false; if true the file is reused as needed to if numToSend is greater than number of lines in the file");
            System.err.println("clientId: defaults to Random GUID");
            System.err.println("qos (Quality of Service): defaults to 2 exactly once");
            System.err.println("");
        } else {

            String host = args[0];
            String topic = args[1];
            String file = args[2];
            Integer desiredRatePerSec = Integer.parseInt(args[3]);
            Long numToSend = Long.parseLong(args[4]);
            String clientId = null;
            Integer qos = null;
            String username = null;
            String password = null;

            boolean reuseFile = true;

            if (numargs > 5) {
                username = args[5];
            }

            if (numargs > 6) {
                password = args[6];
            }

            if (numargs > 7) {
                reuseFile = Boolean.parseBoolean(args[7]);
            }

            if (numargs > 8) {
                clientId = args[8];
            }

            if (numargs > 9) {
                qos = Integer.parseInt(args[9]);
            }

            System.out.println("host: " + host);
            System.out.println("topic: " + topic);
            System.out.println("file: " + file);
            System.out.println("desiredRatePerSec: " + desiredRatePerSec);
            System.out.println("numToSend: " + numToSend);
            System.out.println("clientId: " + clientId);
            System.out.println("qos: " + qos);
            System.out.println("username: " + username);
            System.out.println("password: " + password);
            System.out.println("reuseFile: " + reuseFile);

            Mqtt t = new Mqtt(host, topic, file, desiredRatePerSec, numToSend, username, password, reuseFile, clientId, qos);

        }
    }

}
