### com.esri.rttest.mon.TcpSink

Listens for lines on a Tcp Port; counts the lines and reports rate they are received.

#### Help

```
./sinkTcp
Missing required option: p

usage: TcpSink
 -a,--auto-terminate                If count stops increasing the socket is closed; for GeoEvent use false.
    --help                          display help and exit
 -n,--num-samples-no-change <arg>   Reset after number of this number of samples of no change in count; defaults to 1
 -o,--print-messages                Print Messages to stdout
 -p,--port <arg>                    [Required] The port to listen on)
 -r,--sample-rate-sec <arg>         Sample Rate Seconds; defaults to 10

```

#### Example

```
./sinkTcp -p 8000
port: 8000
sampleRateSec: 10
numSampleEqualBeforeExit: 1
printMessages : false
autoTerminate : false
After starting this; create or restart the sending service.
Once connected you see a 'Thread Started' message for each connection.
Start Count: 10199
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1617741432521 |       0 |              300 |                  |                  |                  |
|          2 |           2 |1617741442521 |      10 |            3,300 |              300 |              300 |              300 |
|          3 |           3 |1617741452521 |      20 |            6,300 |              300 |              300 |              300 |
|          4 |           4 |1617741462521 |      30 |            9,300 |              300 |              300 |              300 |
|          5 |           5 |1617741472521 |      40 |           12,000 |              294 |              270 |              293 |

For last 10  seconds the count has not increased...
Removing sample: 50|12000
Total Count: 12,000 | Linear Regression Rate:  300 | Linear Regression Standard Error: 0.00 | Average Rate: 300

Start Count: 22199
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
```
