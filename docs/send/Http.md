### com.esri.rttest.send.Http

Post lines from a file to Http Server. 

#### Help
```
./sendHttp
Missing required options: l, f, r, n

usage: Http
 -c,--content-type <arg>     Set header content type; defaults to text/plain
 -f,--file <arg>             [Required] File with lines of text to send; if a folder is specified all files in the folder are sent one at a time alphabetically
    --help                   display help and exit
 -l,--url <arg>              [Required] Post Messages to this URL
 -n,--number-to-send <arg>   [Required] Number of lines to send
 -o,--one-time               Send lines only one time. Stop when all lines have been sent.
 -p,--password <arg>         Mqtt Server Password; default no password
 -r,--rate <arg>             [Required] Desired Rate. The tool will try to send at this rate if possible
 -t,--num-threads <arg>      Number of threads to use for sending; default 1
 -u,--username <arg>         Mqtt Server Username; default no username
 -x,--x-origin <arg>         Add header for x-original-url
```

#### Example


```
$ ./sendHttp -l http://localhost:8000 -f planes.csv -r 10 -n 1000
url: http://localhost:8000
file: planes.csv
desiredRatePerSec: 10
numToSend: 1000
contentType: text/plain
numThreads: 1
reuseFile : true
username:
password:
xOriginalUrlHeader:
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
...
|                970 |                 10 |                 10 |
|                980 |                 10 |                 10 |
|                990 |                 10 |                 10 |
|               1000 |                 10 |                 10 |
Done
```
