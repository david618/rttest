### com.esri.rttest.send.Kafka

Send Lines of a file to a Kafka Topic

#### Help

```
./sendKafka
Missing required options: b, t, f, r, n

usage: Kafka
 -b,--brokers <arg>          [Required] Brokers (e.g. broker:9092)
 -f,--file <arg>             [Required] File with lines of text to send; if a folder is specified all files in the folder are sent one at a time alphabetically
    --help                   display help and exit
 -n,--number-to-send <arg>   [Required] Number of lines to send
 -o,--one-time               Send lines only one time. Stop when all lines have been sent.
 -r,--rate <arg>             [Required] Desired Rate. The tool will try to send at this rate if possible
 -t,--topic <arg>            [Required] Kafka Topic
```

#### Example

```
./sendKafka -b gateway-cp-kafka.a4iot-cqvgkj9zrnkn9bcu-services:9092 -t planes -r 250 -n 10000 -f planes.csv
broker: gateway-cp-kafka.a4iot-cqvgkj9zrnkn9bcu-services:9092
topic: planes
file: planes.csv
desiredRatePerSec: 250
numToSend: 10000
reuseFile : true
Start Send
Use Ctrl-C to Abort.
|Number Sent         |Current Rate Per Sec|Overall Rate Per Sec|
|--------------------|--------------------|--------------------|
|                250 |                250 |                250 |
|                500 |                250 |                250 |
|                750 |                250 |                250 |
|               1000 |                250 |                250 |
|               1250 |                251 |                250 |
|               1500 |                250 |                250 |
|               1750 |                250 |                250 |
|               2000 |                250 |                250 |
...
|               9000 |                250 |                250 |
|               9250 |                250 |                250 |
|               9500 |                250 |                250 |
|               9750 |                250 |                250 |
|              10000 |                250 |                250 |
Done
``