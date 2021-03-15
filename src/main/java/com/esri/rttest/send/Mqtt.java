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

import org.apache.commons.cli.*;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

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

            if (epochField != null ) {
                Long epochVal = null;
                // replace epoch
                if (this.epochFieldDelimiter == '.') {
                    // Starting from first label after .
                    int jsonPathIndex = 1;

                    // This will find assuming no Json Arrays in the path
                    JSONObject json = new JSONObject(line);
                    while (jsonPathIndex < epochJsonPathParts.length) {
                        //System.out.println(jsonPathIndex + jsonPathParts[jsonPathIndex]);
                        if (jsonPathIndex < epochJsonPathParts.length - 1) {
                            // Every field before the last is an object
                            json = json.getJSONObject(epochJsonPathParts[jsonPathIndex]);
                        } else {
                            // Last field convert to string
                            epochVal = json.getLong(epochJsonPathParts[jsonPathIndex]);
                            Long ts = System.currentTimeMillis();
                            if (epochVal < 1000000000000L) {
                                ts = (long) Math.round(ts / 1000.0);
                            }
                            json.put(epochJsonPathParts[jsonPathIndex], ts);
                        }
                        jsonPathIndex += 1;
                    }
                    //System.out.println(json.toString());
                    line = json.toString();

                } else {
                    // Use +1 to make the index human friendly field 1 is first field instead of field 0 is first field
                    String delim = this.epochFieldDelimiter.toString();
                    String[] lineParts = line.split(delim);
                    StringBuilder newLine = new StringBuilder();

                    for (int i = 0; i < lineParts.length; i++) {
                        if (i == this.epochFieldNumber - 1) {
                            epochVal = Long.parseLong(line.split(delim)[this.epochFieldNumber - 1]);
                            Long ts = System.currentTimeMillis();
                            if (epochVal < 1000000000000L) {
                                ts = (long) Math.round(ts / 1000.0);
                            }
                            newLine.append(ts.toString());
                        } else {
                            newLine.append(lineParts[i]);
                        }
                        if (i < lineParts.length - 1) newLine.append(delim);
                    }
                    //System.out.println(newLine);
                    line = newLine.toString();
                }
            }

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
    private String epochField = null;
    private Character epochFieldDelimiter = null;
    private Integer epochFieldNumber = null;
    private String[] epochJsonPathParts = null;

    public Mqtt() {

    }

    public void run(String host, String topic, String filename, Integer desiredRatePerSec, Long numToSend, String username,
                String password, boolean reuseFile, String clientId, Integer qos, String groupField, Integer groupRateSec, String epochField) {

        if (clientId == null) {
            UUID uuid = UUID.randomUUID();
            clientId = uuid.toString();
        }

        this.qos = 0;
        if (qos != null) {
            this.qos = qos;
        }

        this.topic = topic;

        if (epochField != null) {
            Long ts = (long) Math.round(System.currentTimeMillis() / 1000.0);

            this.epochField = epochField;
            // Parse the epochField
            char[] epochFieldChars = epochField.toCharArray();
            // Using toCharArray handled case where first char is \t (tab)

            epochFieldDelimiter = Character.valueOf(epochFieldChars[0]);

            if (epochFieldDelimiter == '.') {
                // This means input it json file
                epochJsonPathParts = epochField.split("\\.");
                epochFieldNumber = null;
            } else {
                try {
                    if (epochFieldDelimiter == 't') {
                        // Trim (remove) tab and convert to Integer
                        epochFieldDelimiter = '\t';
                        epochFieldNumber = Integer.parseInt(epochField.substring(1));
                    } else {
                        // Drop first Char and convert to Integer
                        epochFieldNumber = Integer.parseInt(epochField.substring(1));
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Failed to parse Integer from: " + epochField);
                }
                epochJsonPathParts = null;
            }
        }
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

        Mqtt app = new Mqtt();
        String appName = app.getClass().getSimpleName();

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);
        formatter.setLeftPadding(1);

        Option helpOp = Option.builder("help")
                .longOpt("help")
                .desc("display help and exit")
                .build();

        Option hostOp = Option.builder("h")
                .longOpt("host")
                .required()
                .hasArg()
                .desc("[Required] Mqtt host (e.g. tcp://mqtt.eclipse.org:1883")
                .build();

        Option topicOp = Option.builder("t")
                .longOpt("topic")
                .required()
                .hasArg()
                .desc("[Required] Mqtt Topic")
                .build();

        Option fileOp = Option.builder("f")
                .longOpt("file")
                .required()
                .hasArg()
                .desc("[Required] File with lines of text to send; if a folder is specified all files in the folder are sent one at a time alphabetically")
                .build();

        Option rateOp = Option.builder("r")
                .longOpt("rate")
                .required()
                .hasArg()
                .desc("[Required] desiredRatePerSec or groupField\n" +
                        "- desiredRatePerSec: Integer value \n" +
                        "- groupField: String value specified as delimiterFieldNumber or JsonPath (Examples):\n" +
                        "-> \",1\": comma delimited text field 1\n" +
                        "-> \"|3\": pipe delimited text field 3\n" +
                        "-> \"t2\": tab delimited text field 2\n" +
                        "-> \".ts\": json data with field at path ts\n" +
                        "-> \".prop.ts\": json data field at path prop.ts")
                .build();

        Option numToSendOp = Option.builder("n")
                .longOpt("number-to-send")
                .required()
                .hasArg()
                .desc("[Required] Number of lines to send")
                .build();

        Option usernameOp = Option.builder("u")
                .longOpt("username")
                .hasArg()
                .desc("Mqtt Server Username; default no username")
                .build();

        Option passwordOp = Option.builder("p")
                .longOpt("password")
                .hasArg()
                .desc("Mqtt Server Password; default no password")
                .build();

        Option onetimeOp = Option.builder("o")
                .longOpt("one-time")
                .desc("Send lines only one time. Stop when all lines have been sent.")
                .build();

        Option groupRateSecOp = Option.builder("g")
                .longOpt("group-rate-sec")
                .hasArg()
                .desc("Number seconds between each time a group of lines with same groupField are sent; defaults to 1")
                .build();

        Option qosOp = Option.builder("q")
                .longOpt("qos")
                .hasArg()
                .desc("Quality of Service: defaults to 2 => exactly once")
                .build();

        Option clientIdOp = Option.builder("c")
                .longOpt("client-id")
                .hasArg()
                .desc("Client ID; default to random guid")
                .build();

        Option epochFieldOp = Option.builder("e")
                .longOpt("epoch-field")
                .hasArg()
                .desc("Replace specified epoch field with current epoch; specified in same way as groupField; default no epoch-field")
                .build();

        options.addOption(helpOp);
        options.addOption(hostOp);
        options.addOption(topicOp);
        options.addOption(fileOp);
        options.addOption(rateOp);
        options.addOption(numToSendOp);
        options.addOption(usernameOp);
        options.addOption(passwordOp);
        options.addOption(onetimeOp);
        options.addOption(groupRateSecOp);
        options.addOption(qosOp);
        options.addOption(clientIdOp);
        options.addOption(epochFieldOp);


        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.err.println();
            formatter.printHelp(appName, options);
            System.exit(1);
        }

        if (cmd.hasOption("--help")) {
            System.out.println("Send lines from a file to an Mqtt Server");
            System.out.println("");
            formatter.printHelp(appName, options);
            System.exit(0);
        }

        String host = null;
        if(cmd.hasOption("h")) {
            host = cmd.getOptionValue("h");
        }
        System.out.println("host: " + host);


        String topic = null;
        if(cmd.hasOption("t")) {
            topic = cmd.getOptionValue("t");
        }
        System.out.println("topic: " + topic);


        String file = null;
        if(cmd.hasOption("f")) {
            file = cmd.getOptionValue("f");
        }
        System.out.println("file: " + file);


        Integer desiredRatePerSec = null;
        String groupField = null;
        if(cmd.hasOption("r")) {
            String rateGroupVal = cmd.getOptionValue("r");
            try {
                // If integer assume it's a desired rate
                desiredRatePerSec = Integer.parseInt(rateGroupVal);
            } catch (NumberFormatException e ) {
                // Not a number assume group Field
                groupField = rateGroupVal;
            }
        }
        System.out.println("desiredRatePerSec: " + desiredRatePerSec);
        System.out.println("groupField: " + groupField);

        Long numToSend = null;
        if(cmd.hasOption("n")) {
            try {
                numToSend = Long.parseLong(cmd.getOptionValue("n"));
            } catch (NumberFormatException e) {
                System.out.println("");
                System.out.println("Invalid value for num-to-send (n). Must be an Integer");
                System.out.println("");
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("numToSend: " + numToSend);


        String username = null;
        if(cmd.hasOption("u")) {
            username = cmd.getOptionValue("u");
        }
        System.out.println("username: " + username);


        String password = null;
        if(cmd.hasOption("p")) {
            password = cmd.getOptionValue("p");
        }
        System.out.println("password: " + password);

        boolean reuseFile = true;
        if(cmd.hasOption("o")) {
            reuseFile = false;
        }
        System.out.println("reuseFile : " + reuseFile);

        Integer groupRateSec = null;
        if(cmd.hasOption("g")) {
            try {
                groupRateSec = Integer.parseInt(cmd.getOptionValue("g"));
                if (groupRateSec < 0) {
                    throw new Exception("Must be positive");
                }
                if (groupField == null ) {
                    throw new Exception("Must also specify a groupField");
                }
            } catch (NumberFormatException ne) {
                System.out.println("");
                System.out.println("Invalid value for group-rate-sec (g).  Must be an Integer");
                System.out.println("");
                formatter.printHelp(appName, options);
                System.exit(1);
            } catch (Exception e ) {
                System.out.println("");
                System.out.println("Invalid value for group-rate-sec (g). " + e.getMessage());
                System.out.println("");
                formatter.printHelp(appName, options);
                System.exit(1);
            }

        }
        if (groupField != null) {
            // default to 1
            groupRateSec = 1;
        }
        System.out.println("groupRateSec: " + groupRateSec);

        Integer qos = null;
        if(cmd.hasOption("g")) {
            try {
                qos = Integer.parseInt(cmd.getOptionValue("q"));
                if (qos < 0 || qos > 3) {
                    throw new Exception("Must be 0, 1 or 2");
                }
            } catch (NumberFormatException ne) {
                System.out.println("");
                System.out.println("Invalid value for qos (q).  Must be an Integer");
                System.out.println("");
                formatter.printHelp(appName, options);
                System.exit(1);
            } catch (Exception e) {
                System.out.println("");
                System.out.println("Invalid value for qos (q) " + e.getMessage());
                System.out.println("");
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        if (qos == null) {
            qos = 2;
        }
        System.out.println("qos: " + qos);


        String clientId = null;
        if(cmd.hasOption("c")) {
            clientId = cmd.getOptionValue("c");
        }
        System.out.println("clientId : " + clientId);

        String epochField = null;
        if(cmd.hasOption("e")) {
            epochField = cmd.getOptionValue("e");
        }
        System.out.println("epochField : " + epochField);

        app.run(host, topic, file, desiredRatePerSec, numToSend, username, password, reuseFile, clientId, qos, groupField, groupRateSec, epochField);
    }

}
