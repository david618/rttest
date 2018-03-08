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

import com.esri.rttest.sink.TcpSink;
import java.net.InetAddress;
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
    

    private static final String IPADDRESS_PATTERN
            = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public ArrayList<IPPort> getIPPorts(String appNamePattern) {

        ArrayList<IPPort> ipPorts = new ArrayList<>();

        try {
            // See if the url contains app(name)
            String appPat = "app\\[(.*)\\]";

            // Test to see if the appName pattern matches app[some-app-name]
            Pattern appPattern = Pattern.compile(appPat);
            Matcher appMatcher = appPattern.matcher(appNamePattern);

            // Test to see if the appName looks like it contains a ip address num.num.num.num
            Pattern IPpattern = Pattern.compile(IPADDRESS_PATTERN);
            Matcher IPmatcher = IPpattern.matcher(appNamePattern);

            if (appMatcher.find()) {
                // appNamePatter has app pattern
                String appName = appMatcher.group(1);

                int portIndex = 0;
                String appNameParts[] = appName.split(":");
                if (appNameParts.length > 1) {
                    appName = appNameParts[0];
                    portIndex = Integer.parseInt(appNameParts[1]);
                }
                System.out.println("appName:" + appName);
                System.out.println("portIndex:" + portIndex);
                MarathonInfo mi = new MarathonInfo();

                ipPorts = mi.getIPPorts(appName, portIndex);
            } else if (IPmatcher.matches()) {
                // appNamePattern looks like an IP
                String ipPortParts[] = appNamePattern.split(":");
                if (ipPortParts.length != 2) {
                    throw new UnsupportedOperationException("You need to provide IP:port");
                }
                int port = Integer.parseInt(ipPortParts[1]);

                IPPort ipport = new IPPort(ipPortParts[0], port);
                ipPorts.add(ipport);

            } else {
                // Assume it's a DNS-name:port

                String namePortParts[] = appNamePattern.split(":");
                if (namePortParts.length != 2) {
                    throw new UnsupportedOperationException("You need to provide dns-name:port");
                }
                String dnsName = namePortParts[0];
                int port = Integer.parseInt(namePortParts[1]);

                Lookup lookup = new Lookup(dnsName, Type.A);
                lookup.run();
                //System.out.println(lookup.getErrorString());
                if (lookup.getAnswers() == null) {
                    InetAddress addr = InetAddress.getByName(dnsName);

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

            for (IPPort ipport : ipPorts) {
                if (ipport.getPort() == -1) {
                    ipPorts.remove(ipport);
                }
            }
            return ipPorts;
        } catch (NumberFormatException | UnsupportedOperationException | UnknownHostException | TextParseException e) {
            return ipPorts;
        }
    }

    protected IPPorts() {
    }

    private static IPPorts instance = null;

    public static IPPorts getInstance() {
        if (instance == null) {
            instance = new IPPorts();
        }
        return instance;
    }

}
