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
package com.esri.rttest.producers;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 *
 * @author david
 */
public class KafkaSenderThread extends Thread {

    LinkedBlockingQueue<String> lbq;
    private volatile boolean running = true;

    private Producer<String, String> producer;
    private String topic;

    public KafkaSenderThread(LinkedBlockingQueue<String> lbq, Producer<String, String> producer, String topic) {
        this.lbq = lbq;
        this.topic = topic;
        this.producer = producer;


    }

    private long cntErr;
    private long cnt;

    public long getCntErr() {
        return cntErr;
    }

    public long getCnt() {
        return cnt;
    }

    public void terminate() {
        running = false;
        producer.flush();
    }

    @Override
    public void run() {

        try {
            while (running) {
                String line = lbq.take();
                if (line == null) {
                    break;
                }
                // Send the String

                UUID uuid = UUID.randomUUID();
                producer.send(new ProducerRecord<>(this.topic, uuid.toString(), line));
                cnt += 1;

            }

        } catch (InterruptedException e) {
            // ok to ignore
        }

    }
}
