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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.cli.*;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 *
 * @author davi5017
 */
public class RabbitMQMon extends Monitor {


    @Override
    public Sample getSample() {

        long cnt;
        long ts;
        
        cnt = rabbitMQSink.getCnt();
        ts = System.currentTimeMillis();

        return new Sample(cnt, ts);

    }

    @Override
    public void countEnded() {

    }
    
    public RabbitMQMon() {}
            

    public RabbitMQMon(String host, Integer port, String queue, String username, String password, Integer sampleRateSec, Integer numSampleEqualBeforeExit, boolean printMessages) {
        this.host = host;
        this.queue = queue;
        this.username = username;
        this.password = password;
        this.sampleRateSec = sampleRateSec;
        this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        try {
            this.connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            this.channel = this.connection.createChannel();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.rabbitMQSink = new RabbitMQSink(host, port, queue, username, password, printMessages);


        run();
        
        
    }

    String host;
    Channel channel;
    Connection connection;
    String queue;
    Integer port;    
    String username;
    String password;
    RabbitMQSink rabbitMQSink;
    

    public static void main(String[] args) {

        RabbitMQMon app = new RabbitMQMon();
        String appName = app.getClass().getSimpleName();

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);
        formatter.setLeftPadding(1);

        Option helpOp = Option.builder()
                .longOpt("help")
                .desc("display help and exit")
                .build();

        Option hostOp = Option.builder("h")
                .longOpt("host")
                .required()
                .hasArg()
                .desc("[Required] RabbitMQ Host (e.g. rabbitmq.host.name)")
                .build();

        Option portOp = Option.builder("P")
                .longOpt("port")
                .hasArg()
                .desc("port default tos 5672")
                .build();

        Option queueOp = Option.builder("q")
                .longOpt("queue")
                .required()
                .hasArg()
                .desc("[Required] RabbitMQ queue ")
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
                .desc("RabbitMQ Username; default no username")
                .build();

        Option passwordOp = Option.builder("p")
                .longOpt("password")
                .hasArg()
                .desc("RabbitMQ Password; default no password")
                .build();

        Option printMessagesOp = Option.builder("o")
                .longOpt("print-messages")
                .desc("Print Messages to stdout")
                .build();

        options.addOption(helpOp);
        options.addOption(hostOp);
        options.addOption(portOp);
        options.addOption(queueOp);
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
        if (cmd.hasOption("h")) {
            host = cmd.getOptionValue("h");
        }
        System.out.println("host: " + host);

        Integer port = 5672;
        if(cmd.hasOption("P")) {
            try {
                port = Integer.parseInt(cmd.getOptionValue("P"));
                if (port < 1024  || port > 65535) {
                    throw new Exception("Must be between 1024 and 65,535 ");
                }
            } catch (NumberFormatException ne) {
                System.out.println("");
                System.out.println("Invalid value for port (P).  Must be an Integer");
                System.out.println("");
                formatter.printHelp(appName, options);
                System.exit(1);
            } catch (Exception e) {
                System.out.println("");
                System.out.println("Invalid value for port (P) " + e.getMessage());
                System.out.println("");
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("Port: " + port);        

        String queue = null;
        if (cmd.hasOption("q")) {
            queue = cmd.getOptionValue("q");
        }
        System.out.println("queue: " + queue);

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

        app =  new RabbitMQMon(host, port, queue, username, password, sampleRateSec, numSampleEqualBeforeExit, printMessages);
        //app.run();

        
    }

}
