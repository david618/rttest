### com.esri.rttest.monitors.KafkaTopicMon
Monitors a Kafka Topic count and measures and reports rate of change in count.

$ java -cp target/rttest.jar com.esri.rttest.monitors.KafkaTopicMon
Usage: KakfaTopicMon &lt;brokers&gt; &lt;topic&gt; (&lt;sampleRate&gt;)

$ java -cp target/rttest.jar com.esri.rttest.monitors.KafkaTopicMon 172.17.2.5:9528 satellites-in 60

- Connects to Kafka on 172.17.2.5 on port 9528 
- Gets counts for the satellites-in topic
- The sample rate is set to 60; which is 60 seconds
- At or around Kafka 0.10.x; when starting the tool displays a bunch of what looks like INFO messages from logger; if you append a redirect for error messages (e.g.  2>stderr.txt) to the command the messages will be hidden. Tried to configure logger for the tool but it did not help with these Error messages.
