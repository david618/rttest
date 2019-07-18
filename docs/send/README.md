### Send

Tools that send lines from a file to a service.

For example send/Elasticsearch sends lines of json from a file to Elasticsearch. 

#### Example Output

```
java -cp target/rttest.jar com.esri.rttest.send.Tcp
Usage: Tcp [serverPort] [file] [desiredRatePerSec] [numToSend] (reuseFile=true)
Djennings1:rttest davi5017$ java -cp target/rttest.jar com.esri.rttest.send.Tcp localhost:8000 planes.csv 10 1000
localhost:8000
ip: localhost
port:8000
path:
protocol:http
WARNING: An illegal reflective access operation has occurred
WARNING: Illegal reflective access by org.xbill.DNS.ResolverConfig (file:/Users/davi5017/github/rttest/target/lib/dnsjava-2.1.8.jar) to method sun.net.dns.ResolverConfiguration.open()
WARNING: Please consider reporting this to the maintainers of org.xbill.DNS.ResolverConfig
WARNING: Use --illegal-access=warn to enable warnings of further illegal reflective access operations
WARNING: All illegal access operations will be denied in a future release
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

```
The output includes a Github formated table.

|Number Sent         |Current Rate Per Sec|Overall Rate Per Sec|
|--------------------|--------------------|--------------------|
|                 10 |                 10 |                 10 |
|                 20 |                 10 |                 10 |
|                 30 |                 10 |                 10 |
|                 40 |                 10 |                 10 |
|                 50 |                 10 |                 10 |
|                 60 |                 10 |                 10 |
|                 70 |                 10 |                 10 |

Fields
- Number Sent: Number of messages Sent
- Current Rate Per Sec: The rate between this sample and previous
- Overall Rate Per Sec: The rate from this sample and beginning

#### Overview

Each tool takes a requestedRatePerSecond. 

The tool will send that many sample in a burst; then wait for the remainder of the second before sending next batch.

The tool monitors the actual overall rate and adjusts the wait time to try and achieve the requested rate.

Because of limitations in network bandwidth or other hardware the sender may not be able to achieve the requested rate.

In some cases it may be possible to get higher rates by running mulitple instances of a tool.


#### Creating Senders

Each send tool extends the Send and implements it's abstract classes.



