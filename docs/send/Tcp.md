### com.esri.rttest.send.Tcp

Sends lines from a file to a Tcp Server.

#### Help

```
./sendTcp
Missing required options: h, f, r, n

usage: Tcp
 -f,--file <arg>             [Required] File with lines of text to send; if a folder is specified all files in the folder are sent one at a time alphabetically
 -h,--server-port <arg>      [Required] TCP Server:Port
    --help                   display help and exit
 -n,--number-to-send <arg>   [Required] Number of lines to send
 -o,--one-time               Send lines only one time. Stop when all lines have been sent.
 -r,--rate <arg>             [Required] Desired Rate. The tool will try to send at this rate if possible
```


### Example
```
$ ./sendTcp -f planes.csv -h localhost:8000 -n 12000 -r 300

file: planes.csv
desiredRatePerSec: 300
numToSend: 12000
reuseFile : true
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
|                300 |                299 |                299 |
|                600 |                300 |                299 |
|                900 |                301 |                299 |
|               1200 |                300 |                300 |
|               1500 |                300 |                300 |
|               1800 |                300 |                300 |
...
|              10500 |                299 |                300 |
|              10800 |                299 |                300 |
|              11100 |                300 |                300 |
|              11400 |                300 |                300 |
|              11700 |                300 |                300 |
|              12000 |                300 |                300 |
Done


```


