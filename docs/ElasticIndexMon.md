### com.esri.rttest.monitors.ElasticIndexMon
Monitors a Elasticsearch Index count and measures and reports rate of change in count.

<pre>
$ java -cp target/rttest.jar com.esri.rttest.monitors.ElasticIndexMon
Usage: ElasticIndexMon &lt;ElasticsearchServerPort&gt; &lt;Index/Type&gt; (&lt;username&gt; &lt;password> &lt;sampleRateSec&gt;)
</pre>

Example:

<pre>
java -cp target/rttest.jar com.esri.simulator.ElasticIndexMon 172.17.2.5:9200 satellites/satellites - - 60

- Elasticsearch running on 172.17.2.5 on default port of 9200
- The index name is satellites and type name is satellites (satellites/satellites)
- If the system doesn't require a password you can use dash
- Sample every 60 seconds
</pre>

**NOTE:** For GeoEvent you can get the username/password for the spatiotemportal datastore using datastore tool "listadmins". 

