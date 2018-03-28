### com.esri.rttest.mon.FeatureLayerMon

After Started
- Gets a count for the Feature-Layer
- When count changes a sample is collected
- After three samples a best fit rate is also displayed
- After count stops increasing; last sample removed and final count and best fit rate are displayed
- The app continues to watch for count changes.  Use **Ctrl-C** to stop.



<pre>
$ java -cp rttest-big.jar com.esri.rttest.mon.FeatureLayerMon 
Usage: FeatureLayerMon (Feature-Layer) [Seconds-Between-Samples=5]  
</pre>

Examples:

<pre>
$ java -cp Simulator.jar com.esri.rttest.mon.FeatureLayerMon http://dj52web.westus.cloudapp.azure.com/arcgis/rest/services/Hosted/FAA-Stream/FeatureServer/0
</pre>


<pre>
java -cp target/rttest.jar com.esri.rttest.mon.FeatureLayerMon http://localhost/bbc0398c-d19e-493c-aefc-c382d2eb1c05/arcgis/rest/services/planes-bat/FeatureServer/0  60
</pre>

Seconds-Between-Samples is 60.

Example Output:

<pre>
1,1518064100919,2100979
2,1518064221266,5601063
3,1518064341522,8162436,25193
4,1518064461598,10017645,21886
Removing: 1518064461598,10017645
10017645 , 25193.26
</pre>

- Sample Rows: Sample Number, System Time Milliseconds, Current Count, (Rate)
- Final Line: Number Samples, Rate 



To turn restore refresh rate for index for planes-bat index running on DC/OS elasticsearch named sats-ds01.

<pre>
curl -u elastic:changeme -XPUT data.sats-ds01.l4lb.thisdcos.directory:9200/planes-bat/_settings -H 'Content-Type: application/json' -d'
{
    "index" : {
        "refresh_interval" : "1s"
    }
}
'</pre>
