### com.esri.rttest.producers.ElasticsearchTrans

This tool sends lines from a file to Elasticsearch using the Trasport API.  Higher rates were achieved with this method (e.g. More than 50,000/s).

<pre>
java -cp target/rttest.jar com.esri.rttest.producers.ElasticsearchTrans p1:9300 elasticsearch my planes planes.json 70000 5000000 30000
</pre>

The output

<pre>
5000000,54567.28145803776
</pre>
