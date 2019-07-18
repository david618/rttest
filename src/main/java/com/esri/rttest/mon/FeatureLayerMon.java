package com.esri.rttest.mon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class FeatureLayerMon extends  Monitor {
    private static final Logger LOG = LogManager.getLogger(FeatureLayerMon.class);


    String featureLayerURL;
    boolean sendStdout;

    public FeatureLayerMon(String featureLayerURL, int sampleRateSec, int numSampleEqualBeforeExit) {
        this.featureLayerURL = featureLayerURL;

        this.sampleRateSec = sampleRateSec;
        this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;


    }

    @Override
    public void countEnded() {

    }

    @Override
    public Sample getSample() {

        LOG.info("Checking Count");

        long cnt = -1L;
        long ts = 0L;
        JSONObject json = new JSONObject();

        try {

            String url = featureLayerURL + "/query?where=1%3D1&returnCountOnly=true&f=json";
            json = httpQuery(url, "", "");


            cnt = json.getInt("count");

        } catch (JSONException e) {
            cnt = -1;
            try {
                String errorMessage = json.getJSONObject("error").getString("message");
                System.out.println(errorMessage);
            } catch (Exception e2 ) {
                System.out.println("feature layer may not exist");
            }
        }

        ts = System.currentTimeMillis();

        return new Sample(cnt, ts);
    }


    public static void main(String[] args) {



        String url = "";
        int sampleRateSec = 5; // default to 5 seconds.
        int numSampleEqualBeforeExit = 1;


        LOG.info("Entering application.");
        int numargs = args.length;
        if (numargs < 1) {
            System.err.print("Usage: FeatureLayerMon [featureLayerUrl] (sampleRateSec=10) (numSampleEqualBeforeExit=1)\n");
            System.err.println("Example: FeatureLayerMon http://p1/arcgis/rest/services/planes/FeatureServer/0 10");
            System.err.println("");
            System.err.println("featureLayerUrl: URL to Feature Layer");
            System.err.println("sampleRateSec: Will gather a sample every N seconds for linear regression and estimation of rate.");
            System.err.println("numSampleEqualBeforeExit: Number of samples that are equal before exit");
            System.err.println("");

        } else {
            url = args[0];
            if (numargs > 1) {
                sampleRateSec = Integer.parseInt(args[1]);
            }
            if (numargs > 2) {
                numSampleEqualBeforeExit = Integer.parseInt(args[2]);
                if (numSampleEqualBeforeExit < 1) {
                    System.err.println("numSampleEqualBeforeExit must be greater than 1");
                    System.exit(2);
                }
            }

            FeatureLayerMon t = new FeatureLayerMon(url, sampleRateSec, numSampleEqualBeforeExit);
            t.run();
        }

    }

}
