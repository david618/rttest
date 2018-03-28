### com.esri.rttest.send.Http

Post lines from a file to URL. 

Changes
- Added indication of error (error count) to output
- Added additional optional parameter to support threads 
- Added support to lookup ip and ports and send directly to Marathon App instances 
- Added DNS lookup support; If DNS has multiple IP's; threads are assigned round-robin to the IP's

<pre>
java -cp target/rttest.jar com.esri.rttest.send.Http  
Usage: Http &lt;url&gt; &lt;file&gt; &lt;rate&gt; &lt;numrecords&gt; (&lt;numthreads=1&gt;)
</pre>

Parameters
- url: The url you want to send the ports to. Server name can be Marathon app name.
  - If server is specified as app[marathon-app-name]; Http looks up ip:port for each instance
  - Each thread is assigned an ip:port in a round-robin fashion
- file: The name of the file to read lines from 
- rate: Desired rate. App will try to dynamically adjust to achieve this rate
- numrecords: Number of lines to post. Once file is exhausted it will automatically start from top of file again
- numthreads: Optional parameter defaults to 1.

Example
<pre>
java -cp target/rttest.jar com.esri.rttest.send.Http  http<i></i>://app[sits/rcv-txt-rest-planes]/rtgis/receiver/planes/txt  planes00001.1M 50000 1000000 64
</pre>

This command
- Looks up the ip:port for each instance of sits/rcv-txt-rest-planes Marthon app.
- Creates 64 threads; then assigns an ip and port each thread (e.g. http:<i></i>//172.17.2.6:3455//rtgis/receiver/planes/txt) in round-robin fashion. Thread 1 gets ip:port 1, Thread 2 gets ip:port2, and so on. If the number of threads is greater than number of ip:ports the ip:port assignment resumes at 1.
- The lines from the file planes00001.1M are added to a shared blocked queue at the rate specified
- The threads read lines from the queue and send them to the url they were assigned

Example Output

<pre>
172.17.2.9:26264
172.17.2.8:28203
172.17.2.4:11865
172.17.2.6:2152
172.17.2.7:1718
172.17.2.5:12370
15620,0,3042
31925,0,3150
48303,0,3191
...
969367,0,3339
987061,0,3343
1000000,0,3330
Queue Empty
1000000,0,3330
</pre>

The command outputs
- The IP:PORT's found for this Marathon App.
- Current Count Sent, Number of Errors (Should be zero), and rate achieved every 5 seconds.
- The rate send is often less than rate requested; because of back pressure from the endpoint

Number of Errors is the number of responses that were not HTTP 200. This happens if the URL is invalid or the end point is having some problem.
