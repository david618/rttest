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
 * Given appNamePattern return a list of ips and ports 
 *     dns-name:port returns all ip's associated with the dns-name
 *     app[Marathon-App-Name[:index]) returns all ip and ports for the Marathon-App-Name the index is the port index defaults to 0
 *     host:port returns names from hosts file and port
 *     ip:port returns just one ip:port
 */
package com.esri.rttest;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

/**
 *
 * @author david
 */
public class IPPorts {

    private static final Logger LOG = LogManager.getLogger(IPPorts.class);

    ArrayList<IPPort> ipPorts;
    String protocol;
    String path;

    public IPPorts(String appNamePattern) {

        ipPorts = new ArrayList<>();

        System.out.println(appNamePattern);

        try {
//            final String IPADDRESS_PATTERN = "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
//                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
//                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
//                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5]).*";

            final String IPADDRESS_PATTERN = ".*\\D([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                    + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\D.*";

            // See if the url contains app(name)
            final String APP_PATTERN = "(.*)://app\\[(.*)\\](.*)";

            if (!appNamePattern.startsWith("http")) {
                appNamePattern = "http://" + appNamePattern;
            }

            // Test to see if the appName pattern matches app[some-app-name]
            Pattern appPattern = Pattern.compile(APP_PATTERN);
            Matcher appMatcher = appPattern.matcher(appNamePattern);

            // Test to see if the appName looks like it contains a ip address num.num.num.num
            Pattern IPpattern = Pattern.compile(IPADDRESS_PATTERN);
            Matcher IPmatcher = IPpattern.matcher(appNamePattern);

            if (appMatcher.find()) {
                // appNamePatter has app pattern
                protocol = appMatcher.group(1);
                String appName = appMatcher.group(2);
                path = appMatcher.group(3);

                int portIndex = 0;
                String appNameParts[] = appName.split(":");
                if (appNameParts.length > 1) {
                    appName = appNameParts[0];
                    portIndex = Integer.parseInt(appNameParts[1]);
                }
                System.out.println("appName:" + appName);
                System.out.println("portIndex:" + portIndex);
                System.out.println("path:" + path);
                System.out.println("protocol:" + protocol);
                MarathonInfo mi = new MarathonInfo();

                ipPorts = mi.getIPPorts(appName, portIndex);
            } else  {

                URL aURL;
                aURL = new URL(appNamePattern);
                String host = aURL.getHost();
                int port = aURL.getPort();

                try {
                    protocol = aURL.getProtocol();
                } catch (Exception e) {
                    protocol = "http";
                }

                if (port == -1) {
                    if (protocol.equalsIgnoreCase("http")) {
                        port = 80;
                    } else {
                        port = 443;
                    }
                }

                path = aURL.getPath();

                System.out.println("ip: " + host);
                System.out.println("port:" + port);
                System.out.println("path:" + path);
                System.out.println("protocol:" + protocol);
                

                if (IPmatcher.matches())  {
                    IPPort ipport = new IPPort(host, port);
                    ipPorts.add(ipport);
                } else {
                    
                    // Lookup and populate ipPorts using host
                    Lookup lookup = new Lookup(host, Type.A);
                    lookup.run();
                    //System.out.println(lookup.getErrorString());
                    if (lookup.getAnswers() == null) {
                        InetAddress addr = InetAddress.getByName(host);
    
                        IPPort ipport = new IPPort(addr.getHostAddress(), port);
                        ipPorts.add(ipport);
                    } else {
                        for (Record ans : lookup.getAnswers()) {
                            String ip = ans.rdataToString();
                            IPPort ipport = new IPPort(ip, port);
                            ipPorts.add(ipport);
                            System.out.println(ipport);
    
                        }
                    }
                    
                }
                                
            }

            for (IPPort ipport : ipPorts) {
                if (ipport.getPort() == -1) {
                    ipPorts.remove(ipport);
                }
            }
        } catch (Exception e) {
            ipPorts = null;
            e.printStackTrace();
        }

    }

    public ArrayList<IPPort> getIPPorts() {
        return ipPorts;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getPath() {
        return path;
    }

    
    
    
    public static void main(String[] args) {

//        String name = "http://app[Someapp/Name:1234]/some/path/info";
//        IPPorts ipp = new IPPorts(name);
//        //ipp.getIPPorts();
//
//        System.out.println();
//        name = "https://172.16.0.133/somepath";
//        ipp = new IPPorts(name);
//
//        System.out.println();
//        name = "https://example.com/somepath";
//        ipp = new IPPorts(name);

        IPPorts ipp = new IPPorts((args[0]));
        
       ArrayList<IPPort> ipPorts = ipp.getIPPorts();

       for (IPPort ipport : ipPorts) {
          System.out.println(ipport.getIp() + ":" + ipport.getPort());
       }

        
        
    }

}
