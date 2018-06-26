/*
 * (C) Copyright 2017 David Jennings
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     David Jennings
 */
/**
 * Monitors an Solr Collection.
 * Periodically does a count and when count is changing collects samples.
 * After three samples are made outputs rates based on linear regression.
 * After counts stop changing outputs the final rate and last estimated rate.
 *
 * Creator: David Jennings
 */
package com.esri.rttest.mon;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class CassandraMon
{

    private static final Logger LOG = LogManager.getLogger(CassandraMon.class);

    class CheckCount extends TimerTask {

        long cnt1;
        long cnt2;
        long startCount;
        long endCount;
        int numSamples;
        HashMap<Long, Long> samples;
        long t1;
        long t2;
        SimpleRegression regression;

        public CheckCount() {
            regression = new SimpleRegression();
            cnt1 = 0;
            cnt2 = -1;
            startCount = 0;
            numSamples = 0;
            t1 = 0L;
            t2 = 0L;
            samples = null;
        }

        boolean inCounting() {
            if (cnt1 > 0) {
                return true;
            } else {
                return false;
            }
        }

        HashMap<Long, Long> getSamples() {
            return samples;
        }

        long getStartCount() {
            return startCount;
        }

        long getEndCount() {
            return endCount;
        }

        @Override
        public void run() {
            try {

                LOG.info("Checking Count");

                // connect to the cassandra cluster and get the count
                Cluster cluster = null;
                try {
                    cluster = Cluster.builder()
                        .addContactPoint(cassandraHost)
                        .build();
                    Session session = cluster.connect();

                    ResultSet rs = session.execute("SELECT COUNT(*) as COUNT FROM " + keyspace + "." + tableName + "");
                    Row row = rs.one();
                    cnt1 = row.getLong("COUNT");
                } finally {
                    if (cluster != null)
                        cluster.close();                                          // (5)
                }

                t1 = System.currentTimeMillis();

                if (cnt2 == -1 || cnt1 < cnt2) {
                    cnt2 = cnt1;
                    startCount = cnt1;
                    endCount = cnt1;
                    regression = new SimpleRegression();
                    samples = new HashMap<>();
                    numSamples = 0;

                } else if (cnt1 > cnt2) {
                    // Add to Linear Regression
                    regression.addData(t1, cnt1);
                    samples.put(t1, cnt1);
                    // Increase number of samples
                    numSamples += 1;
                    if (numSamples > 2) {
                        double rcvRate = regression.getSlope() * 1000;
                        System.out.format("%d,%d,%d,%.0f\n", numSamples, t1, cnt1, rcvRate);
                    } else {
                        System.out.format("%d,%d,%d\n", numSamples, t1, cnt1);
                    }

                } else if (cnt1 == cnt2 && numSamples > 0) {
                    numSamples -= 1;
                    // Remove the last sample
                    regression.removeData(t2, cnt2);
                    samples.remove(t2, cnt2);
                    if (sendStdout) {
                        System.out.println("Removing: " + t2 + "," + cnt2);
                    }
                    // Output Results
                    long cnt = cnt2 - startCount;
                    double rcvRate = regression.getSlope() * 1000;  // converting from ms to seconds

                    if (numSamples > 5) {
                        double rateStdErr = regression.getSlopeStdErr();
                        if (sendStdout) {
                            System.out.format("%d , %.2f, %.4f\n", cnt, rcvRate, rateStdErr);
                        }
                    } else if (numSamples >= 2) {
                        if (sendStdout) {
                            System.out.format("%d , %.2f\n", cnt, rcvRate);
                        }
                    } else {
                        if (sendStdout) {
                            System.out.println("Not enough samples to calculate rate. ");
                        }
                    }

                    // Reset
                    cnt1 = -1;
                    cnt2 = -1;
                    t1 = 0L;
                    t2 = 0L;

                }

                cnt2 = cnt1;
                t2 = t1;

            } catch (Exception error) {
                LOG.error("ERROR", error);
            }

        }

    }

    Timer timer;
    String cassandraHost;
    String keyspace;
    String tableName;
    String user;
    String userpw;
    int sampleRateSec;
    boolean sendStdout;

    public CassandraMon(String cassandraHost, String keyspace, String tableName, int sampleRateSec, String user, String userpw, boolean sendStdout) {

        this.cassandraHost = cassandraHost;
        this.keyspace = keyspace;
        this.tableName = tableName;
        this.sampleRateSec = sampleRateSec;
        this.user = user;
        this.userpw = userpw;
        this.sendStdout = sendStdout;
    }

    public void run() {
        try {

            timer = new Timer();
            timer.schedule(new CassandraMon.CheckCount(), 0, sampleRateSec * 1000);

        } catch (Exception e) {
            LOG.error("ERROR", e);
        }

    }

    public static void main(String[] args) {

        String cassandraHost = "";
        String keyspace = "";
        String tableName = "";
        String username = "";   // default to empty string
        String password = "";  // default to empty string
        int sampleRateSec = 5; // default to 5 seconds.  
        Boolean sendStdout = true;

        LOG.info("Entering application.");
        int numargs = args.length;
        if (numargs != 3 && numargs != 4 &&numargs != 6) {
            System.err.print("Usage: CassandraMon [CassandraHost] [Keyspace] [TableName] (sampleRateSec) ((username) (password))  \n");
            System.err.println("Example: java -cp target/rttest.jar com.esri.rttest.mon.CassandraMon localhost realtime safegraph 20 20 user pass");
        } else {
            cassandraHost = args[0];
            keyspace = args[1];
            tableName = args[2];

            if (numargs >= 4) {
                sampleRateSec = Integer.parseInt(args[3]);
            }

            if (numargs == 6) {
                username = args[4];
                password = args[5];
            }

            CassandraMon t = new CassandraMon(cassandraHost, keyspace, tableName, sampleRateSec, username, password, sendStdout);
            t.run();

        }

    }
}
