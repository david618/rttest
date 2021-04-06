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
 *
 * Sends lines of Json to a PostgreSQL database created Point Geometries.  PostgreSQL must have PostGIS installed.
 * You must create the table manually before running command to load data.  If you specify just the first four parameters SQL for create will be provided.
 * This code has not been setup fro sending at a specified rate yet.  For PostgreSQL ruunning on local VM; rates near 3,000/s are possible.
 */
package com.esri.rttest.send;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author david
 */
public class Postgresql extends Send {


    @Override
    public long sendBatch(ArrayList<String> lines) {

        return 0;
    }

    @Override
    public void sendDone() {

    }

    private static final Logger LOG = LogManager.getLogger(Postgresql.class);

    final static int INT = 0;
    final static int LNG = 1;
    final static int DBL = 2;
    final static int STR = 3;

    final static int MAXSTRLEN = 100;

    private void printCreate(String tablename, String fileJsonLines, String geomFieldName, String oidFieldName) {

    	BufferedReader br = null;
    	
        try {

            //
            // CREATE TABLE ecenter ( oid integer, clat double precision, clon
            // double precision, num integer );
            //
            // SELECT AddGeometryColumn('', 'ecenter','geom',4326,'POINT',2);
            //
            //
            FileReader fr = new FileReader(fileJsonLines);

            br = new BufferedReader(fr);

            String line = br.readLine();

            JSONObject json;

            StringBuilder sql = new StringBuilder();

            if (line != null) {

                sql.append("CREATE TABLE ").append(tablename).append(" (").append(oidFieldName).append(" serial4,");

                // Create the Schema
                json = new JSONObject(line);

                Set<String> ks = json.keySet();

                for (String k : ks) {
                    //System.out.println(k);

                    Object val = json.get(k);

                    if (val instanceof Integer) {
                        sql.append(k).append(" integer,");
                    } else if (val instanceof Long) {
                        sql.append(k).append(" bigint,");
                    } else if (val instanceof Double) {
                        sql.append(k).append(" double precision,");
                    } else if (val instanceof String) {
                        sql.append(k).append(" varchar(" + MAXSTRLEN + "),");
                    }

                }


                String sqlStr = sql.toString().substring(0, sql.length() - 1) + ");";

                System.out.println(sqlStr);

                sqlStr = "SELECT AddGeometryColumn('','" + tablename + "','" + geomFieldName + "',4326,'POINT',2);";
                System.out.println(sqlStr);

            }

        } catch (IOException | JSONException e) {
            LOG.error("ERROR", e);
        } finally {
        	if (br != null) {
        		try {
        			br.close();
        		} catch (Exception e) {
        			// ok to ignore
        		}
        	}
        }

    }

    private void run(String tablename, String fileJsonLines, String geomFieldName, String oidFieldName, String serverDB, String username, String password, String lonFieldName, String latFieldName) {
        try {

            // Create DB Connection
            Connection c;
            Statement stmt;
            c = DriverManager
                    .getConnection("jdbc:postgresql://" + serverDB,
                            username, password);
            c.setAutoCommit(false);

            //stmt = c.createStatement();
            FileReader fr;
            fr = new FileReader(fileJsonLines);

            try (BufferedReader br = new BufferedReader(fr)) {
                String line = br.readLine();
                
                StringBuilder sqlPrefix = new StringBuilder();
                JSONObject json = null;
                
                HashMap<String, Integer> jsonMap = new HashMap<>();
                
                if (line != null) {
                    
                    sqlPrefix.append("INSERT INTO ").append(tablename).append(" (").append(oidFieldName).append(",");
                    
                    // Create the Schema
                    json = new JSONObject(line);
                    
                    Set<String> ks = json.keySet();
                    
                    for (String k : ks) {
                        //System.out.println(k);
                        
                        Object val = json.get(k);
                        
                        if (val instanceof Integer) {
                            jsonMap.put(k, INT);
                        } else if (val instanceof Long) {
                            jsonMap.put(k, LNG);
                        } else if (val instanceof Double) {
                            jsonMap.put(k, DBL);
                        } else if (val instanceof String) {
                            jsonMap.put(k, STR);
                        }
                        //System.out.println();
                        sqlPrefix.append(k).append(",");
                        
                    }

                    sqlPrefix.append(geomFieldName).append(") VALUES (DEFAULT,");
                    
                }

                int num = 0;
                
                while (line != null) {
                    StringBuilder sql = new StringBuilder();
                    
                    sql.append(sqlPrefix.toString());
                    
                    for (String key : jsonMap.keySet()) {
                        
                        switch (jsonMap.get(key)) {
                            case INT:
                                sql.append(json.getInt(key)).append(",");
                                break;
                            case LNG:
                                sql.append(json.getLong(key)).append(",");
                                break;
                            case DBL:
                                sql.append(json.getDouble(key)).append(",");
                                break;
                            case STR:
                                sql.append("'").append(json.getString(key).replace("'", "''")).append("',");
                                break;
                            default:
                                break;
                        }
                        
                    }
                    
                    //ST_GeomFromText('POINT(-71.060316 48.432044)', 4326)
                    //sql = sql.substring(0,sql.length() - 1) + ");";
                    sql.append("ST_GeomFromText('POINT(").append(json.getDouble(lonFieldName)).append(" ").append(json.getDouble(latFieldName)).append(")', 4326)").append(");");
                    //System.out.println(sql);
                    
                    stmt = c.createStatement();
                    stmt.executeUpdate(sql.toString());
                    
                    num += 1;
                    
                    if (num % 1000 == 0) {
                        c.commit();
                    }
                    
                    line = br.readLine();
                    
                    if (line != null) {
                        json = new JSONObject(line);
                    }
                    
//                break;
                }
                
                c.commit();
                
                c.close();
            }
            fr.close();
        } catch (IOException | SQLException | JSONException e) {
            LOG.error("ERROR", e);
        }

    }

    public static void main(String[] args) {


        Postgresql app = new Postgresql();
        String appName = app.getClass().getSimpleName();

        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(160);
        formatter.setLeftPadding(1);

        Option helpOp = Option.builder()
                .longOpt("help")
                .desc("display help and exit")
                .build();

        Option tableNameOp = Option.builder("t")
                .longOpt("table-name")
                .required()
                .hasArg()
                .desc("[Required] Postgres Table Name")
                .build();

        Option serverConOp = Option.builder("h")
                .longOpt("server-connect")
                .hasArg()
                .desc("[Required] Postgres Connect (e.g. postgresserver:5432/db1 ")
                .build();

        Option fileOp = Option.builder("f")
                .longOpt("file")
                .required()
                .hasArg()
                .desc("[Required] File with lines of text to send; if a folder is specified all files in the folder are sent one at a time alphabetically")
                .build();

        Option rateOp = Option.builder("r")
                .longOpt("rate")
                .hasArg()
                .desc("[Required] Desired Rate. The tool will try to send at this rate if possible")
                .build();

        Option numToSendOp = Option.builder("n")
                .longOpt("number-to-send")
                .hasArg()
                .desc("[Required] Number of lines to send")
                .build();


        Option onetimeOp = Option.builder("o")
                .longOpt("one-time")
                .desc("Send lines only one time. Stop when all lines have been sent.")
                .build();

        Option usernameOp = Option.builder("u")
                .longOpt("username")
                .hasArg()
                .desc("Postgres Server Username; default no username")
                .build();

        Option passwordOp = Option.builder("p")
                .longOpt("password")
                .hasArg()
                .desc("Postgres Server Password; default no password")
                .build();

        Option geomFieldOp = Option.builder("g")
                .longOpt("geom-field")
                .required()
                .hasArg()
                .desc("Geometry Field Name")
                .build();

        Option oidOp = Option.builder("i")
                .longOpt("oid-fieldname")
                .required()
                .hasArg()
                .desc("Geometry oid Field Name")
                .build();

        Option lonOp = Option.builder("x")
                .longOpt("lon")
                .hasArg()
                .desc("lon field in json input")
                .build();

        Option latOp = Option.builder("y")
                .longOpt("lat")
                .hasArg()
                .desc("lat field in json input")
                .build();

        options.addOption(helpOp);
        options.addOption(tableNameOp);
        options.addOption(serverConOp);
        options.addOption(fileOp);
        options.addOption(rateOp);
        options.addOption(numToSendOp);
        options.addOption(onetimeOp);
        options.addOption(usernameOp);
        options.addOption(passwordOp);
        options.addOption(geomFieldOp);
        options.addOption(oidOp);
        options.addOption(lonOp);
        options.addOption(latOp);


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
            System.out.println("Send lines from a file to an Http Server");
            System.out.println();
            formatter.printHelp(appName, options);
            System.exit(0);
        }

        String tableName = null;
        if (cmd.hasOption("t")) {
            tableName = cmd.getOptionValue("t");
        }
        System.out.println("tableName: " + tableName);

        String serverConn = null;
        if (cmd.hasOption("t")) {
            serverConn = cmd.getOptionValue("t");
        }
        System.out.println("serverConn: " + serverConn);

        String filename = null;
        if(cmd.hasOption("f")) {
            filename = cmd.getOptionValue("f");
        }
        System.out.println("filename: " + filename);

        Integer desiredRatePerSec = null;
        if(cmd.hasOption("r")) {
            try {
                desiredRatePerSec = Integer.parseInt(cmd.getOptionValue("r"));
            } catch (NumberFormatException e ) {
                // Rate Must be Integer
                System.out.println();
                System.out.println("Invalid value for rate (r).  Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("desiredRatePerSec: " + desiredRatePerSec);

        Long numToSend = null;
        if(cmd.hasOption("n")) {
            try {
                numToSend = Long.parseLong(cmd.getOptionValue("n"));
            } catch (NumberFormatException e) {
                System.out.println();
                System.out.println("Invalid value for num-to-send (n). Must be an Integer");
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }
        }
        System.out.println("numToSend: " + numToSend);

        boolean reuseFile = true;
        if(cmd.hasOption("o")) {
            reuseFile = false;
        }
        System.out.println("reuseFile : " + reuseFile);

        String username = "";
        if(cmd.hasOption("u")) {
            username = cmd.getOptionValue("u");
        }
        System.out.println("username: " + username);

        String password = "";
        if(cmd.hasOption("p")) {
            password = cmd.getOptionValue("p");
        }
        System.out.println("password: " + password);

        String geomFieldName = null;
        if(cmd.hasOption("g")) {
            geomFieldName = cmd.getOptionValue("g");
        }
        System.out.println("geomFieldName: " + geomFieldName);

        String oidFieldName = null;
        if(cmd.hasOption("i")) {
            oidFieldName = cmd.getOptionValue("i");
        }
        System.out.println("oidFieldName: " + oidFieldName);

        String lonFieldName = null;
        if(cmd.hasOption("x")) {
            lonFieldName = cmd.getOptionValue("x");
        }
        System.out.println("lonFieldName: " + lonFieldName);

        String latFieldName = null;
        if(cmd.hasOption("y")) {
            latFieldName = cmd.getOptionValue("y");
        }
        System.out.println("latFieldName: " + latFieldName);

        if (serverConn == null) {
            app.printCreate(tableName, filename, geomFieldName, oidFieldName);
        } else {
            boolean missingParam = false;
            if (username == null) {
                missingParam = true;
                System.out.println("You must Postgres Username");
            }
            if (password == null) {
                missingParam = true;
                System.out.println("You must Postgres Password");
            }
            if (lonFieldName == null) {
                missingParam = true;
                System.out.println("You must a Longitude Field Name");
            }
            if (latFieldName == null) {
                missingParam = true;
                System.out.println("You must a Latitude Field Name");
            }
            if (missingParam) {
                System.out.println();
                formatter.printHelp(appName, options);
                System.exit(1);
            }

            app.run(tableName, filename, geomFieldName, oidFieldName, serverConn, username, password, lonFieldName, latFieldName);
        }



    }

}
