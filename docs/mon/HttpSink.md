### com.esri.rttest.mon.HttpSink

Listen for posts; count and output rates. 

#### Help

```
./sinkHttp
Missing required option: p

usage: HttpSink
    --help                          display help and exit
 -n,--num-samples-no-change <arg>   Reset after number of this number of samples of no change in count; defaults to 1
 -o,--print-messages                Print Messages to stdout
 -p,--port <arg>                    [Required] The port to listen on)
 -r,--sample-rate-sec <arg>         Sample Rate Seconds; defaults to 10
 ```

#### Example

```
./sinkHttp -p 8000
port: 8000
sampleRateSec: 10
numSampleEqualBeforeExit: 1
printMessages : false
After starting this; create or restart the sending service.
Once connected you see a 'Thread Started' message for each connection.
Start Count: 0
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1617742223472 |       0 |               50 |                  |                  |                  |
|          2 |           2 |1617742233473 |      10 |              150 |               10 |               10 |               10 |
|          3 |           3 |1617742243477 |      20 |              250 |               10 |               10 |               10 |
|          4 |           4 |1617742253479 |      30 |              350 |               10 |               10 |               10 |
|          5 |           5 |1617742263482 |      40 |              450 |               10 |               10 |               10 |
|          6 |           6 |1617742273485 |      50 |              550 |               10 |               10 |               10 |
|          7 |           7 |1617742283489 |      60 |              650 |               10 |               10 |               10 |
|          8 |           8 |1617742293493 |      70 |              750 |               10 |               10 |               10 |
|          9 |           9 |1617742303493 |      80 |              850 |               10 |               10 |               10 |
|         10 |          10 |1617742313493 |      90 |              950 |               10 |               10 |               10 |
|         11 |          11 |1617742323497 |     100 |            1,000 |               10 |                5 |                9 |

For last 10  seconds the count has not increased...
Removing sample: 110|1000
Total Count: 1,000 | Linear Regression Rate:  10 | Linear Regression Standard Error: 0.00 | Average Rate: 10

Start Count: 1000
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
```
