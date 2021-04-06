### com.esri.rttest.mon.ElasticIndexMon

Monitors a Elasticsearch Index count and measures and reports rate of change in count.  

Use **Ctrl-C** to stop.

#### Help

Bash command: monElastic

```
./monElastic
Missing required option: l

usage: ElasticIndexMon
    --help                          display help and exit
 -l,--elastic-index-url <arg>       [Required] Elastic Index URL (e.g. http://es:9200/planes)
 -n,--num-samples-no-change <arg>   Reset after number of this number of samples of no change in count; defaults to 1
 -p,--password <arg>                Mqtt Server Password; default no password
 -r,--sample-rate-sec <arg>         Sample Rate Seconds; defaults to 10
 -u,--username <arg>                Mqtt Server Username; default no username
```

#### Example

```
./monElastic -l http://datastore-elasticsearch-client.a4iot-cqvgkj9zrnkn9bcu-services:9200/planes -n 1 -r 10
url: http://datastore-elasticsearch-client.a4iot-cqvgkj9zrnkn9bcu-services:9200/planes
sampleRateSec: 10
numSampleEqualBeforeExit: 1
username:
password:
Start Count: 1280
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1617739120901 |       0 |            1,000 |                  |                  |                  |
|          2 |           2 |1617739130903 |      10 |            2,000 |              100 |              100 |              100 |
|          3 |           3 |1617739141013 |      20 |            3,000 |               99 |               99 |               99 |
|          4 |           4 |1617739150900 |      29 |            4,000 |              100 |              101 |              100 |
|          5 |           5 |1617739160901 |      40 |            4,900 |               98 |               90 |               98 |
|          6 |           6 |1617739170916 |      50 |            6,000 |               99 |              110 |              100 |
|          7 |           7 |1617739180917 |      60 |            7,000 |              100 |              100 |              100 |
|          8 |           8 |1617739190902 |      70 |            8,000 |              100 |              100 |              100 |
|          9 |           9 |1617739200906 |      80 |            8,900 |               99 |               90 |               99 |
|         10 |          10 |1617739210905 |      90 |           10,000 |              100 |              110 |              100 |

For last 10  seconds the count has not increased...
Removing sample: 100|10000
Total Count: 10,000 | Linear Regression Rate:  99 | Linear Regression Standard Error: 0.00 | Average Rate: 99

Start Count: 11280
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|


```
