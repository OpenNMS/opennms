/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of the Memcached service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 */
@Distributable
final public class MemcachedMonitor extends AbstractServiceMonitor {

    /**
     * Default FTP port.
     */
    private static final int DEFAULT_PORT = 11211;

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 3000; // 3 second timeout on read()

    private static final String[] m_keys = new String[] {
        "uptime", "rusageuser", "rusagesystem",
        "curritems", "totalitems", "bytes", "limitmaxbytes",
        "currconnections", "totalconnections", "connectionstructure",
        "cmdget", "cmdset", "gethits", "getmisses", "evictions",
        "bytesread", "byteswritten", "threads"
    };
    
    /**
     * {@inheritDoc}
     *
     * Poll the specified address for Memcached service availability.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {

        TimeoutTracker timeoutTracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
        
        int port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);

        // Extract the address
        InetAddress ipv4Addr = svc.getAddress();
        String host = InetAddressUtils.str(ipv4Addr);

        if (log().isDebugEnabled())
            log().debug("polling interface: " + host + timeoutTracker);

        PollStatus serviceStatus = PollStatus.unavailable();

        for(timeoutTracker.reset(); timeoutTracker.shouldRetry() && !serviceStatus.isAvailable(); timeoutTracker.nextAttempt()) {
            Socket socket = null;
            try {

                timeoutTracker.startAttempt();
                
                socket = new Socket();
                socket.connect(new InetSocketAddress(ipv4Addr, port), timeoutTracker.getConnectionTimeout());
                socket.setSoTimeout(timeoutTracker.getSoTimeout());
                log().debug("connected to host: " + host + " on port: " + port);

                // We're connected, so upgrade status to unresponsive
                serviceStatus = PollStatus.unresponsive();

                OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
                osw.write("stats\n");
                osw.flush();

                // Allocate a line reader
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Map<String, Number> statProps = new LinkedHashMap<String,Number>();
                for (String key : m_keys) {
                    statProps.put(key, null);
                }

                String line = null;
                do {
                    line = reader.readLine();
                    if (line == null) break;
                    String[] statEntry = line.trim().split("\\s", 3);
                    if (statEntry[0].equals("STAT")) {
                        try {
                            Number value;
                            if (statEntry[2].contains(".")) {
                                value = Double.parseDouble(statEntry[2]);
                            } else {
                                value = Long.parseLong(statEntry[2]);
                            }
                            String key = statEntry[1].toLowerCase();
                            key = key.replaceAll("_", "");
                            if (key.length() > 19) {
                                key = key.substring(0, 19);
                            }
                            if (statProps.containsKey(key)) {
                                statProps.put(key, value);
                            }
                        } catch (Throwable e) {
                            // ignore errors parsing
                        }
                    } else if (statEntry[0].equals("END")) {
                        serviceStatus = PollStatus.available();
                        osw.write("quit\n");
                        osw.flush();
                        break;
                    }
                } while (line != null);

                serviceStatus.setProperties(statProps);
                serviceStatus.setResponseTime(timeoutTracker.elapsedTimeInMillis());
            } catch (ConnectException e) {
                // Connection refused!! Continue to retry.
            	serviceStatus = logDown(Level.DEBUG, "Connection refused by host "+host, e);
            } catch (NoRouteToHostException e) {
            	// No route to host!! Try retries anyway in case strict timeouts are enabled
                serviceStatus = logDown(Level.INFO, "Unable to test host " + host + ", no route available", e);
            } catch (InterruptedIOException e) {
            	serviceStatus = logDown(Level.DEBUG, "did not connect to host " + host +" within timeout: " + timeoutTracker);
            } catch (IOException e) {
            	serviceStatus = logDown(Level.INFO, "Error communicating with host " + host, e);
            } catch (Throwable t) {
                serviceStatus = logDown(Level.WARN, "Undeclared throwable exception caught contacting host " + host, t);
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                        socket = null;
                    }
                } catch (IOException e) {
                }
            }
            
        }

        //
        // return the status of the service
        //
        return serviceStatus;
    }

}
