### com.esri.rttest.sink.WebSocketSink

Consumes a WebSocket counting and report rate of lines received. 

#### Help

```
java -cp target/rttest.jar com.esri.rttest.mon.WebSocketSink
Missing required option: l

usage: WebSocketSink
    --help                          display help and exit
 -l,--websocket-url <arg>           [Required] Websocket URL (e.g. ws://websats.westus2.cloudapp.azure.com/websats/SatStream/subscribe)
 -n,--num-samples-no-change <arg>   Reset after number of this number of samples of no change in count; defaults to 1
 -o,--print-messages                Print Messages to stdout
 -r,--sample-rate-sec <arg>         Sample Rate Seconds; defaults to 10
 ```

 Bash Command: sinkWebSocket

#### Example


```
WSLR="wss://us-iotdev.arcgis.com/devdeer/cqvgkj9zrnkn9bcu/streams/arcgis/ws/services/devdeer_cqvg_planes1x100/StreamServer/subscribe?token=39IGHyinXJMp5Xa....8XW2qCY91MFKnq1LgmMGvO38."
./sinkWebSocket -l ${WSLR} -n 2 -r 20
```

```
websockerurl: wss://us-iotdev.arcgis.com/devdeer/cqvgkj9zrnkn9bcu/streams/arcgis/ws/services/devdeer_cqvg_planes1x100/StreamServer/subscribe?token=39IGHyinXJMp5Xa....8XW2qCY91MFKnq1LgmMGvO38.
sampleRateSec: 20
numSampleEqualBeforeExit: 2
printMessages : false
Start Count: 0
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1617741683767 |       0 |            1,400 |                  |                  |                  |
|          2 |           2 |1617741703772 |      20 |            3,400 |              100 |              100 |              100 |
|          3 |           3 |1617741723772 |      40 |            5,400 |              100 |              100 |              100 |
```


