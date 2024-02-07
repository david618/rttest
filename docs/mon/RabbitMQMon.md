### com.esri.rttest.mon.RabbitMQMon

Monitors a Rabbit MQ queue and measures and reports rate of change in count.  

Use **Ctrl-C** to stop.

#### Help

Bash Command: monRabbitMQ

```
./monRabbitMQ 
Missing required options: h, q

usage: RabbitMQMon
 -h,--host <arg>                    [Required] RabbitMQ Host (e.g. rabbitmq.host.name)
    --help                          display help and exit
 -n,--num-samples-no-change <arg>   Reset after number of this number of samples of no change in count; defaults to 1
 -o,--print-messages                Print Messages to stdout
 -P,--port <arg>                    port default tos 5672
 -p,--password <arg>                RabbitMQ Password; default no password
 -q,--queue <arg>                   [Required] RabbitMQ queue
 -r,--sample-rate-sec <arg>         Sample Rate Seconds; defaults to 10
 -u,--username <arg>                RabbitMQ Username; default no username
 ```

#### Example

```
 ./monRabbitMQ -h tf-lb-20240109223247744700000001-aeeaca5c9a058db4.elb.us-west-2.amazonaws.com -q planes -r 10 -n 3 -u admin -p **REDACTED**
host: tf-lb-20240109223247744700000001-aeeaca5c9a058db4.elb.us-west-2.amazonaws.com
Port: 5672
queue: planes
sampleRateSec: 10
numSampleEqualBeforeExit: 3
username: admin
password: password
printMessages : false
Start Count: 0
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1707267333597 |       0 |               70 |                  |                  |                  |
|          2 |           2 |1707267343597 |      10 |              170 |               10 |               10 |               10 |
|          3 |           3 |1707267353598 |      20 |              270 |               10 |               10 |               10 |
|          4 |           4 |1707267363602 |      30 |              370 |               10 |               10 |               10 |
|          5 |           5 |1707267373604 |      40 |              470 |               10 |               10 |               10 |
|          6 |           6 |1707267383605 |      50 |              570 |               10 |               10 |               10 |
|          7 |           7 |1707267393606 |      60 |              670 |               10 |               10 |               10 |
|          8 |           8 |1707267403607 |      70 |              770 |               10 |               10 |               10 |
|          9 |           9 |1707267413608 |      80 |              870 |               10 |               10 |               10 |
|         10 |          10 |1707267423612 |      90 |              970 |               10 |               10 |               10 |
|         11 |          11 |1707267433612 |     100 |            1,000 |               10 |                3 |                9 |
|         12 |       ***** |1707267443612 |     110 |            1,000 |               10 |                0 |                8 |
|         13 |       ***** |1707267453616 |     120 |            1,000 |               10 |                0 |                8 |

For last 30  seconds the count has not increased...
Removing sample: 130|1000
Total Count: 1,000 | Linear Regression Rate:  10 | Linear Regression Standard Error: 0.00 | Average Rate: 10

Start Count: 1000
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|

```