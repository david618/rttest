### Send

Tools that send lines from a file to a service.

For example send/Elasticsearch sends lines of json from a file to Elasticsearch. 

#### Example Output

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
|                750 |                251 |                250 |
|               1000 |                250 |                250 |
|               1250 |                250 |                250 |
|               1500 |                250 |                250 |
|               1750 |                250 |                250 |
...
|               8000 |                250 |                250 |
|               8250 |                250 |                250 |
|               8500 |                250 |                250 |
|               8750 |                251 |                250 |
|               9000 |                250 |                250 |
|               9250 |                250 |                250 |
|               9500 |                250 |                250 |
|               9750 |                250 |                250 |
|              10000 |                250 |                250 |
Done
```



#### Overview

Each tool has input of desiredRate. 

The tool will try to send (desiredRate) number of lines each second.

The lines are send in a burst (as fast as hardware supports) then pauses for the reminder of that second. 

It it takes longer than a second the tool to send desiredRate of samples; the actual rate send will fall short of the desiredRate.

The actual rate it reported every desiredRate samples sent. 

#### Creating Senders

Each send tool extends the Send and implements it's abstract classes.



