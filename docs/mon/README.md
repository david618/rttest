### Mon (Montiors)

Two varieties of monitors counters and sinks.

- Counters: Periodically get a count from a source (e.g. Count From FeatureLayerMon)
- Sinks: Listens (e.g. TCP Port) and counts messages received on that port

These both output results in a Github formatted table.

For example

```
java -cp target/rttest.jar com.esri.rttest.mon.TcpSink 8000
After starting this; create or restart the sending service.
Once connected you see a 'Thread Started' message for each connection.
Start Count: 9
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1563310709188 |       0 |              100 |                  |                  |                  |
|          2 |           2 |1563310719192 |      10 |              200 |               10 |               10 |               10 |
|          3 |           3 |1563310729195 |      20 |              300 |               10 |               10 |               10 |
|          4 |           4 |1563310739199 |      30 |              410 |               10 |               11 |               10 |
|          5 |           5 |1563310749202 |      40 |              510 |               10 |               10 |               10 |
|          6 |           6 |1563310759206 |      50 |              610 |               10 |               10 |               10 |
|          7 |           7 |1563310769210 |      60 |              710 |               10 |               10 |               10 |
|          8 |           8 |1563310779214 |      70 |              810 |               10 |               10 |               10 |
|          9 |           9 |1563310789219 |      80 |              910 |               10 |               10 |               10 |
|         10 |          10 |1563310799222 |      90 |              990 |               10 |                8 |               10 |

For last 10  seconds the count has not increased...
Removing sample: 100|990
Total Count: 990 | Linear Regression Rate:  10 | Linear Regression Standard Error: 0.00 | Average Rate: 10

Start Count: 999
```

You can copy just the table and it will look like this in GitHub.



|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1563310709188 |       0 |              100 |                  |                  |                  |
|          2 |           2 |1563310719192 |      10 |              200 |               10 |               10 |               10 |
|          3 |           3 |1563310729195 |      20 |              300 |               10 |               10 |               10 |
|          4 |           4 |1563310739199 |      30 |              410 |               10 |               11 |               10 |
|          5 |           5 |1563310749202 |      40 |              510 |               10 |               10 |               10 |
|          6 |           6 |1563310759206 |      50 |              610 |               10 |               10 |               10 |
|          7 |           7 |1563310769210 |      60 |              710 |               10 |               10 |               10 |
|          8 |           8 |1563310779214 |      70 |              810 |               10 |               10 |               10 |
|          9 |           9 |1563310789219 |      80 |              910 |               10 |               10 |               10 |
|         10 |          10 |1563310799222 |      90 |              990 |               10 |                8 |               10 |

#### Fields

- Query Number: One up counter for queries.
- Sample Number: Samples used for Linear Regression. Only Query that show increasing counts.
- Epoch (ms):  System time in number of milliseconds from Epoch
- Time (s): Number of seconds from when counts increased.
- Count: Total number of messages counted.
- Linear Regression Rate: This is a best line to the data.
- Rate from previous: This is the rate from this query to the previous query.
- Rate from first: This is the rate from the first query until this query.

If the rate is approximately linear the Linear Regression rate and the Rate from First will be about equal.

The [page](../AverageVsLinearRegression) explains the differnce between Average and Linear Regression.


#### Creating Monitors

Each Sink or Monitor extends the Monitor implements it's abstract classes.

