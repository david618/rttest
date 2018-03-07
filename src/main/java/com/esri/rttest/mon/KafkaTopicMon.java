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
 * Monitors an Kafka Topic
 * Periodically does a count and when count is changing collects samples.
 * After three samples are made outputs rates based on linear regression.
 * After counts stop changing outputs the final rate and last estimated rate.
 *
 * 30 Aug 2017: Started adding Logging to try to get rid of log messages on startup.
 * Didn't work. If however you add 2>/dev/null to end of command line the info messages are hidden.
 *
 * Creator: David Jennings
 *
 */
package com.esri.rttest.mon;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author david
 */
public class KafkaTopicMon {

    private static final Logger log = LogManager.getLogger(KafkaTopicMon.class);

    // ******************* TimerTask Class ******************************
    class CheckCount extends TimerTask {

        long cnt1 = 0;
        long cnt2 = -1;
        long startCount = 0;
        long endCount = 0;
        int numSamples = 0;
        HashMap<Long, Long> samples;
        long t1 = 0L;
        long t2 = 0L;
        SimpleRegression regression;

        public CheckCount() {
            regression = new SimpleRegression();
            cnt1 = 0;
            cnt2 = -1;
            startCount = 0;
            numSamples = 0;
            t1 = 0L;
            t2 = 0L;

        }

        boolean inCounting() {
            if (cnt1 > 0) {
                return true;
            } else {
                return false;
            }
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
                log.info("Checking Count");

                List<TopicPartition> partitions = consumer.partitionsFor(topic).stream()
                        .map(p -> new TopicPartition(topic, p.partition()))
                        .collect(Collectors.toList());
                consumer.assign(partitions);
                consumer.seekToEnd(Collections.emptySet());
                Map<TopicPartition, Long> endPartitions = partitions.stream()
                        .collect(Collectors.toMap(Function.identity(), consumer::position));
//              consumer.seekToBeginning(Collections.emptySet());
//              cnt1 = partitions.stream().mapToLong(p -> endPartitions.get(p) - consumer.position(p)).sum();
                Iterator itTP = endPartitions.entrySet().iterator();
                cnt1 = 0;
                while (itTP.hasNext()) {
                    Map.Entry tp = (Map.Entry) itTP.next();
                    cnt1 += (long) tp.getValue();
                }

                t1 = System.currentTimeMillis();

                if (cnt2 == -1 || cnt1 < cnt2) {
                    cnt2 = cnt1;
                    startCount = cnt1;
                    endCount = cnt1;
                    regression = new SimpleRegression();
                    samples = new HashMap<>();
                    numSamples = 0;

                } else if (cnt1 > cnt2) {
                    // Increase number of samples
                    numSamples += 1;

                    // Add to Linear Regression
                    regression.addData(t1, cnt1);
                    samples.put(t1, cnt1);
                    

                    if (numSamples > 2) {
                        double rcvRate = regression.getSlope() * 1000;
                        if (sendStdout) {
                            System.out.println(numSamples + "," + t1 + "," + cnt1 + "," + String.format("%.0f", rcvRate));
                        }
                    } else {
                        System.out.println(numSamples + "," + t1 + "," + cnt1);
                    }

                } else if (cnt1 == cnt2 && numSamples > 0) {
                    
                    endCount = cnt1;
                    
                    numSamples -= 1;
                    // Remove the last sample
                    regression.removeData(t2, cnt2);
                    samples.remove(t2, cnt2);
                    
                    if (sendStdout) {
                        System.out.println("Removing: " + t2 + "," + cnt2);
                    }
                    // Output Results
                    long cnt = cnt2 - startCount;
                    double rcvRate = regression.getSlope() * 1000;  // converting from ms to seconds

                    if (numSamples > 5) {
                        double rateStdErr = regression.getSlopeStdErr();
                        if (sendStdout) {
                            System.out.format("%d , %.0f, %.4f\n", cnt, rcvRate, rateStdErr);
                        }
                    } else if (numSamples >= 2) {
                        if (sendStdout) {
                            System.out.format("%d , %.0f\n", cnt, rcvRate);
                        }
                    } else {
                        if (sendStdout) {
                            System.out.println("Not enough samples to calculate rate. ");
                        }
                    }

                    // Reset 
                    cnt1 = -1;
                    cnt2 = -1;
                    startCount = 0;
                    numSamples = 0;
                    t1 = 0L;
                    t2 = 0L;
                    regression = new SimpleRegression();

                }

                cnt2 = cnt1;
                t2 = t1;
            } catch (Exception e) {
                log.error("ERROR", e);

            }

        }

    }
    // *****************************************************************

    Timer timer;
    String brokers;
    String topic;
    long sampleRate;
    KafkaConsumer<String, String> consumer;
    boolean sendStdout;

    public KafkaTopicMon(String brokers, String topic, long sampleRate, boolean sendStdout) {

        try {
            this.brokers = brokers;
            this.topic = topic;
            this.sampleRate = sampleRate;
            this.sendStdout = sendStdout;

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
                log.info(tp);
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
            log.error("Could not connect to Kafka");
            System.exit(-1);

        } catch (Exception e) {
            log.error("ERROR", e);

        }

    }

    public void run() {
        try {

            timer = new Timer();
            timer.schedule(new CheckCount(), 0, sampleRate * 1000);

        } catch (Exception e) {
            log.error("ERROR", e);
        }

    }

    public static void main(String[] args) {

        String broker = "";
        String topic = "";
        int sampleRateSec = 5; // default to 5 seconds.
        Boolean sendStdout = true;
        

        log.info("Entering application.");
        int numargs = args.length;
        if (numargs != 2 && numargs != 3) {
            System.err.println("Usage: KakfaTopicMon [brokers] [topic] (sampleRateSec)");
            System.err.println("Example Command: java -cp target/rttest.jar com.esri.rttest.mon.KafkaTopicMon broker.kafka.l4lb.thisdcos.directory:9092 simFile 2>/dev/null");
        } else {
            broker = args[0];
            topic = args[1];
            if (numargs == 3) {
                sampleRateSec = Integer.parseInt(args[2]);
            }
        }

        KafkaTopicMon ktm = new KafkaTopicMon(broker, topic, sampleRateSec, sendStdout);
        ktm.run();

    }

}
