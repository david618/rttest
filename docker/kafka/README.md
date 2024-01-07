# Running Kafka Locally for Testing

## bitnami charts

https://github.com/bitnami/containers/blob/main/bitnami/kafka/README.md


## Starting Kafka

```
docker-compose up -d
```

## Sending to Kafka

```
./sendKafka -b localhost:9094 -f planes.csv -n -1 -r 1000 -t planes
```

```
broker: localhost:9094
topic: planes
file: planes.csv
desiredRatePerSec: 1000
numToSend: -1
reuseFile : true
username :
password :
truststore :
Start Send
Use Ctrl-C to Abort.

|Number Sent         |Current Rate Per Sec|Overall Rate Per Sec|
|--------------------|--------------------|--------------------|
|               1000 |                996 |                996 |
|               2000 |              1,000 |                997 |
|               3000 |              1,002 |                998 |
|               4000 |              1,002 |                999 |
...
```



## Monitoring Kafka


```
./monKafka  -b localhost:9094 -n 3 -t planes
```

```
broker: localhost:9094
topic: planes
sampleRateSec: 10
numSampleEqualBeforeExit: 3
username :
password :
truststore :
Start Count: 133480
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1704665859429 |       0 |           10,000 |                  |                  |                  |
|          2 |           2 |1704665869422 |       9 |           20,000 |            1,001 |            1,001 |            1,001 |
|          3 |           3 |1704665879421 |      19 |           30,000 |            1,000 |            1,000 |            1,000 |
...
```

## Stopping Sender and Monitor

Ctrl-C on sender.

The monitor will report that no new messages are coming in; after three samples monitor will reset.

```
...
|          3 |           3 |1704665879421 |      19 |           30,000 |            1,000 |            1,000 |            1,000 |
|          4 |           4 |1704665889424 |      29 |           40,000 |            1,000 |            1,000 |            1,000 |
|          5 |           5 |1704665899425 |      39 |           50,000 |            1,000 |            1,000 |            1,000 |
|          6 |           6 |1704665909424 |      49 |           60,000 |            1,000 |            1,000 |            1,000 |
|          7 |           7 |1704665919427 |      59 |           67,000 |              968 |              700 |              950 |
|          8 |       ***** |1704665929428 |      69 |           67,000 |              968 |                0 |              814 |
|          9 |       ***** |1704665939428 |      79 |           67,000 |              968 |                0 |              713 |

For last 30  seconds the count has not increased...
Removing sample: 89|67000
Total Count: 67,000 | Linear Regression Rate:  1,000 | Linear Regression Standard Error: 0.00 | Average Rate: 1,000

Start Count: 200480
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
```

## Stop Compose

```
docker-compose down
```
