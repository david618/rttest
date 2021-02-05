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

 /*
Sends lines of Json to a PostgreSQL database created Point Geometries.  PostgreSQL must have PostGIS installed. 
You must create the table manually before running command to load data.  If you specify just the first four parameters SQL for create will be provided.

This code has not been setup fro sending at a specified rate yet.  For PostgreSQL ruunning on local VM; rates near 3,000/s are possible.

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

            /**
             * CREATE TABLE ecenter ( oid integer, clat double precision, clon
             * double precision, num integer );
             *
             * SELECT AddGeometryColumn('', 'ecenter','geom',4326,'POINT',2);
             *
             */
            FileReader fr = new FileReader(fileJsonLines);

            br = new BufferedReader(fr);

            String line = br.readLine();

            JSONObject json;

            String sql;

            if (line != null) {

                sql = "CREATE TABLE " + tablename + " (" + oidFieldName + " serial4,";

                // Create the Schema
                json = new JSONObject(line);

                Set<String> ks = json.keySet();

                for (String k : ks) {
                    //System.out.println(k);

                    Object val = json.get(k);

                    if (val instanceof Integer) {
                        sql += k + " integer,";
                    } else if (val instanceof Long) {
                        sql += k + " bigint,";
                    } else if (val instanceof Double) {
                        sql += k + " double precision,";
                    } else if (val instanceof String) {
                        sql += k + " varchar(" + MAXSTRLEN + "),";
                    }

                }

                sql = sql.substring(0, sql.length() - 1) + ");";

                System.out.println(sql);

                sql = "SELECT AddGeometryColumn('','" + tablename + "','" + geomFieldName + "',4326,'POINT',2);";
                System.out.println(sql);

            }

        } catch (IOException | JSONException e) {
            LOG.error("ERROR", e);
        } finally {
        	if (br != null) {
        		try {
        			br.close();
        		} catch (Exception e) {
        			
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
                
                String sqlPrefix = "";
                JSONObject json = null;
                
                HashMap<String, Integer> jsonMap = new HashMap<>();
                
                if (line != null) {
                    
                    sqlPrefix = "INSERT INTO " + tablename + " (" + oidFieldName + ",";
                    
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
                        sqlPrefix += k + ",";
                        
                    }
                    
                    //oid,a,b,clat,clon,rot,num,geom
                    //sqlPrefix = sqlPrefix.substring(0,sqlPrefix.length() - 1) + ") VALUES (";
                    sqlPrefix += geomFieldName + ") VALUES (DEFAULT,";
                    
                }

                int num = 0;
                
                while (line != null) {
                    //System.out.println(line);
                    // Create sql line
                    String sql;
                    
                    sql = sqlPrefix;
                    
                    for (String key : jsonMap.keySet()) {
                        
                        switch (jsonMap.get(key)) {
                            case INT:
                                sql += json.getInt(key) + ",";
                                break;
                            case LNG:
                                sql += json.getLong(key) + ",";
                                break;
                            case DBL:
                                sql += json.getDouble(key) + ",";
                                break;
                            case STR:
                                sql += "'" + json.getString(key).replace("'", "''") + "',";
                                break;
                            default:
                                break;
                        }
                        
                    }
                    
                    //ST_GeomFromText('POINT(-71.060316 48.432044)', 4326)
                    //sql = sql.substring(0,sql.length() - 1) + ");";
                    sql += "ST_GeomFromText('POINT(" + json.getDouble(lonFieldName) + " " + json.getDouble(latFieldName) + ")', 4326)" + ");";
                    //System.out.println(sql);
                    
                    stmt = c.createStatement();
                    stmt.executeUpdate(sql);
                    
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
        Postgresql t = new Postgresql();

//        String tableName = "planes3";
//        String filename = "flights.json";
//        String geomFieldName = "geom";
//        String oidFieldName = "oid";
//
//        String serverConn = "pg1:5432/db1";
//        String username = "user1";
//        String password = "user1";
//        String lonFieldName = "lon";
//        String latFieldName = "lat";
//        //t.printCreate(tableName, filename, geomFieldName, oidFieldName);
//        t.run(tableName, filename, geomFieldName, oidFieldName, serverConn, username, password, lonFieldName, latFieldName);
        String tableName;
        String filename;
        String geomFieldName;
        String oidFieldName;

        String serverConn;
        String username;
        String password;
        String lonFieldName;
        String latFieldName;

        int numargs = args.length;

        if (numargs != 4 && numargs != 9) {
            System.err.println("**** WARNING:  This tool is in development; still does not output rates ***");
            System.err.println("");
            System.err.println("Usage Print Create Table: Postgresql [tableName] [fileName] [geomFieldName] [oidFieldName]");
            System.err.println("or");
            System.err.println("Usage Load Data: Postgresql [tableName] [fileName] [geomFieldName] [oidFieldName] [serverConn] [username] [password] [lonFieldName] [latFieldName]");
            System.err.println("");
            System.err.println("Example: java -cp target/rttest.jar com.esri.rttest.send.Postgresql planes planes.json geom gid 192.168.57.2:5432/gis1 gis PASSWORD lon lat");
            System.err.println("");
            System.err.println("Loads lines from file planes.json to table planes. The table planes has oidFieldName of gid and geomFieldName of geom.  The serverConn is the IP:PORT/database.");
            System.err.println("  You'll need to specify the username and password that can insert into the table.  The lonFieldName (lon) and latFieldname (lat) from the json that will be used to create points.");
            System.err.println("");


        } else {
            tableName = args[0];
            filename = args[1];
            geomFieldName = args[2];
            oidFieldName = args[3];

            if (numargs == 4) {

                t.printCreate(tableName, filename, geomFieldName, oidFieldName);
            } else {
                serverConn = args[4];
                username = args[5];
                password = args[6];
                lonFieldName = args[7];
                latFieldName = args[8];

                t.run(tableName, filename, geomFieldName, oidFieldName, serverConn, username, password, lonFieldName, latFieldName);
            }
        }

    }

}
