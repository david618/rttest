### com.esri.rttest.sinks.WebSocketSink

```
java -cp target/rttest.jar com.esri.rttest.sinks.WebSocketSink
```
Usage: WebSocketSink (wsUrl) [(sample-rate-sec=5) (printMessages=false)]

```
java -cp target/rttest.jar com.esri.rttest.mon.WebSocketSink ws://websats.westus2.cloudapp.azure.com/websats/SatStream/subscribe 
```

Output will look like:

```
Watching for changes in count...  Use Ctrl-C to Exit.
|Sample Number|Epoch|Count|Linear Regression Rate|Approx. Instantaneous Rate|
|-------------|-----|-----|----------------------|--------------------------|
| 1 | 1551736479117 | 3670 |           |           |
| 2 | 1551736484118 | 7340 | 734 | 734 |
| 3 | 1551736489118 | 10511 | 684 | 634 |
| 4 | 1551736494122 | 14680 | 724 | 833 |
| 5 | 1551736499126 | 18350 | 734 | 733 |
| 6 | 1551736504130 | 22020 | 736 | 733 |
| 7 | 1551736509134 | 25690 | 737 | 733 |
| 8 | 1551736514138 | 27158 | 700 | 293 |
Count is no longer increasing...
Removing sample: 1551736514138|27158
Total Count: 27,158 | Linear Regression Rate:  737 | Average Rate: 734
```

