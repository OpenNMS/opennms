/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of the trivial UNIX "time" service on remote interfaces. The class
 * implements the ServiceMonitor interface that allows it to be used along with
 * other plug-ins by the service poller framework.
 *
 * @author <A HREF="mailto:jeffg@opennms.org">Jeff Gehlbach</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

@Distributable
final public class TrivialTimeMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(TrivialTimeMonitor.class);

    /**
     * Default layer-4 protocol to use
     */
    private static final String DEFAULT_PROTOCOL = "tcp"; // Use TCP by default

    /**
     * Default port.
     */
    private static final int DEFAULT_PORT = 37;

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 3000; // 3 second timeout
    
    /**
     * Default permissible skew between the remote and local clocks
     */
    private static final int DEFAULT_ALLOWED_SKEW = 30; // 30 second skew
    
    /**
     * Seconds to subtract from a 1970-01-01 00:00:00-based UNIX timestamp
     * to make it comparable to a 1900-01-01 00:00:00-based timestamp from
     * the trivial time service (actually adding a negative value)
     */
    private static final int EPOCH_ADJ_FACTOR = 2085978496;

    /**
     * Whether to persist the skew value in addition to the response latency
     */
    private static final boolean DEFAULT_PERSIST_SKEW = false;
    
    /**
     * {@inheritDoc}
     *
     * Poll the specified address for service availability.
     *
     * During the poll an attempt is made to retrieve the time value from the
     * remote system.  This can be done via either TCP or UDP depending on the
     * provided parameters (default TCP).  If the time value returned is within
     * the specified number of seconds of the local system's clock time, then
     * the service is considered available.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        //
        // Process parameters
        //

        //
        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_INET)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");

        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        // Port
        //
        int port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);

        // Get the address instance.
        //
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        LOG.debug("poll: address = {}, port = {}, tracker = {}", InetAddressUtils.str(ipv4Addr), port, tracker);
        
        // Get the permissible amount of skew.
        //
        int allowedSkew = ParameterMap.getKeyedInteger(parameters, "allowed-skew", DEFAULT_ALLOWED_SKEW);

        // Determine whether to persist the skew value in addition to the latency
        boolean persistSkew = ParameterMap.getKeyedBoolean(parameters, "persist-skew", DEFAULT_PERSIST_SKEW);

        // Give it a whirl
        //
        PollStatus serviceStatus = PollStatus.unavailable();

        String protocol = ParameterMap.getKeyedString(parameters, "protocol", DEFAULT_PROTOCOL).toLowerCase();
        if (! protocol.equalsIgnoreCase("tcp") && ! protocol.equalsIgnoreCase("udp")) {
            throw new  IllegalArgumentException("Unsupported protocol, only TCP and UDP currently supported");
        } else if (protocol.equalsIgnoreCase("udp")) {
            // TODO test UDP support
            LOG.warn("UDP support is largely untested");
        }
        
        if (protocol.equalsIgnoreCase("tcp")) {
            serviceStatus = pollTimeTcp(svc, parameters, serviceStatus, tracker, ipv4Addr, port, allowedSkew, persistSkew);
        } else if (protocol.equalsIgnoreCase("udp")) {
            serviceStatus = pollTimeUdp(svc, parameters, serviceStatus, tracker, ipv4Addr, port, allowedSkew, persistSkew);
        }

        //
        // return the status of the service
        //
        return serviceStatus;
    }

    /**
     * <p>storeResult</p>
     *
     * @param serviceStatus a {@link org.opennms.netmgt.model.PollStatus} object.
     * @param skew a {@link java.lang.Number} object.
     * @param responseTime a {@link java.lang.Double} object.
     * @param persistSkew a boolean.
     */
    public void storeResult(PollStatus serviceStatus, Number skew, Double responseTime, boolean persistSkew) {
        Map<String,Number> skewProps = new LinkedHashMap<String,Number>();
        if (persistSkew) {
            skewProps.put("skew", skew);
	    LOG.debug("persistSkew: Persisting time skew (value = {}) for this node", skew);
        }
        skewProps.put("response-time", responseTime);
        serviceStatus.setProperties(skewProps);
    }

    /**
     * <p>pollTimeTcp</p>
     *
     * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
     * @param parameters a {@link java.util.Map} object.
     * @param serviceStatus a {@link org.opennms.netmgt.model.PollStatus} object.
     * @param tracker a {@link org.opennms.core.utils.TimeoutTracker} object.
     * @param ipv4Addr a {@link java.net.InetAddress} object.
     * @param port a int.
     * @param allowedSkew a int.
     * @param persistSkew a boolean.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public PollStatus pollTimeTcp(MonitoredService svc, Map<String, Object> parameters, PollStatus serviceStatus, TimeoutTracker tracker, InetAddress ipv4Addr, int port, int allowedSkew, boolean persistSkew) {
        int localTime = 0;
        int remoteTime = 0;
        boolean gotTime = false;

        for (tracker.reset(); tracker.shouldRetry() && !gotTime; tracker.nextAttempt()) {
            Socket socket = null;
            try {

                tracker.startAttempt();

                socket = new Socket();
                socket.connect(new InetSocketAddress(ipv4Addr, port), tracker.getConnectionTimeout());
                socket.setSoTimeout(tracker.getSoTimeout());
                LOG.debug("Connected to host: {} on TCP port: {}", ipv4Addr, port);

                //
                // Try to read from the socket
                //
                byte[] timeBytes = new byte[4];
                ByteBuffer timeByteBuffer = ByteBuffer.wrap(timeBytes);
                int bytesRead = socket.getInputStream().read(timeBytes);

                if (bytesRead != 4)
                    continue;
                LOG.debug("pollTimeTcp: bytes read = {}", bytesRead);
                
                try {
                    remoteTime = timeByteBuffer.getInt();
                } catch (BufferUnderflowException bue) {
                    LOG.error("Encountered buffer underflow while reading time from remote socket.");
                    remoteTime = 0;
                    serviceStatus = PollStatus.unavailable("Failed to read a valid time from remote host.");
                    continue; // to next iteration of for() loop
                }
                
                localTime  = (int)(System.currentTimeMillis() / 1000) - EPOCH_ADJ_FACTOR;
                gotTime = true;
                serviceStatus = qualifyTime(remoteTime, localTime, allowedSkew, serviceStatus, tracker.elapsedTimeInMillis(), persistSkew);
            } catch (NoRouteToHostException e) {
                String reason = "No route to host exception for address " + InetAddressUtils.str(ipv4Addr);
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (InterruptedIOException e) {
                String reason = "did not connect to host with " + tracker;
                LOG.debug(reason);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (ConnectException e) {
                String reason = "Connection exception for address: " + ipv4Addr;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (IOException e) {
                String reason = "IOException while polling address: " + ipv4Addr;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } finally {
                try {
                    // Close the socket
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    e.fillInStackTrace();
                    LOG.debug("pollTimeTcp: Error closing socket.", e);
                }
            }
        }
        return serviceStatus;
    }


    /**
     * <p>pollTimeUdp</p>
     *
     * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
     * @param parameters a {@link java.util.Map} object.
     * @param serviceStatus a {@link org.opennms.netmgt.model.PollStatus} object.
     * @param tracker a {@link org.opennms.core.utils.TimeoutTracker} object.
     * @param ipv4Addr a {@link java.net.InetAddress} object.
     * @param port a int.
     * @param allowedSkew a int.
     * @param persistSkew a boolean.
     * @return a {@link org.opennms.netmgt.model.PollStatus} object.
     */
    public PollStatus pollTimeUdp(MonitoredService svc, Map<String, Object> parameters, PollStatus serviceStatus, TimeoutTracker tracker, InetAddress ipv4Addr, int port, int allowedSkew, boolean persistSkew) {
        int localTime = 0;
        int remoteTime = 0;
        boolean gotTime = false;
        for (tracker.reset(); tracker.shouldRetry() && !gotTime; tracker.nextAttempt()) {
            DatagramSocket socket = null;
            final String hostAddress = InetAddressUtils.str(ipv4Addr);
			try {
    
                tracker.startAttempt();
    
                socket = new DatagramSocket();
                socket.setSoTimeout(tracker.getSoTimeout());
                LOG.debug("Requesting time from host: {} on UDP port: {}", ipv4Addr, port);
    
                //
                // Send an empty datagram per RFC868
                //
                socket.send(new DatagramPacket(new byte[]{}, 0, ipv4Addr, port));
                
                //
                // Try to receive a response from the remote socket
                //
                byte[] timeBytes = new byte[4];
                ByteBuffer timeByteBuffer = ByteBuffer.wrap(timeBytes);
                DatagramPacket timePacket = new DatagramPacket(timeBytes, timeBytes.length, ipv4Addr, port);
                socket.receive(timePacket);
                int bytesRead = timePacket.getLength();
    
                if (bytesRead != 4)
                    continue;
                LOG.debug("pollTimeUdp: bytes read = {}", bytesRead);
                
                try {
                    remoteTime = timeByteBuffer.getInt();
                } catch (BufferUnderflowException bue) {
                    LOG.error("Encountered buffer underflow while reading time from remote socket.");
                    remoteTime = 0;
                    serviceStatus = PollStatus.unavailable("Failed to read a valid time from remote host.");
                    continue; // to next iteration of for() loop
                }
                
                localTime  = (int)(System.currentTimeMillis() / 1000) - EPOCH_ADJ_FACTOR;
                gotTime = true;
                serviceStatus = qualifyTime(remoteTime, localTime, allowedSkew, serviceStatus, tracker.elapsedTimeInMillis(), persistSkew);
            } catch (PortUnreachableException e) {
                String reason = "Port unreachable exception for address " + hostAddress;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (NoRouteToHostException e) {
                String reason = "No route to host exception for address " + hostAddress;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (InterruptedIOException e) {
                String reason = "did not connect to host with " + tracker;
                LOG.debug(reason);
                serviceStatus = PollStatus.unavailable(reason);
            } catch (IOException e) {
                String reason = "IOException while polling address: " + ipv4Addr;
                LOG.debug(reason, e);
                serviceStatus = PollStatus.unavailable(reason);
            } finally {
                if (socket != null)
                    socket.close();
            }
        }
        return serviceStatus;
    }
    
    private PollStatus qualifyTime(int remoteTime, int localTime, int allowedSkew, PollStatus serviceStatus, double responseTime, boolean persistSkew) {
        LOG.debug("qualifyTime: checking remote time {} against local time {} with max skew of {}", remoteTime, localTime, allowedSkew);
        if ((localTime - remoteTime > allowedSkew) || (remoteTime - localTime > allowedSkew)) {
            String reason = "Remote time is " + (localTime > remoteTime ? ""+(localTime-remoteTime)+" seconds slow" : ""+(remoteTime-localTime)+" seconds fast");
            LOG.debug(reason);
            serviceStatus = PollStatus.unavailable(reason);
        }
        if ((localTime > remoteTime) && (localTime - remoteTime > allowedSkew)) {
            String reason = "Remote time is " + (localTime - remoteTime) + " seconds behind local, more than the allowable " + allowedSkew;
            LOG.debug(reason);
            serviceStatus = PollStatus.unavailable(reason);
        } else if ((remoteTime > localTime) && (remoteTime - localTime > allowedSkew)) {
            String reason = "Remote time is " + (remoteTime - localTime) + " seconds ahead of local, more than the allowable " + allowedSkew;
            LOG.debug(reason);
            serviceStatus = PollStatus.unavailable(reason);
        } else {
            serviceStatus = PollStatus.available();
        }

        storeResult(serviceStatus, remoteTime - localTime, responseTime, persistSkew);

        return serviceStatus;
    }
}
