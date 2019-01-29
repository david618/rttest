package com.esri.rttest.mon;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class TimescaleSqlMon {
  private static final Logger LOG = LogManager.getLogger(TimescaleSqlMon.class);

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


        if(connection == null || connection.isClosed()){
          //String url = "jdbc:postgresql://$kTimescaleHost:5432/$schema";
          Properties properties = new Properties();
          properties.put("user", "realtime");
          properties.put("password", "esri.test");
          connection = DriverManager.getConnection(connectionUrl, properties);
        }


        Statement statement = connection.createStatement();
        if(hyperTablePrefix == null || "".equalsIgnoreCase(hyperTablePrefix)) {
          //identify sub-tables of hypertable
          ResultSet chunks = statement.executeQuery("SELECT show_chunks('" + schema + "." + tableName + "')");

          if (chunks.next()){
            //chunk table name format: _hyper_[tableid]_[chunkid]_chunk
            String fullTableName = chunks.getString(1);
            String chunkTableName = fullTableName.split("\\.")[1];
            String[] parts = chunkTableName.split("_");
            StringBuilder prefix = new StringBuilder();
            prefix.append("_");
            for (int i = 0; i < parts.length - 2; i++) {
              if(!"".equalsIgnoreCase(parts[i])) {
                prefix.append(parts[i]);
                prefix.append("_");
              }
            }
            hyperTablePrefix = prefix.toString();

          }
          chunks.close();
        }

        ResultSet hyperTableCount = statement.executeQuery("SELECT sum(n_tup_ins) from pg_stat_user_tables where" +
            " relname like '" + hyperTablePrefix + "%';");

        if(hyperTableCount.next()) {
          cnt1 = hyperTableCount.getLong(1);
          t1 = System.currentTimeMillis();
        }
        hyperTableCount.close();
        statement.close();

        if (cnt2 == -1) {
          System.out.println("Watching for changes in count...  Use Ctrl-C to Exit.");
          System.out.println("|Sample Number|Epoch|Count|Linear Regression Rate|Approx. Instantaneous Rate|");
          System.out.println("|-------------|-----|-----|----------------------|--------------------------|");
        }

        if (cnt2 == -1 || cnt1 < cnt2) {
          cnt2 = cnt1;
          startCount = cnt1;
          endCount = cnt1;
          regression = new SimpleRegression();
          samples = new HashMap<>();
          numSamples = 0;

        } else if (cnt1 > cnt2) {
          // Increase number of samples
          numSamples += 1;

          // Add to Linear Regression
          regression.addData(t1, cnt1);
          samples.put(t1, cnt1);

          if (numSamples >= 2) {
            double regRate = regression.getSlope() * 1000;
            double iRate = (double) (cnt1 - cnt2) / (double) (t1 - t2) * 1000.0;
            if (sendStdout) {
              System.out.println("| " + numSamples + " | " + t1 + " | " + (cnt1 - startCount) + " | " + String.format("%.0f", regRate) + " | " + String.format("%.0f", iRate) + " |");
            }
          } else {
            System.out.println("| " + numSamples + " | " + t1 + " | " + (cnt1 - startCount) + " |           |           |");
          }

        } else if (cnt1 == cnt2 && numSamples > 0) {
          System.out.println("Count is no longer increasing...");

          endCount = cnt1;
          numSamples -= 1;

          // Remove the last sample
          regression.removeData(t2, cnt2);
          samples.remove(t2, cnt2);

          // Calculate Average Rate
          long minTime = Long.MAX_VALUE;
          long maxTime = Long.MIN_VALUE;
          long minCount = Long.MAX_VALUE;
          long maxCount = Long.MIN_VALUE;
          for (Map.Entry pair : samples.entrySet()) {
            long time = (long) pair.getKey();
            long count = (long) pair.getValue();
            if (time < minTime) {
              minTime = time;
            }
            if (time > maxTime) {
              maxTime = time;
            }
            if (count < minCount) {
              minCount = count;
            }
            if (count > maxCount) {
              maxCount = count;
            }
          }
          double avgRate = (double) (maxCount - minCount) / (double) (maxTime - minTime) * 1000.0;

          if (sendStdout) {
            System.out.println("Removing sample: " + t2 + "|" + (cnt2 - startCount));
          }
          // Output Results
          long cnt = cnt2 - startCount;
          double regRate = regression.getSlope() * 1000;  // converting from ms to seconds

          if (numSamples >= 2) {
            if (sendStdout) {
              System.out.format("Total Count: %,d | Linear Regression Rate:  %,.0f | Average Rate: %,.0f\n\n", cnt, regRate, avgRate);
            }
          } else {
            if (sendStdout) {
              System.out.format("Total Count: %,d | Not enough samples Rate calculations. \n\n", cnt);
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

      } catch (UnsupportedOperationException | JSONException | SQLException e) {
        LOG.error("ERROR", e);

      }

    }

  }

  Timer timer;
  String connectionUrl;
  String user;
  String userpw;
  String schema;
  String tableName;
  String hyperTablePrefix;
  int sampleRateSec;
  boolean sendStdout;
  Connection connection;

  public TimescaleSqlMon(String connectionUrl, String schema, String tableName, int sampleRateSec, String user, String userpw, boolean sendStdout) {

    this.connectionUrl = connectionUrl;
    this.sampleRateSec = sampleRateSec;
    this.user = user;
    this.userpw = userpw;
    this.sendStdout = sendStdout;
    this.schema  = schema;
    this.tableName = tableName;
  }

  public void run() {
    try {

      timer = new Timer();
      timer.schedule(new TimescaleSqlMon.CheckCount(), 0, sampleRateSec * 1000);

    } catch (Exception e) {
      LOG.error("ERROR", e);
    }

  }

  public static void main(String[] args) {

    String connectionUrl = "";
    String username = "";   // default to empty string
    String password = "";  // default to empty string
    String schema = "";
    String tableName = "";
    int sampleRateSec = 5; // default to 5 seconds.
    Boolean sendStdout = true;

    LOG.info("Entering application.");
    int numargs = args.length;
    if (numargs != 3 && numargs != 4 && numargs != 6) {
      System.err.print("Usage: TimescaleSqlMon [connectionUrl] [schema] [tableName] (sampleRateSec) ((username) (password))  \n");
      System.err.println("Example: java -cp target/rttest.jar com.esri.rttest.mon.TimescaleSqlMon jdbc:postgresql://HostName:5432/dbName realtime safegraph 20 user pass");
    } else {
      connectionUrl = args[0];
      schema = args[1];
      tableName = args[2];

      if (numargs >= 4) {
        sampleRateSec = Integer.parseInt(args[3]);
      }

      if (numargs == 6) {
        username = args[4];
        password = args[5];
      }

      TimescaleSqlMon t = new TimescaleSqlMon(connectionUrl, schema, tableName, sampleRateSec, username, password, sendStdout);
      t.run();

    }

  }
}
