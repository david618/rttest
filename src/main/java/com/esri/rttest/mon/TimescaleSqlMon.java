package com.esri.rttest.mon;

import org.apache.commons.cli.*;
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
        long ts;

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
            e.printStackTrace();
        }

        ts = System.currentTimeMillis();

        return new Sample(cnt, ts);
    }


    public TimescaleSqlMon() {
    }

    String connectionUrl;
    String username;
    String password;
    String schema;
    String tableName;
    String hyperTablePrefix;
    Connection connection;

    public TimescaleSqlMon(String connectionUrl, String schema, String tableName, int sampleRateSec, int numSampleEqualBeforeExit, String username, String password) {

        this.connectionUrl = connectionUrl;
        this.username = username;
        this.password = password;
        this.schema = schema;
        this.tableName = tableName;

        this.sampleRateSec = sampleRateSec;
        this.numSampleEqualBeforeExit = numSampleEqualBeforeExit;

    }

    public static void main(String[] args) {

        TimescaleSqlMon app = new TimescaleSqlMon();
        String appName = app.getClass().getSimpleName();

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);
        formatter.setLeftPadding(1);

        Option helpOp = Option.builder()
                .longOpt("help")
                .desc("display help and exit")
                .build();

        Option urlOp = Option.builder("l")
                .longOpt("timescale-url")
                .required()
                .hasArg()
                .desc("[Required] Timescale DB URL (e.g. jdbc:postgresql://HostName:5432/dbName)")
                .build();

        Option schemaOp = Option.builder("s")
                .longOpt("schema")
                .required()
                .hasArg()
                .desc("[Required] Timescale schema (e.g. realtime)")
                .build();


        Option tableOp = Option.builder("t")
                .longOpt("table")
                .required()
                .hasArg()
                .desc("[Required] Timescale table (e.g. safegraph)")
                .build();


        Option sampleRateSecOp = Option.builder("r")
                .longOpt("sample-rate-sec")
                .hasArg()
                .desc("Sample Rate Seconds; defaults to 10")
                .build();

        Option resetCountOp = Option.builder("n")
                .longOpt("num-samples-no-change")
                .hasArg()
                .desc("Reset after number of this number of samples of no change in count; defaults to 1")
                .build();

        Option usernameOp = Option.builder("u")
                .longOpt("username")
                .hasArg()
                .desc("Mqtt Server Username; default no username")
                .build();

        Option passwordOp = Option.builder("p")
                .longOpt("password")
                .hasArg()
                .desc("Mqtt Server Password; default no password")
                .build();

        options.addOption(helpOp);
        options.addOption(urlOp);
        options.addOption(schemaOp);
        options.addOption(tableOp);
        options.addOption(sampleRateSecOp);
        options.addOption(resetCountOp);
        options.addOption(usernameOp);
        options.addOption(passwordOp);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);

        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.err.println();
            formatter.printHelp(appName, options);
            System.exit(1);
        }

        if (cmd.hasOption("--help")) {
            System.out.println("Send lines from a file to an Elastic Server");
            System.out.println();
            formatter.printHelp(appName, options);
            System.exit(0);
        }

        String connectionUrl = null;
        if (cmd.hasOption("l")) {
            connectionUrl = cmd.getOptionValue("l");
        }
        System.out.println("connectionUrl: " + connectionUrl);

        String schema = null;
        if (cmd.hasOption("s")) {
            schema = cmd.getOptionValue("s");
        }
        System.out.println("schema: " + schema);

        String tableName = null;
        if (cmd.hasOption("t")) {
            tableName = cmd.getOptionValue("t");
        }
        System.out.println("tableName: " + tableName);

        int sampleRateSec = 10;
        if (cmd.hasOption("r")) {
            try {
                sampleRateSec = Integer.parseInt(cmd.getOptionValue("r"));
            } catch (NumberFormatException e) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid sample-rate-sec (r).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("sampleRateSec: " + sampleRateSec);

        int numSampleEqualBeforeExit = 1;
        if (cmd.hasOption("n")) {
            try {
                numSampleEqualBeforeExit = Integer.parseInt(cmd.getOptionValue("n"));
            } catch (NumberFormatException e) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid num-samples-no-change (s).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("numSampleEqualBeforeExit: " + numSampleEqualBeforeExit);

        String username = "";
        if (cmd.hasOption("u")) {
            username = cmd.getOptionValue("u");
        }
        System.out.println("username: " + username);


        String password = "";
        if (cmd.hasOption("p")) {
            password = cmd.getOptionValue("p");
        }
        System.out.println("password: " + password);

        app = new TimescaleSqlMon(connectionUrl, schema, tableName, sampleRateSec, numSampleEqualBeforeExit, username, password);
        app.run();
    }

}

