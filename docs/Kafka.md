### com.esri.rttest.producers.Kafka

$ java -cp rttest.jar com.esri.simulator.Kafka 

Usage: Kafka &lt;broker-list-or-hub-name&gt; &lt;topic&gt; &lt;file&gt; &lt;rate&gt; &lt;numrecords&gt; (&lt;burst-delay-ms&gt;)
- Sends lines from file to the specified broker-list-or-hub-name topic.  
- The simulator tries to send numrecords at rate requested. 
- If burst-delay-ms is specified the records are send in bursts ever burst-delay-ms milliseconds to achieve the desired rate. For example, if you request 10,000 e/s with a burst delay of 100; the simulator will send at max rate possible 1,000 events every 100 ms.  If not specified the results are sent one every 1/10,000 of a second. 
