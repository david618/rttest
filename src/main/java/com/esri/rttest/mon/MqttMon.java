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
 * 
 * Creator: David Jennings
 */

package com.esri.rttest.mon;

import org.apache.commons.cli.*;

/**
 *
 * @author davi5017
 */
public class MqttMon extends Monitor {

    @Override
    public Sample getSample() {
        long cnt ;
        long ts;

        cnt = mqttSink.getCnt();
        ts = System.currentTimeMillis();


        return new Sample(cnt, ts);    }

    @Override
    public void countEnded() {

    }
    
    public MqttMon() {}
            

    public MqttMon(String host, String topic, String username, String password, Integer sampleRateSec, Integer numSampleEqualBeforeExit, boolean printMessages) {
        this.host = host;
        this.topic = topic;
        this.username = username;
        this.password = password;
        this.sampleRateSec = sampleRateSec;
        this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;

        this.mqttSink = new MqttSink(host, topic, username, password, printMessages);

        run();
        
        
    }

    String host;
    String topic;
    String username;
    String password;
    MqttSink mqttSink;
    

    public static void main(String[] args) {

        MqttMon app = new MqttMon();
        String appName = app.getClass().getSimpleName();

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);
        formatter.setLeftPadding(1);

        Option helpOp = Option.builder()
                .longOpt("help")
                .desc("display help and exit")
                .build();

        Option brokersOp = Option.builder("h")
                .longOpt("host")
                .required()
                .hasArg()
                .desc("[Required] Mqtt Host (e.g. tcp://52.191.131.159:1883)")
                .build();

        Option topicOp = Option.builder("t")
                .longOpt("topic")
                .required()
                .hasArg()
                .desc("[Required] Kafka Topic ")
                .build();

        Option sampleRateSecOp = Option.builder("r")
                .longOpt("sample-rate-sec")
                .hasArg()
                .desc("Sample Rate Seconds; defaults to 10")
                .build();

        Option resetCountOp = Option.builder("n")
                .longOpt("num-samples-no-change")
                .hasArg()
                .desc("Reset after number of this number of samples of no change in count; defaults to 1")
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

        Option printMessagesOp = Option.builder("o")
                .longOpt("print-messages")
                .desc("Print Messages to stdout")
                .build();

        options.addOption(helpOp);
        options.addOption(brokersOp);
        options.addOption(topicOp);
        options.addOption(sampleRateSecOp);
        options.addOption(resetCountOp);
        options.addOption(usernameOp);
        options.addOption(passwordOp);
        options.addOption(printMessagesOp);

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
            System.out.println("Send lines from a file to an Http Server");
            System.out.println();
            formatter.printHelp(appName, options);
            System.exit(0);
        }

        String host = null;
        if (cmd.hasOption("b")) {
            host = cmd.getOptionValue("b");
        }
        System.out.println("broker: " + host);

        String topic = null;
        if (cmd.hasOption("t")) {
            topic = cmd.getOptionValue("t");
        }
        System.out.println("topic: " + topic);

        int sampleRateSec = 10;
        if(cmd.hasOption("r")) {
            try {
                sampleRateSec = Integer.parseInt(cmd.getOptionValue("r"));
            } catch (NumberFormatException e ) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid sample-rate-sec (r).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("sampleRateSec: " + sampleRateSec);

        int numSampleEqualBeforeExit = 1;
        if(cmd.hasOption("n")) {
            try {
                numSampleEqualBeforeExit = Integer.parseInt(cmd.getOptionValue("n"));
            } catch (NumberFormatException e ) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid num-samples-no-change (s).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("numSampleEqualBeforeExit: " + numSampleEqualBeforeExit);

        String username = "";
        if (cmd.hasOption("u")) {
            username = cmd.getOptionValue("u");
        }
        System.out.println("username: " + username);


        String password = "";
        if (cmd.hasOption("p")) {
            password = cmd.getOptionValue("p");
        }
        System.out.println("password: " + password);

        boolean printMessages = false;
        if(cmd.hasOption("o")) {
            printMessages = true;
        }
        System.out.println("printMessages : " + printMessages);

        app =  new MqttMon(host, topic, username, password, sampleRateSec, numSampleEqualBeforeExit, printMessages);
        app.run();

        
    }

}
