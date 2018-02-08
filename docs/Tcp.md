### com.esri.rttest.producers.Tcp

- Sends lines from file to the specified server and port
- The app tries to send numrecords at rate requested
- During the run the app counts the records and actual rate; timing is automatically adjusted to try try and match rate requested
- There is a maximum rate that is possible depending on hardware and network resources
- While running the count and actual rate sent is displayed  (Count, Rate)

<pre>
java -cp target/rttest.jar com.esri.rttest.producers.Tcp
Usage: Tcp2 &lt;server:port&gt; &lt;file&gt; &lt;rate&gt; &lt;numrecords&gt; (numThreads=1) (append-time=false)
server:port: The IP or hostname of server to send events to. Could be ip:port, dns-name:port, or app[marathon-app-name(:portindex)]
filename: sends line by line from this file.
rate: Attempts to send at this rate.
numrecords: Sends this many lines; file is automatically recycled if needed.
numThread: Number of threads defaults to 1
append-time: Adds system time as extra parameter to each request. 
</pre>

The function also support an optional numThreads to allow multple senders.
- With one thread you may see rates max out around 140,000/s
- With four threads you may see rates as high as 600,000/s
- With more than four the rates may not improve.

**Note:** append-time assomes the sending file is csv. This is intended for testing system latency.
