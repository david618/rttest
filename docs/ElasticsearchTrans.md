### com.esri.rttest.send.ElasticsearchTrans

This tool sends lines from a file to Elasticsearch using the Trasport API.  Higher rates were achieved with this method (e.g. More than 50,000/s).


<pre>

java -cp target/rttest.jar com.esri.rttest.send.ElasticsearchTrans
Usage: ElasticsearchTrans (elastic-search-transports) (cluster-name) (index) (type) (file) (rate) (numrecords) [elastic-bulk-num=1000]
</pre>

Default elastic-bulk-num = 1000.

#### Example Command

<pre>
java -cp target/rttest.jar com.esri.rttest.send.ElasticsearchTrans p1:9300 elasticsearch my planes planes.json 70000 5000000 30000
</pre>

Sends lines from planes.json to server named p1 on port 9300.  Tries to send  5,000,000 lines at 70,000/s using a bulk num of 30,000. 

#### Example output

<pre>
5000000,54567.28145803776
</pre>
