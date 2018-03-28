### com.esri.rttest.sinks.WebSocketSink

$ java -cp target/rttest.jar com.esri.rttest.sinks.WebSocketSink
Usage: WebSocketSink (ws-url) [timeout] [sample-every-N-records=1000] [display-messages=false]

$ java -cp target/rttest.jar com.esri.rttest.sinks.WebSocketSink  ws://localhost:8080/websats/SatStream/subscribe
- Connects to the websocket 
- Uses default timeout-ms; Waits for data; after 10000 ms (10s) disconnect and reconnects
- The default sample rate is every 1,000 records

$ java -cp target/rttest.jar com.esri.rttest.sinks.WebSocketSink  ws://localhost:8080/websats/SatStream/subscribe 100 true
- Connects to the websocket 
- Sample rate is 100 samples (This would be better for measuring slow rates)
- Setting display-messages to true will cause the sink to just display messages
