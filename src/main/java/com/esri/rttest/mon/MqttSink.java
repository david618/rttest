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

import java.util.UUID;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author davi5017
 */
public class MqttSink implements MqttCallback {
    // Connects to Mqtt Host and listens for topic counting messages returned

    long cnt;
    boolean printMessages;

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        cnt += 1;
        if (printMessages) System.out.println(message);
    }

    public Long getCnt() {
        return cnt;
    }

    public MqttSink(String host, String topic, String username, String password, boolean printMessages) {
        this.printMessages = printMessages;

        UUID uuid = UUID.randomUUID();
        String clientId = uuid.toString();
        
        cnt = 0L;
        
        try {
            MqttClient sampleClient = new MqttClient(host, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + host);
            connOpts.setUserName(username);
            connOpts.setPassword(password.toCharArray());
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            sampleClient.setCallback(this);
            sampleClient.subscribe(topic);

        } catch (MqttException me) {
            System.out.println("reason " + me.getReasonCode());
            System.out.println("msg " + me.getMessage());
            System.out.println("loc " + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }        
        
    }


    @Override
    public void connectionLost(Throwable thrwbl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


}
