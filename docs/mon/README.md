### Mon (Montiors)

#### Overview

- When the tool starts it gets the current count and starts sampling count every sampleRateSec seconds (defaults to 5 seconds).
- When count changes the tool starts collecting sample points. 
- Starting with Second Sample rates are output.
- After count stops changing the final line will give the count received and the best fit linear approximation of the rate.  The last sample is excluded from the final rate calculation.
- After reporting the final count and rate the tool will continue monitoring for count changes.  

Two varieties of monitors and sinks.

- Monitors: Periodically get a count from a source (e.g. Count From FeatureLayerMon)
- Sinks: Listens (e.g. TCP Port) and counts messages received on that port

These both output results in a Github formatted table.

#### Example

```
./monElastic -l http://datastore-elasticsearch-client.a4iot-cqvgkj9zrnkn9bcu-services:9200/planes -n 1 -r 10
url: http://datastore-elasticsearch-client.a4iot-cqvgkj9zrnkn9bcu-services:9200/planes
sampleRateSec: 10
numSampleEqualBeforeExit: 1
username:
password:
Start Count: 1280
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1617739120901 |       0 |            1,000 |                  |                  |                  |
|          2 |           2 |1617739130903 |      10 |            2,000 |              100 |              100 |              100 |
|          3 |           3 |1617739141013 |      20 |            3,000 |               99 |               99 |               99 |
|          4 |           4 |1617739150900 |      29 |            4,000 |              100 |              101 |              100 |
|          5 |           5 |1617739160901 |      40 |            4,900 |               98 |               90 |               98 |
|          6 |           6 |1617739170916 |      50 |            6,000 |               99 |              110 |              100 |
|          7 |           7 |1617739180917 |      60 |            7,000 |              100 |              100 |              100 |
|          8 |           8 |1617739190902 |      70 |            8,000 |              100 |              100 |              100 |
|          9 |           9 |1617739200906 |      80 |            8,900 |               99 |               90 |               99 |
|         10 |          10 |1617739210905 |      90 |           10,000 |              100 |              110 |              100 |

For last 10  seconds the count has not increased...
Removing sample: 100|10000
Total Count: 10,000 | Linear Regression Rate:  99 | Linear Regression Standard Error: 0.00 | Average Rate: 99

Start Count: 11280
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
```

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

