### com.esri.rttest.send.RabbitMQ

Send Lines of a file to a RabbitMQ Queue

#### Help

```
./sendRabbitMQ
Missing required options: h, q, f, r, n

usage: RabbitMQ
 -c,--client-id <arg>        Client ID; default to random guid
 -e,--epoch-field <arg>      Replace specified epoch field with current epoch; specified in same way as groupField; default no epoch-field
 -f,--file <arg>             [Required] File with lines of text to send; if a folder is specified all files in the folder are sent one at a time alphabetically
 -g,--group-rate-sec <arg>   Number seconds between each time a group of lines with same groupField are sent; defaults to 1
 -h,--host <arg>             [Required] RabbitMQ host (e.g. rabbitmq.host.name
 -help,--help                display help and exit
 -n,--number-to-send <arg>   [Required] Number of lines to send
 -o,--one-time               Send lines only one time. Stop when all lines have been sent.
 -p,--password <arg>         RabbitMQ Server Password; default no password
 -P,--port <arg>             port default tos 5672
 -q,--queue <arg>            [Required] RabbitMQ Queue
 -r,--rate <arg>             [Required] desiredRatePerSec or groupField
                             - desiredRatePerSec: Integer value
                             - groupField: String value specified as delimiterFieldNumber or JsonPath (Examples):
                             -> ",1": comma delimited text field 1
                             -> "|3": pipe delimited text field 3
                             -> "t2": tab delimited text field 2
                             -> ".ts": json data with field at path ts
                             -> ".prop.ts": json data field at path prop.ts
 -u,--username <arg>         RabbitMQ Server Username; default no username
```

#### Example

```
./sendRabbitMQ -h tf-lb-20240109223247744700000001-aeeaca5c9a058db4.elb.us-west-2.amazonaws.com  -f planes.csv -q planes -r 10 -n 1000 -u admin -p **REDACTED**
host: tf-lb-20240109223247744700000001-aeeaca5c9a058db4.elb.us-west-2.amazonaws.com
queue: planes
file: planes.csv
desiredRatePerSec: 10
groupField: null
numToSend: 1000
username: admin
password: password
reuseFile : true
groupRateSec: null
Port: 5672
clientId : null
epochField : null
Connected
Start Send
Use Ctrl-C to Abort.

|Number Sent         |Current Rate Per Sec|Overall Rate Per Sec|
|--------------------|--------------------|--------------------|
|                 10 |                 10 |                 10 |
|                 20 |                 10 |                 10 |
|                 30 |                 10 |                 10 |
|                 40 |                 10 |                 10 |
|                 50 |                 10 |                 10 |
|                 60 |                 10 |                 10 |
|                 70 |                 10 |                 10 |
|                 80 |                 10 |                 10 |
|                 90 |                 10 |                 10 |
|                100 |                 10 |                 10 |
|                110 |                 10 |                 10 |
|                120 |                 10 |                 10 |
...
|                980 |                 10 |                 10 |
|                990 |                 10 |                 10 |
|               1000 |                 10 |                 10 |
Done
``