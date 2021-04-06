### com.esri.rttest.mon.FeatureLayerMon

Monitors an Esri Feature Layer count and measures and reports rate of change in count.

#### Help

Bash Command: monFeatureLayer

```
./monFeatureLayer
Missing required option: l

usage: FeatureLayerMon
    --help                          display help and exit
 -l,--feature-layer-url <arg>       [Required] Feature Layer URL
 -n,--num-samples-no-change <arg>   Reset after number of this number of samples of no change in count; defaults to 1
 -r,--sample-rate-sec <arg>         Sample Rate Seconds; defaults to 10
 -t,--token <arg>                   Esri Token; defaults to empty string
```

#### Example

```
FLYR=https://us-iotdev.arcgis.com/devdeer/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services/devdeer_cqvg_planes1x500_lyr/FeatureServer/0
TOKEN=$(cat ~/.itemctl/devext_publisher.token)
./monFeatureLayer -l ${FLYR} -n 3 -r 60 -t ${TOKEN}
```

```
url: https://us-iotdev.arcgis.com/devdeer/cqvgkj9zrnkn9bcu/maps/arcgis/rest/services/devdeer_cqvg_planes1x500_lyr/FeatureServer/0
sampleRateSec: 60
numSampleEqualBeforeExit: 3
token: -ef8Ni_-57J1O83ERzhoBYp-8vGWuprQfwkysmm9Oi82q8rQOo5Zjt4ueBnT-0lmYwNJUjzIiAaozYES7eKCCmexiOkNX_TFvDASa7FFtxnROESV-uzvSZBVdi9hR_lpsrBRYfGFpL71Ggzmv8GvZEIUckUCUqtgr0Y3Pwm0Zqy_ewTKjjyxGW5Wayw7b1nEqv8_9wFX3cRTsHvAXpyqFC7Z_dxOK_2kd_6L0_D7kxs.
Start Count: 10881050
Watching for changes in count...  Use Ctrl-C to Exit.

|Query Number|Sample Number|Epoch (ms)    |Time (s) |Count             |Linear Reg. Rate  |Rate From Previous|Rate From First   |
|------------|-------------|--------------|---------|------------------|------------------|------------------|------------------|
|          1 |           1 |1617740721301 |       0 |           29,266 |                  |                  |                  |
|          2 |           2 |1617740781294 |      59 |           59,139 |              498 |              498 |              498 |
|          3 |           3 |1617740841299 |     119 |           89,117 |              499 |              500 |              499 |
|          4 |           4 |1617740901290 |     179 |          118,732 |              497 |              494 |              497 |
```

Use **Ctrl-C** to stop.

Samples the FeatureLayer count every 60 seconds. If the count doesn't change for 3 times in a row; output summary and reset. 

