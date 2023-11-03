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

import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

import java.util.*;


/*
 * Sends lines of a text file to a Kafka Topic
 * Lines are sent at a specified rate.
 *
 * @author david
 */
public class Kafka extends Send {


    @Override
    public long sendBatch(ArrayList<String> lines) {

        Iterator<String> linesIterator = lines.iterator();

        long cnt = 0;

        while (linesIterator.hasNext()) {
            String line = linesIterator.next();
            UUID uuid = UUID.randomUUID();
            producer.send(new ProducerRecord<>(this.topic, uuid.toString(), line));
            cnt += 1;
        }
        producer.flush();

        return cnt;

    }

    @Override
    public void sendDone() {

    }

    public Kafka() {}

    private Producer<String, String> producer;
    private String topic;

    public void run(String brokers, String topic, String filename, Integer desiredRatePerSec, Long numToSend, boolean reuseFile, String username, String password, String truststore) {


        // https://kafka.apache.org/documentation/#producerconfigs
        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("client.id", Kafka.class.getName());
        props.put("acks", "1");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 8192000);
        props.put("request.timeout.ms", "11000");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        if (truststore != "") {
            if (username != "" && password != "") {
                props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            } else {
                props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
            }
        } else {
            if (username != "" && password != "") {
                props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            }
        }


        if (truststore != "" &&  !truststore.equals("nocert")) {
            System.out.println(truststore);

            String ext = FilenameUtils.getExtension(truststore);

            if (ext.equalsIgnoreCase("pem")) {
                props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "PEM");
                props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststore);
            } else if (ext.equalsIgnoreCase("jks")) {
                props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "JKS");
                props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, truststore);
            } else {
                System.out.println("Unrecognized truststore format; should end with pem or jks");
                System.exit(1);
            }
        }

        if (username != "" && password != "") {
            props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
            props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + username + "\" password=\"" + password + "\";");
        }

        /* Addin Simple Partioner didn't help */
        //props.put("partitioner.class", SimplePartitioner.class.getCanonicalName());

        this.producer = new KafkaProducer<>(props);
        this.topic = topic;

        // Part of Abstract Class Send
        this.desiredRatePerSec = desiredRatePerSec;
        this.numToSend = numToSend;
        this.filename = filename;
        this.reuseFile = reuseFile;

        sendFiles();

    }

    public static void main(String[] args) {

        Kafka app = new Kafka();
        String appName = app.getClass().getSimpleName();

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);
        formatter.setLeftPadding(1);

        Option helpOp = Option.builder()
                .longOpt("help")
                .desc("display help and exit")
                .build();

        Option brokersOp = Option.builder("b")
                .longOpt("brokers")
                .required()
                .hasArg()
                .desc("[Required] Brokers (e.g. broker:9092)")
                .build();

        Option topicOp = Option.builder("t")
                .longOpt("topic")
                .required()
                .hasArg()
                .desc("[Required] Kafka Topic ")
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
                .desc("[Required] Desired Rate. The tool will try to send at this rate if possible")
                .build();

        Option numToSendOp = Option.builder("n")
                .longOpt("number-to-send")
                .required()
                .hasArg()
                .desc("[Required] Number of lines to send")
                .build();


        Option onetimeOp = Option.builder("o")
                .longOpt("one-time")
                .desc("Send lines only one time. Stop when all lines have been sent.")
                .build();

        Option usernameOp = Option.builder("u")
                .longOpt("username")
                .hasArg()
                .desc("(Optional) Username for Kafka")
                .build();

        Option passwordOp = Option.builder("p")
                .longOpt("password")
                .hasArg()
                .desc("(Optional) Password for Kafka")
                .build();

        Option truststoreOp = Option.builder("s")
                .longOpt("file")
                .hasArg()
                .desc("[Required] Truststore file with either pem or jks certificates")
                .build();

        options.addOption(helpOp);
        options.addOption(brokersOp);
        options.addOption(topicOp);
        options.addOption(fileOp);
        options.addOption(rateOp);
        options.addOption(numToSendOp);
        options.addOption(onetimeOp);
        options.addOption(usernameOp);
        options.addOption(passwordOp);
        options.addOption(truststoreOp);


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

        String broker = null;
        if (cmd.hasOption("b")) {
            broker = cmd.getOptionValue("b");
        }
        System.out.println("broker: " + broker);

        String topic = null;
        if (cmd.hasOption("t")) {
            topic = cmd.getOptionValue("t");
        }
        System.out.println("topic: " + topic);

        String file = null;
        if(cmd.hasOption("f")) {
            file = cmd.getOptionValue("f");
        }
        System.out.println("file: " + file);

        Integer desiredRatePerSec = null;
        if(cmd.hasOption("r")) {
            try {
                desiredRatePerSec = Integer.parseInt(cmd.getOptionValue("r"));
            } catch (NumberFormatException e ) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid value for rate (r).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("desiredRatePerSec: " + desiredRatePerSec);

        Long numToSend = null;
        if(cmd.hasOption("n")) {
            try {
                numToSend = Long.parseLong(cmd.getOptionValue("n"));
            } catch (NumberFormatException e) {
                System.out.println();
                System.out.println("Invalid value for num-to-send (n). Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("numToSend: " + numToSend);

        boolean reuseFile = true;
        if(cmd.hasOption("o")) {
            reuseFile = false;
        }
        System.out.println("reuseFile : " + reuseFile);

        String username = "";
        if (cmd.hasOption("u")) {
            username = cmd.getOptionValue("u");
        }
        System.out.println("username : " + username);


        String password = "";
        if (cmd.hasOption("p")) {
            password = cmd.getOptionValue("p");
        }
        System.out.println("password : " + password);

        String truststore = "";
        if (cmd.hasOption("s")) {
            truststore = cmd.getOptionValue("s");
        }
        System.out.println("truststore : " + truststore);

        app.run(broker,topic, file, desiredRatePerSec, numToSend, reuseFile, username, password, truststore);


    }
}
