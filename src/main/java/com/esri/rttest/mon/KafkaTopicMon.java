package com.esri.rttest.mon;

import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

public class KafkaTopicMon extends Monitor {

    private static final Logger LOG = LogManager.getLogger(KafkaTopicMon.class);


    @Override
    public void countEnded() {

    }

    @Override
    public Sample getSample() {

        long cnt;
        long ts;

        try {
            List<TopicPartition> partitions = consumer.partitionsFor(topic).stream()
                    .map(p -> new TopicPartition(topic, p.partition()))
                    .collect(Collectors.toList());
            consumer.assign(partitions);
            consumer.seekToEnd(Collections.emptySet());
            Map<TopicPartition, Long> endPartitions = partitions.stream()
                    .collect(Collectors.toMap(Function.identity(), consumer::position));
            Iterator<Entry<TopicPartition, Long>> itTP = endPartitions.entrySet().iterator();
            cnt = 0;
            while (itTP.hasNext()) {
                cnt += itTP.next().getValue();
            }

        } catch (Exception e) {
            cnt = -1;
        }

        ts = System.currentTimeMillis();

        return new Sample(cnt, ts);

    }

    public KafkaTopicMon() {}

    KafkaConsumer<String, String> consumer;
    String brokers;
    String topic;

    public KafkaTopicMon(String brokers, String topic, int sampleRateSec, int numSampleEqualBeforeExit, String username, String password, String truststore) {

        try {
            this.brokers = brokers;
            this.topic = topic;

            this.sampleRateSec = sampleRateSec;
            this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;


            Properties props = new Properties();

            // https://kafka.apache.org/documentation/#consumerconfigs
            props.put("bootstrap.servers", this.brokers);
            // Should include another parameter for group.id this would allow differenct consumers of same topic
            props.put("group.id", "abc");
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", 1000);
            props.put("auto.offset.reset", "earliest");
            props.put("session.timeout.ms", "10000");
            props.put("request.timeout.ms", "11000");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

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
                //props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");

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
                //props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
                props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
                props.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + username + "\" password=\"" + password + "\";");
            }

            consumer = new KafkaConsumer<>(props);

            boolean topicFound = false;

            for (String tp : consumer.listTopics().keySet()) {

                LOG.info(tp);
                if (this.topic.equals(tp)) {
                    topicFound = true;
                    break;
                }

            }

            if (!topicFound) {
                System.out.println("Topic not found");
                System.exit(-2);
            }

        } catch (TimeoutException e) {
            LOG.error("Could not connect to Kafka");
            System.exit(-1);

        } catch (Exception e) {
            LOG.error("ERROR", e);

        }

    }

    public static void main(String[] args) {

        KafkaTopicMon app = new KafkaTopicMon();
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
        options.addOption(sampleRateSecOp);
        options.addOption(resetCountOp);
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


        app =  new KafkaTopicMon(broker, topic, sampleRateSec, numSampleEqualBeforeExit, username, password, truststore);
        app.run();

    }

}
