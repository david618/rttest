### com.esri.rttest.monitors.ElasticIndexMon
Monitors a Elasticsearch Index count and measures and reports rate of change in count.  

When the tool starts it gets the current count and starts sampling count every sampleRateSec seconds (defaults to 5 seconds).

When count changes the tool starts collecting sample points. 

After collecting three points the output will use linear regression to estimate the rate of change.

After count stops changing the final line will give the count received and the best fit linear approximation of the rate.

<pre>
java -cp target/rttest.jar com.esri.rttest.monitors.ElasticIndexMon
Usage: ElasticIndexMon &lt;ElasticsearchServerPort&gt; &lt;Index/Type&gt; (&lt;username&gt; &lt;password> &lt;sampleRateSec&gt;)
</pre>

Example:

<pre>
java -cp target/rttest.jar com.esri.rttest.monitors.ElasticIndexMon 172.17.2.5:9200 satellites/satellites - - 60

- Elasticsearch running on 172.17.2.5 on default port of 9200
- The index name is satellites and type name is satellites (satellites/satellites)
- If the system doesn't require a password you can use dash
- Sample every 60 seconds
</pre>

### DC/OS

For DC/OS if you deployed Elastic name "sats-ds01" then you can access via an endpoint like: `data.sats-ds01.l4lb.thisdcos.directory`

If you used the default username/password.

<pre>
curl -u elastic:changeme data.sats-ds01.l4lb.thisdcos.directory:9200
curl -u elastic:changeme data.sats-ds01.l4lb.thisdcos.directory:9200/_aliases?pretty
</pre>

Example Command:
<pre>
java -cp target/rttest.jar com.esri.rttest.monitors.ElasticIndexMon data.sats-ds01.l4lb.thisdcos.directory:9200 planes-bat/planes-bat elastic changeme 20
</pre>


### GeoEvent

**NOTE:** For GeoEvent you can get the username/password for the spatiotemportal datastore using Datastore tool "listadmins". 


