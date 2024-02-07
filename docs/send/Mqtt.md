### com.esri.rttest.send.Mqtt

Send Lines of a file to a Mqtt Topic

**NOTE:** When sending from my desktop the peak send rate is around 5/second.  I don't see this with other senders; however, if you send from Cloud-to-Cloud the rates are fine. 

#### Help

```
./sendMqtt
Missing required options: h, t, f, r, n

usage: Mqtt
 -c,--client-id <arg>        Client ID; default to random guid
 -e,--epoch-field <arg>      Replace specified epoch field with current epoch; specified in same way as groupField; default no epoch-field
 -f,--file <arg>             [Required] File with lines of text to send; if a folder is specified all files in the folder are sent one at a time alphabetically
 -g,--group-rate-sec <arg>   Number seconds between each time a group of lines with same groupField are sent; defaults to 1
 -h,--host <arg>             [Required] Mqtt host (e.g. tcp://mqtt.eclipse.org:1883
 -help,--help                display help and exit
 -n,--number-to-send <arg>   [Required] Number of lines to send
 -o,--one-time               Send lines only one time. Stop when all lines have been sent.
 -p,--password <arg>         Mqtt Server Password; default no password
 -q,--qos <arg>              Quality of Service: defaults to 2 => exactly once
 -r,--rate <arg>             [Required] desiredRatePerSec or groupField
                             - desiredRatePerSec: Integer value
                             - groupField: String value specified as delimiterFieldNumber or JsonPath (Examples):
                             -> ",1": comma delimited text field 1
                             -> "|3": pipe delimited text field 3
                             -> "t2": tab delimited text field 2
                             -> ".ts": json data with field at path ts
                             -> ".prop.ts": json data field at path prop.ts
 -t,--topic <arg>            [Required] Mqtt Topic
 -u,--username <arg>         Mqtt Server Username; default no username
```

#### Example

```
./sendMqtt -h tcp://velokafka.westus2.cloudapp.azure.com:31883 -t planes -f planes.csv -r 5 -n 500 -u david -p **REDACTED** 
host: tcp://velokafka.westus2.cloudapp.azure.com:31883
topic: planes
file: planes.csv
desiredRatePerSec: 5
groupField: null
numToSend: 500
username: david
password: F3qYCLY4Yp5Fceszz7Od
reuseFile : true
groupRateSec: null
qos: 2
clientId : null
epochField : null
Connected
Start Send
Use Ctrl-C to Abort.

|Number Sent         |Current Rate Per Sec|Overall Rate Per Sec|
|--------------------|--------------------|--------------------|
|                  5 |                  5 |                  5 |
|                 10 |                  5 |                  5 |
|                 15 |                  5 |                  5 |
|                 20 |                  5 |                  5 |
|                 25 |                  5 |                  5 |
|                 30 |                  5 |                  5 |
...
|                490 |                  5 |                  5 |
|                495 |                  5 |                  5 |
|                500 |                  5 |                  5 |
Done
``