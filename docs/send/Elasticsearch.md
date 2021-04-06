### com.esri.rttest.send.Elasticsearch

Send Lines of a file to an Elastic Index.

#### Help
```
java -cp target/rttest.jar com.esri.rttest.send.Elasticsearch
Missing required options: l, f, r, n

usage: Elasticsearch
 -f,--file <arg>             [Required] File with lines of text to send; if a folder is specified all files in the folder are sent one at a time alphabetically
    --help                   display help and exit
 -l,--url <arg>              [Required] Elasticsearch Index URL
 -n,--number-to-send <arg>   [Required] Number of lines to send
 -o,--one-time               Send lines only one time. Stop when all lines have been sent.
 -r,--rate <arg>             [Required] Desired Rate. The tool will try to send at this rate if possible
```

#### Example Command

```
./sendElastic  -l http://datastore-elasticsearch-client.a4iot-cqvgkj9zrnkn9bcu-services:9200/planes -f planes.json -n 10000 -r 100
indexUrl: http://datastore-elasticsearch-client.a4iot-cqvgkj9zrnkn9bcu-services:9200/planes
file: planes.json
desiredRatePerSec: 100
numToSend: 10000
numThreads: 1
reuseFile : true
Start Send
Use Ctrl-C to Abort.

|Number Sent         |Current Rate Per Sec|Overall Rate Per Sec|
|--------------------|--------------------|--------------------|
|                100 |                100 |                100 |
|                200 |                100 |                100 |
|                300 |                100 |                100 |
|                400 |                100 |                100 |
|                500 |                100 |                100 |
|                600 |                100 |                100 |
|                700 |                100 |                100 |
...
|               9400 |                100 |                100 |
|               9500 |                100 |                100 |
|               9600 |                100 |                100 |
|               9700 |                100 |                100 |
|               9800 |                100 |                100 |
|               9900 |                100 |                100 |
|              10000 |                100 |                100 |
Done
```





