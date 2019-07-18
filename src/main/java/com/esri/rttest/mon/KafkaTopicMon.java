package com.esri.rttest.mon;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

        long cnt = -1L;
        long ts = 0L;

        try {
            List<TopicPartition> partitions = consumer.partitionsFor(topic).stream()
                    .map(p -> new TopicPartition(topic, p.partition()))
                    .collect(Collectors.toList());
            consumer.assign(partitions);
            consumer.seekToEnd(Collections.emptySet());
            Map<TopicPartition, Long> endPartitions = partitions.stream()
                    .collect(Collectors.toMap(Function.identity(), consumer::position));
            Iterator itTP = endPartitions.entrySet().iterator();
            cnt = 0;
            while (itTP.hasNext()) {
                Map.Entry tp = (Map.Entry) itTP.next();
                cnt += (long) tp.getValue();
            }

        } catch (Exception e) {
            cnt = -1;
        }

        ts = System.currentTimeMillis();

        return new Sample(cnt, ts);

    }


    KafkaConsumer<String, String> consumer;
    String brokers;
    String topic;

    public KafkaTopicMon(String brokers, String topic, int sampleRateSec, int numSampleEqualBeforeExit) {

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

            consumer = new KafkaConsumer<>(props);

            boolean topicFound = false;

            Iterator<String> tps = consumer.listTopics().keySet().iterator();
            while (tps.hasNext()) {

                String tp = tps.next();
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

        String broker = "";
        String topic = "";
        int sampleRateSec = 10;
        int numSampleEqualBeforeExit = 1;

        LOG.info("Entering application.");
        int numargs = args.length;
        if (numargs < 2 ) {
            System.err.println("Usage: KakfaTopicMon [brokers] [topic] (sampleRateSec=10) (numSampleEqualBeforeExit=1)");
            System.err.println("Example Command: KafkaTopicMon broker:9092 planes 30");
            System.err.println("");
            System.err.println("brokers: Server name or ip of broker(s)");
            System.err.println("topic: Kafka Topic");
            System.err.println("sampleRateSecs: How many seconds to wait between samples.");
            System.err.println("numSampleEqualBeforeExit: Summarize and reset after this many samples where count does not change.");
            System.err.println("");
        } else {
            broker = args[0];
            topic = args[1];
            if (numargs > 2) {
                sampleRateSec = Integer.parseInt(args[2]);
                if (sampleRateSec < 1) {
                    System.err.println("SampleRateSec must be greater than 0");
                    System.exit(1);
                }            }
            if (numargs > 3) {
                numSampleEqualBeforeExit = Integer.parseInt(args[3]);
                if (numSampleEqualBeforeExit < 1) {
                    System.err.println("numSampleEqualBeforeExit must be greater than 1");
                    System.exit(2);
                }
            }

            KafkaTopicMon ktm = new KafkaTopicMon(broker, topic, sampleRateSec, numSampleEqualBeforeExit);
            ktm.run();
        }

    }

}
