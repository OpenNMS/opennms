//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.poller;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;
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
 * 
 */
final public class NtpMonitor extends IPv4LatencyMonitor {
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
     * 
     * @param iface
     *            The network interface to test the service on.
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used for
     *            this poll.
     * 
     * @return The availibility of the interface and if a transition event
     *         should be supressed.
     * 
     */
    public int poll(NetworkInterface iface, Map parameters, org.opennms.netmgt.config.poller.Package pkg) {
        //
        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");

        // get the log
        //
        Category log = ThreadCategory.getInstance(getClass());

        // get the parameters
        //
        int retry = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
        int port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);
        int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);
        String rrdPath = ParameterMap.getKeyedString(parameters, "rrd-repository", null);
        String dsName = ParameterMap.getKeyedString(parameters, "ds-name", null);

        if (rrdPath == null) {
            log.info("poll: RRD repository not specified in parameters, latency data will not be stored.");
        }
        if (dsName == null) {
            dsName = DS_NAME;
        }

        // get the address and NTP address request
        //
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

        int serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
        DatagramSocket socket = null;
        long responseTime = -1;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout); // will force the
            // InterruptedIOException

            for (int attempts = 0; attempts <= retry && serviceStatus != SERVICE_AVAILABLE; attempts++) {
                try {
                    // Send NTP request
                    //
                    byte[] data = new NtpMessage().toByteArray();
                    DatagramPacket outgoing = new DatagramPacket(data, data.length, ipv4Addr, port);
                    long sentTime = System.currentTimeMillis();
                    socket.send(outgoing);

                    // Get NTP Response
                    //
                    // byte[] buffer = new byte[512];
                    DatagramPacket incoming = new DatagramPacket(data, data.length);
                    socket.receive(incoming);
                    responseTime = System.currentTimeMillis() - sentTime;
                    double destinationTimestamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;

                    // Validate NTP Response
                    // IOException thrown if packet does not decode as expected.
                    NtpMessage msg = new NtpMessage(incoming.getData());
                    double localClockOffset = ((msg.receiveTimestamp - msg.originateTimestamp) + (msg.transmitTimestamp - destinationTimestamp)) / 2;

                    if (log.isDebugEnabled())
                        log.debug("poll: valid NTP request received the local clock offset is " + localClockOffset + ", responseTime= " + responseTime + "ms");
                    log.debug("poll: NTP message : " + msg.toString());
                    serviceStatus = ServiceMonitor.SERVICE_AVAILABLE;
                } catch (InterruptedIOException ex) {
                    // Ignore, no response received.
                }
            }
        } catch (NoRouteToHostException e) {
            e.fillInStackTrace();
            log.debug("No route to host exception for address: " + ipv4Addr, e);
        } catch (ConnectException e) {
            // Connection refused. Continue to retry.
            //
            e.fillInStackTrace();
            log.debug("Connection exception for address: " + ipv4Addr, e);
        } catch (IOException ex) {
            ex.fillInStackTrace();
            log.info("IOException while polling address: " + ipv4Addr, ex);
        } finally {
            if (socket != null)
                socket.close();
        }

        // Store response time if available
        //
        if (serviceStatus == ServiceMonitor.SERVICE_AVAILABLE) {
            // Store response time in RRD
            if (responseTime >= 0 && rrdPath != null) {
                try {
                    this.updateRRD(rrdPath, ipv4Addr, dsName, responseTime, pkg);
                } catch (RuntimeException rex) {
                    log.debug("There was a problem writing the RRD:" + rex);
                }
            }
        }

        // 
        //
        // return the status of the service
        //
        return serviceStatus;
    }

}