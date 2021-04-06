### com.esri.rttest.monitors.KafkaTopicMon

Monitors a Kafka Topic count and measures and reports rate of change in count.  

Use **Ctrl-C** to stop.


#### Help

Bash Command: monKafka

```
./monKafka
Missing required options: b, t

usage: KafkaTopicMon
 -b,--brokers <arg>                 [Required] Brokers (e.g. broker:9092)
    --help                          display help and exit
 -n,--num-samples-no-change <arg>   Reset after number of this number of samples of no change in count; defaults to 1
 -r,--sample-rate-sec <arg>         Sample Rate Seconds; defaults to 10
 -t,--topic <arg>                   [Required] Kafka Topic
 ```

#### Example

```
./monKafka -n 2 -r 20 -b gateway-cp-kafka.a4iot-cqvgkj9zrnkn9bcu-services:9092 -t planes
broker: gateway-cp-kafka.a4iot-cqvgkj9zrnkn9bcu-services:9092
topic: planes
sampleRateSec: 20
numSampleEqualBeforeExit: 2
Start Count: 3870565
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1617734486431 |       0 |        2,001,173 |                  |                  |                  |
|          2 |           2 |1617734506422 |      19 |        3,988,320 |           99,402 |           99,402 |           99,402 |
|          3 |           3 |1617734526412 |      39 |        6,007,113 |          100,196 |          100,990 |          100,196 |
|          4 |           4 |1617734546412 |      59 |        7,130,435 |           87,061 |           56,166 |           85,515 |
|          5 |       ***** |1617734566412 |      79 |        7,130,435 |           87,061 |                0 |           64,131 |

For last 40  seconds the count has not increased...
Removing sample: 99|7130435
Total Count: 7,130,435 | Linear Regression Rate:  100,196 | Linear Regression Standard Error: 0.46 | Average Rate: 100,196

Start Count: 11001000
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
```
