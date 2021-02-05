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

/**
 *
 * @author davi5017
 */
public class MqttMon extends Monitor {

    @Override
    public Sample getSample() {
        long cnt = -1L;
        long ts = 0L;

        cnt = mqttSink.getCnt();
        ts = System.currentTimeMillis();


        return new Sample(cnt, ts);    }

    @Override
    public void countEnded() {

    }
    

            

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
        int numargs = args.length;

        if (numargs < 4) {
            System.err.append("Usages: MqqtMon [host] [topic] [username] [password] (sampleRateSec=10) (numSampleEqualBeforeExit=1)");
            System.err.append("Example: java -cp target/rttest.jar com.esri.rttest.mon.MqttMon tcp://52.191.131.159:1883 test username password 10 8\n");
        }

        String host = args[0];
        String topic = args[1];
        String username = args[2];
        String password = args[3];

        Integer sampleRateSec = 10;
        if (numargs > 4) {
            sampleRateSec = Integer.parseInt(args[4]);
        }

        Integer numSampleEqualBeforeExit = 1;
        if (numargs > 5) {
            numSampleEqualBeforeExit = Integer.parseInt(args[5]);
        }

        Boolean printMessages = false;
        if (numargs > 6) {
            printMessages = Boolean.parseBoolean(args[6]);
        }

        new MqttMon(host, topic, username, password, sampleRateSec, numSampleEqualBeforeExit, printMessages);
        
    }

}
