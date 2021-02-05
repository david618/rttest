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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

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


    private Producer<String, String> producer;
    private String topic;

    public Kafka(String brokers, String topic, String filename, Integer desiredRatePerSec, Long numToSend, boolean reuseFile) {


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

    public static void main(String args[]) throws Exception {

        // Command Line d1.trinity.dev:9092 simFile simFile_1000_10s.dat 1000 10000

        int numargs = args.length;
        if (numargs < 5 ) {
            System.err.print("Usage: Kafka [brokers] [topic] [file] [desiredRatePerSec] [numToSend] (reuseFile=true) \n");
            System.err.println("");
            System.err.println("broker: hostname or ip of the broker(s)");
            System.err.println("topic: Kafka Topic");
            System.err.println("file: file with lines of text to send to Elasticsearch; if folder than all files in the folder are sent and reuseFile is set to false.");
            System.err.println("desiredRatePerSec: Desired Rate. The tool will try to send at this rate if possible");
            System.err.println("numToSend: Number of lines to send");
            System.err.println("resueFile: true or false; if true the file is reused as needed to if numToSend is greater than number of lines in the file");
            System.err.println("");
        } else {

            String brokers = args[0];
            String topic = args[1];
            String file = args[2];
            Integer desiredRatePerSec = Integer.parseInt(args[3]);
            Long numToSend = Long.parseLong(args[4]);

            boolean reuseFile = true;
            if (numargs > 5) {
                reuseFile = Boolean.parseBoolean(args[5]);
            }

            new Kafka(brokers,topic, file, desiredRatePerSec, numToSend, reuseFile);

        }

    }
}
