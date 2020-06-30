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

    public Mqtt(String host, String topic, String filename, Integer desiredRatePerSec, Long numToSend, String username, String password, boolean reuseFile, String clientId, Integer qos, String groupField, Integer groupRateSec) {

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
            
            this.mqttClient = new MqttClient(host, clientId, null);
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
        this.groupField = groupField;
        this.groupRateSec = groupRateSec;

        sendFiles();

    }

    public static void main(String[] args) {
        int numargs = args.length;
        if (numargs < 5) {
            System.err.println("Usage: Mqqt [host] [topic] [file] [desiredRatePerSec|groupField] [numToSend] (username=null) (password=null) (reuseFile=true) (groupRateSec) (qos=0) (clientId=randomGuid) \n");
            System.err.println("java -cp target/rttest.jar com.esri.rttest.send.Mqtt tcp://52.191.131.159:1883 test /Users/davi5017/Downloads/safegraph_time_grouped.txt 100 10000 username password");
            System.err.println("");
            System.err.println("host: mqtt host (e.g. tcp://mqtt.eclipse.org:1883");
            System.err.println("topic: Mqtt Topic");
            System.err.println("file: file with lines of text to send to Elasticsearch; if folder then all files in the folder are sent one at a time alphabetically");
            System.err.println("desiredRatePerSec|groupField: Use Number to specify a desiredRatePerSec|groupField start with delimiter followed by field number or period with json path to field");
            System.err.println("  groupField examples: ");
            System.err.println("    \",1\": comma delimited text field 1");
            System.err.println("    \"|3\": pipe delimited text field 3 ");
            System.err.println("    \".ts\": json data with field at path ts");
            System.err.println("    \".properties.ts\": json data with field at path properties.ts");
            System.err.println("numToSend: Number of lines to send");
            System.err.println("username");
            System.err.println("password");            
            System.err.println("resueFile: true or false; if true the file is reused as needed to if numToSend is greater than number of lines in the file");
            System.err.println("groupRateSec: Send a group every groupRateSec seconds: default to 1");
            System.err.println("qos (Quality of Service): defaults to 2 exactly once");
            System.err.println("clientId: defaults to Random GUID");
            
            System.err.println("");
        } else {

            Integer desiredRatePerSec = -1;
            
            String clientId = null;
            Integer qos = null;
            String username = null;
            String password = null;
            String groupField = null;
            Integer groupRateSec = null;
            boolean reuseFile = true;
            
            String host = args[0];
            String topic = args[1];
            String file = args[2];
            
            try {
                // If integer assume it's a desired rate
                desiredRatePerSec = Integer.parseInt(args[3]);
            } catch (NumberFormatException e ) {
                // Not a number assume group Field
                groupField = args[3];
            }

            Long numToSend = Long.parseLong(args[4]);

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
                groupRateSec = Integer.parseInt(args[8]);
            }
            
            if (numargs > 9) {
                qos = Integer.parseInt(args[9]);
            }
            
            if (numargs > 10) {
                clientId = args[10];
            }
                                   
            System.out.println("host: " + host);
            System.out.println("topic: " + topic);
            System.out.println("file: " + file);
            System.out.println("desiredRatePerSec: " + desiredRatePerSec);
            System.out.println("groupField: " + groupField);
            System.out.println("numToSend: " + numToSend);
            System.out.println("username: " + username);
            System.out.println("password: " + password);
            System.out.println("reuseFile: " + reuseFile);
            System.out.println("groupRateSec: " + groupRateSec);
            System.out.println("qos: " + qos);
            System.out.println("clientId: " + clientId);
            System.out.println();
            

            Mqtt t = new Mqtt(host, topic, file, desiredRatePerSec, numToSend, username, password, reuseFile, clientId, qos, groupField, groupRateSec);

        }
    }

}
