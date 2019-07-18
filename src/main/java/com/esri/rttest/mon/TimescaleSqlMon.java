package com.esri.rttest.mon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;


public class TimescaleSqlMon extends Monitor {
  private static final Logger LOG = LogManager.getLogger(TimescaleSqlMon.class);

  @Override
  public void countEnded() {

  }

  @Override
  public Sample getSample() {
    long cnt = -1L;
    long ts = 0L;

    try {
      if (connection == null || connection.isClosed()) {
        //String url = "jdbc:postgresql://$kTimescaleHost:5432/$schema";
        Properties properties = new Properties();
        properties.put("user", "realtime");
        properties.put("password", "esri.test");
        connection = DriverManager.getConnection(connectionUrl, properties);
      }


      Statement statement = connection.createStatement();
      if (hyperTablePrefix == null || "".equalsIgnoreCase(hyperTablePrefix)) {
        //identify sub-tables of hypertable
        ResultSet chunks = statement.executeQuery("SELECT show_chunks('" + schema + "." + tableName + "')");

        if (chunks.next()) {
          //chunk table name format: _hyper_[tableid]_[chunkid]_chunk
          String fullTableName = chunks.getString(1);
          String chunkTableName = fullTableName.split("\\.")[1];
          String[] parts = chunkTableName.split("_");
          StringBuilder prefix = new StringBuilder();
          prefix.append("_");
          for (int i = 0; i < parts.length - 2; i++) {
            if (!"".equalsIgnoreCase(parts[i])) {
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

      if (hyperTableCount.next()) {
        cnt = hyperTableCount.getLong(1);
      }
      hyperTableCount.close();
      statement.close();

      ts = System.currentTimeMillis();

      return new Sample(cnt, ts);

    } catch (SQLException e) {

    }

    ts = System.currentTimeMillis();

    return new Sample(cnt, ts);
  }



  String connectionUrl;
  String user;
  String userpw;
  String schema;
  String tableName;
  String hyperTablePrefix;
  Connection connection;

  public TimescaleSqlMon(String connectionUrl, String schema, String tableName, int sampleRateSec, int numSampleEqualBeforeExit, String user, String userpw) {

    this.connectionUrl = connectionUrl;
    this.user = user;
    this.userpw = userpw;
    this.schema  = schema;
    this.tableName = tableName;

    this.sampleRateSec = sampleRateSec;
    this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;

  }

  public static void main(String[] args) {

    String connectionUrl = "";
    String schema = "";
    String tableName = "";
    int sampleRateSec = 10; // default to 10 seconds.
    int numSampleEqualBeforeExit = 1;

    String username = "";   // default to empty string
    String password = "";  // default to empty string


    LOG.info("Entering application.");
    int numargs = args.length;
    if (numargs < 3) {
      System.err.print("Usage: TimescaleSqlMon [connectionUrl] [schema] [tableName] (sampleRateSec=10)  (numSampleEqualBeforeExit=1) (username=\"\") (password=\"\")  \n");
      System.err.println("Example: java -cp target/rttest.jar com.esri.rttest.mon.TimescaleSqlMon jdbc:postgresql://HostName:5432/dbName realtime safegraph 20 user pass");
    } else {
      connectionUrl = args[0];
      schema = args[1];
      tableName = args[2];

      if (numargs > 3) {
        sampleRateSec = Integer.parseInt(args[3]);
      }

      if (numargs > 4) {
        numSampleEqualBeforeExit = Integer.parseInt(args[4]);
        if (numSampleEqualBeforeExit < 1) {
          System.err.println("numSampleEqualBeforeExit must be greater than 1");
          System.exit(2);
        }
      }

      if (numargs > 5) {
        username = args[5];
      }

      if (numargs > 6) {
        password = args[6];
      }

      TimescaleSqlMon t = new TimescaleSqlMon(connectionUrl, schema, tableName, sampleRateSec, numSampleEqualBeforeExit, username, password);
      t.run();

    }

  }
}
