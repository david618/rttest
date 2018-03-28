### com.esri.rttest.send.Elasticsearch

This tools sends json from a file to Elasticsearch using Elasticsearch Rest API.  

<pre>
$ java -cp target/rttest.jar com.esri.rttest.send.Elasticsearch 
Usage: Elasticsearch &lt;elastic-search-transports&gt; &lt;cluster-name&gt; &lt;index&gt; &lt;type&gt; &lt;file&gt; &lt;rate&gt; &lt;numrecords&gt; (&lt;elastic-bulk-num&gt;)
</pre>

Example Command:

<pre>
java -cp target/rttest.jar com.esri.rttest.send.Elasticsearch http://localhost:9200/my/planes planes.json 10000 500000
</pre>

The last parameter is optional and default to 1,000. 

When complete the tool outputs the number of records sent and the achieved rate.  For example:

<pre>
500000,3667.6251393697553
</pre>

**Note**: This tool coule probably be improved by implementing threads.  Max rate is limited using a single thread to less than 5,000/s.  



