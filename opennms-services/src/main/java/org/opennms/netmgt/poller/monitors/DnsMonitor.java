//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2010 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2010 Feb 16: Make fatal error codes configurable (bug 3564) - jeffg@opennms.org
// 2003 Jul 18: Enabled retries for monitors.
// 2003 Jun 11: Added a "catch" for RRD update errors. Bug #748.
// 2003 Jan 31: Added the ability to imbed RRA information in poller packages.
// 2003 Jan 29: Added response times to certain monitors.
// 2002 Nov 12: Display DNS response time data in webUI.
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

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.opennms.protocols.dns.DNSAddressRequest;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the DNS service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
@Distributable
final public class DnsMonitor extends IPv4Monitor {
    /**
     * Default DNS port.
     */
    private static final int DEFAULT_PORT = 53;

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
     * Default list of fatal response codes. Original behavior was hard-coded
     * so that only a ServFail(2) was fatal, so make that the configurable
     * default even though it makes little sense.
     */
    private static final int[] DEFAULT_FATAL_RESP_CODES = { 2 };

    /**
     * <P>
     * Poll the specified address for DNS service availability.
     * </P>
     * 
     * <P>
     * During the poll an DNS address request query packet is generated for
     * hostname 'localhost'. The query is sent via UDP socket to the interface
     * at the specified port (by default UDP port 53). If a response is
     * received, it is parsed and validated. If the DNS lookup was successful
     * the service status is set to SERVICE_AVAILABLE and the method returns.
     * </P>
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used for
     *            this poll.
     * @param iface
     *            The network interface to test the service on.
     * @return The availibility of the interface and if a transition event
     *         should be supressed.
     * 
     */
    public PollStatus poll(MonitoredService svc, Map parameters) {
        NetworkInterface iface = svc.getNetInterface();

        //
        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");

        // get the parameters
        //
        TimeoutTracker timeoutTracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
        int port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);

        // Host to lookup?
        //
        String lookup = ParameterMap.getKeyedString(parameters, "lookup", null);
        if (lookup == null || lookup.length() == 0) {
            // Get hostname of local machine for future DNS lookups
            //
            try {
                lookup = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ukE) {
                // Recast the exception as a Service Monitor Exception
                //
                ukE.fillInStackTrace();
                throw new UndeclaredThrowableException(ukE);
            }
        }
        
        // get the address and DNS address request
        //
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();
        DNSAddressRequest request = new DNSAddressRequest(lookup);

        // List of fatal response codes?
        //
        int[] fatalCodes = ParameterMap.getKeyedIntegerArray(parameters, "fatal-response-codes", DEFAULT_FATAL_RESP_CODES);
        if (fatalCodes != DEFAULT_FATAL_RESP_CODES) {
            List<Integer> codeList = new ArrayList<Integer>();
            for (int code : fatalCodes) {
                codeList.add(code);
            }
            request.setFatalResponseCodes(codeList);
        }
        
        PollStatus serviceStatus = PollStatus.unavailable();
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(timeoutTracker.getSoTimeout()); // will force the InterruptedIOException

            for (timeoutTracker.reset(); timeoutTracker.shouldRetry() && !serviceStatus.isAvailable(); timeoutTracker.nextAttempt()) {
                try {
                    // Send DNS request
                    //
                    byte[] data = request.buildRequest();
                    DatagramPacket outgoing = new DatagramPacket(data, data.length, ipv4Addr, port);
                    
                    byte[] buffer = new byte[512];
                    DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);

                    timeoutTracker.startAttempt();

                    socket.send(outgoing);

                    // Get DNS Response
                    socket.receive(incoming);

                    double responseTime = timeoutTracker.elapsedTimeInMillis();

                    // Validate DNS Response
                    // IOException thrown if packet does not decode as expected.
                    request.verifyResponse(incoming.getData(), incoming.getLength());
                    
                    serviceStatus = logUp(Level.DEBUG, responseTime, "valid DNS request received, responseTime= " + responseTime + "ms");

                } catch (InterruptedIOException ex) {
                    // Ignore, no response received.
                } catch (NoRouteToHostException e) {
                    serviceStatus = logDown(Level.DEBUG, "No route to host exception for address: " + ipv4Addr, e);
                } catch (ConnectException e) {
                    serviceStatus = logDown(Level.DEBUG, "Connection exception for address: " + ipv4Addr, e);
                } catch (IOException ex) {
                    serviceStatus = logDown(Level.DEBUG, "IOException while polling address: " + ipv4Addr, ex);
                }
            }
        } catch (IOException ex) {
            serviceStatus = logDown(Level.DEBUG, "Failed to create Datagram Socket for : " + ipv4Addr, ex);
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