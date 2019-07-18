### com.esri.rttest.send.Elasticsearch

Elasticsearch sends lines of json from a file to Elasticsearch using Elasticsearch Rest API.  

```
java -cp target/rttest.jar com.esri.rttest.send.Elasticsearch 
Usage: Elasticsearch [indexURL] [file] [desiredRatePerSec] [numToSend] (reuseFile=true)
```

Example Command:

```
java -cp target/rttest.jar com.esri.rttest.send.Elasticsearch http://localhost:9200/planes planes.json 10000 500000
```

The last parameter is optional and default to 1,000. 



