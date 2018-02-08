### com.esri.rttest.sinks.TcpSink

$ java -cp target/rttest.jar com.esri.rttest.sinks.TcpSink
Usage: TcpSink &lt;port-to-listen-on&gt; (&lt;sample-every-N-records/1000&gt;) (&lt;display-messages/false&gt;)

- Listens on the port-to-listen-on for TCP 
- Counts features arriving 
- Adds sample every sample-every-N-records samples; defaults to 1,000
- Five seconds after features stop arriving the rate is calcuated and output to screen; then resets and starts listening again
- Setting display-messages to true will cause the sink to just display messages