/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of the Memcached service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 * @author <A HREF="mailto:ranger@opennms.org">Benjamin Reed</A>
 */
final public class MemcachedMonitor extends AbstractServiceMonitor {

    public static final Logger LOG = LoggerFactory.getLogger(MemcachedMonitor.class);

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

        LOG.debug("polling interface: {} {}", host, timeoutTracker);

        PollStatus serviceStatus = PollStatus.unavailable();

        for(timeoutTracker.reset(); timeoutTracker.shouldRetry() && !serviceStatus.isAvailable(); timeoutTracker.nextAttempt()) {
            Socket socket = null;
            try {

                timeoutTracker.startAttempt();
                
                socket = new Socket();
                socket.connect(new InetSocketAddress(ipv4Addr, port), timeoutTracker.getConnectionTimeout());
                socket.setSoTimeout(timeoutTracker.getSoTimeout());
                LOG.debug("connected to host: {} on port: {}", host, port);

                // We're connected, so upgrade status to unresponsive
                serviceStatus = PollStatus.unresponsive();

                OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
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
            	String reason = "Connection refused by host "+host;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (NoRouteToHostException e) {
            	// No route to host!! Try retries anyway in case strict timeouts are enabled
                String reason = "Unable to test host " + host + ", no route available";
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (InterruptedIOException e) {
            	String reason = "did not connect to host " + host +" within timeout: " + timeoutTracker;
                LOG.debug(reason);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (IOException e) {
            	String reason = "Error communicating with host " + host;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (Throwable t) {
                String reason = "Undeclared throwable exception caught contacting host " + host;
                LOG.debug(reason, t);
                serviceStatus = PollStatus.unavailable(reason);
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
