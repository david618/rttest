### com.esri.rttest.monitors.FeatureLayerMon

<pre>
$ java -cp rttest-big.jar com.esri.rttest.monitors.FeatureLayerMon 
Usage: FeatureLayerMon &lt;Feature-Layer&gt; (&lt;Seconds-Between-Samples&gt; Default 5 seconds)  
</pre>

Example:

<pre>
$ java -cp Simulator.jar com.esri.simulator.FeatureLayerMon http://dj52web.westus.cloudapp.azure.com/arcgis/rest/services/Hosted/FAA-Stream/FeatureServer/0
</pre>

- The code counts the number of features from the Feature-Layer
- If no count change is detected it will wait
- Each time change is detected a sample is added and output to the screen
- After count stops increasing; least-square fit is used to calculate the rate of change 
- Results are printed to the screen
