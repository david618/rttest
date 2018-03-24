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
package com.esri.rttest;

/**
 *
 * @author david
 */
public class Help {
    
    
    public static void main(String args[]) {
        
        System.out.println("Here is a list of supported functions of the Simulator with a brief description.");
        
        System.out.println("");
        System.out.println("Classes that send.");        
        System.out.println("com.esri.send.Elasticsearch   : Send lines from file to Elasticsearch using Transport Protocol.");
        System.out.println("com.esri.send.Http            : Send lines from file to server using Http POST.");
        System.out.println("com.esri.send.Kafka           : Send lines from file to Kafka.");
        System.out.println("com.esri.send.Postgresql      : Send lines from a file to Postgresql Database.");        
        System.out.println("com.esri.send.Tcp             : Send lines from file to server on tcp port.");
        System.out.println("com.esri.send.Tcp2            : Send lines from file to server on tcp port (uses threads).");
        
        System.out.println("");
        System.out.println("Classes that monitor.");        
        System.out.println("com.esri.mon.FeatureLayerMon : Monitor count on a Feature Layer.");
        System.out.println("com.esri.mon.KafkaTopicMon   : Monitor count of a Kafka Topic.");
        
        System.out.println("");
        System.out.println("Classes that sink.");                
        System.out.println("com.esri.sink.TcpSink         : Count messages recevied via TCP on a specified port.");
        System.out.println("com.esri.sink.WebSocketSink   : Count messages received via Web Socket on a specified port.");
        
        System.out.println("");
        System.out.println("For additional help on each command; execute without any command line arguments. ");
        System.out.println("For example: java -cp target/Simulator.jar com.esri.simulator.Elasticsearch");
        
        
    }
    
}
