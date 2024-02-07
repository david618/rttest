package com.esri.rttest.mon;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitMQSink  {

    long cnt = 0L;
    boolean printMessages;

    public RabbitMQSink(String host, Integer port, String queue, String username, String password, boolean printMessages) {
        
        this.printMessages = printMessages;

           ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setUsername(username);
            factory.setPassword(password);
            Connection connection;
            Channel channel;
            
            try {
                connection = factory.newConnection();
                channel = connection.createChannel();
                DefaultConsumer consumer = new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(
                        String consumerTag,
                        Envelope envelope, 
                        AMQP.BasicProperties properties, 
                        byte[] body) throws IOException {
                            incrementCnt();
                            if (printMessages) {
                                String message = new String(body, "UTF-8");                            
                                System.out.println(message);    
                            }
                    }
                };
                channel.basicConsume(queue, true, consumer);


            } catch (IOException | TimeoutException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 

    }

    private void incrementCnt() {
        cnt += 1;
    }

    public long getCnt() {
        return cnt;
    }


    
}
