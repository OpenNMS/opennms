/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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
import java.net.NoRouteToHostException;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.opennms.protocols.ntp.NtpMessage;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the NTP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

@Distributable
final public class NtpMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(NtpMonitor.class);
    /**
     * Default NTP port.
     */
    private static final int DEFAULT_PORT = 123;

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 5000;

    /**
     * {@inheritDoc}
     *
     * <P>
     * Poll the specified address for NTP service availability.
     * </P>
     *
     * <P>
     * During the poll an NTP request query packet is generated. The query is
     * sent via UDP socket to the interface at the specified port (by default
     * UDP port 123). If a response is received, it is parsed and validated. If
     * the NTP was successful the service status is set to SERVICE_AVAILABLE and
     * the method returns.
     * </P>
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        //
        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_INET)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");

        // get the log
        //

        // get the parameters
        //
        TimeoutTracker tracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);

        int port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);

        // get the address and NTP address request
        //
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        PollStatus serviceStatus = PollStatus.unavailable();
        DatagramSocket socket = null;
        double responseTime = -1.0;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(tracker.getSoTimeout()); // will force the
            // InterruptedIOException

            for (tracker.reset(); tracker.shouldRetry() && !serviceStatus.isAvailable(); tracker.nextAttempt()) {
                try {
                    // Send NTP request
                    //
                    byte[] data = new NtpMessage().toByteArray();
                    DatagramPacket outgoing = new DatagramPacket(data, data.length, ipv4Addr, port);

                    tracker.startAttempt();

                    socket.send(outgoing);

                    // Get NTP Response
                    //
                    // byte[] buffer = new byte[512];
                    DatagramPacket incoming = new DatagramPacket(data, data.length);
                    socket.receive(incoming);
                    responseTime = tracker.elapsedTimeInMillis();
                    double destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;

                    // Validate NTP Response
                    // IOException thrown if packet does not decode as expected.
                    NtpMessage msg = new NtpMessage(incoming.getData());
                    double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;


                    LOG.debug("poll: valid NTP request received the local clock offset is {}, responseTime= {}ms", localClockOffset, responseTime);
                    LOG.debug("poll: NTP message : {}", msg);
                    serviceStatus = PollStatus.available(responseTime);
                } catch (InterruptedIOException ex) {
                    // Ignore, no response received.
                }
            }
        } catch (NoRouteToHostException e) {
        	
        	String reason = "No route to host exception for address: " + ipv4Addr;
            LOG.debug(reason, e);
            serviceStatus = PollStatus.unavailable(reason);
        	
        } catch (ConnectException e) {
        	
        	String reason = "Connection exception for address: " + ipv4Addr;
            LOG.debug(reason, e);
            serviceStatus = PollStatus.unavailable(reason);
        	
        } catch (IOException ex) {
        	
        	String reason = "IOException while polling address: " + ipv4Addr;
            LOG.debug(reason, ex);
            serviceStatus = PollStatus.unavailable(reason);
        	
        } finally {
            if (socket != null)
                socket.close();
        }

        // 
        //
        // return the status of the service
        //
        return serviceStatus;
    }

}
