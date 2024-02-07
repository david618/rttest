### com.esri.rttest.mon.MqttMon

Monitors a MQTT Topic and measures and reports rate of change in count.  

Use **Ctrl-C** to stop.

#### Help

Bash Command: monMqtt

```
./monMqtt 
Missing required options: h, t

usage: MqttMon
 -h,--host <arg>                    [Required] Mqtt Host (e.g. tcp://52.191.131.159:1883)
    --help                          display help and exit
 -n,--num-samples-no-change <arg>   Reset after number of this number of samples of no change in count; defaults to 1
 -o,--print-messages                Print Messages to stdout
 -p,--password <arg>                Mqtt Server Password; default no password
 -r,--sample-rate-sec <arg>         Sample Rate Seconds; defaults to 10
 -t,--topic <arg>                   [Required] Kafka Topic
 -u,--username <arg>                Mqtt Server Username; default no username
 ```

#### Example

```
./monMqtt -h tcp://velokafka.westus2.cloudapp.azure.com:31883 -u david -p **REDACTED**  -t planes
host: tcp://velokafka.westus2.cloudapp.azure.com:31883
topic: planes
sampleRateSec: 10
numSampleEqualBeforeExit: 1
username: david
password: F3qYCLY4Yp5Fceszz7Od
printMessages : false
Connecting to host: tcp://velokafka.westus2.cloudapp.azure.com:31883
Connected
Start Count: 0
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1707267864366 |       0 |                2 |                  |                  |                  |
|          2 |           2 |1707267874366 |      10 |               52 |                5 |                5 |                5 |
|          3 |           3 |1707267884367 |      20 |              102 |                5 |                5 |                5 |
|          4 |           4 |1707267894372 |      30 |              152 |                5 |                5 |                5 |
|          5 |           5 |1707267904376 |      40 |              202 |                5 |                5 |                5 |
|          6 |           6 |1707267914379 |      50 |              252 |                5 |                5 |                5 |
|          7 |           7 |1707267924382 |      60 |              302 |                5 |                5 |                5 |
|          8 |           8 |1707267934384 |      70 |              352 |                5 |                5 |                5 |
|          9 |           9 |1707267944389 |      80 |              402 |                5 |                5 |                5 |
|         10 |          10 |1707267954393 |      90 |              452 |                5 |                5 |                5 |
|         11 |          11 |1707267964397 |     100 |              500 |                5 |                5 |                5 |

For last 10  seconds the count has not increased...
Removing sample: 110|500
Total Count: 500 | Linear Regression Rate:  5 | Linear Regression Standard Error: 0.00 | Average Rate: 5

Start Count: 1241
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
```