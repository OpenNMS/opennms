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

package org.opennms.netmgt.capsd.plugins;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.protocols.dns.DNSAddressRequest;

/**
 * This plugin is used to check a host for DNS (Domain Name Server) support.
 * This is done by contacting the specified host and requesting the default
 * address or <em>localhost</em>. If a valid resposne is returned then the
 * server is considered a DNS server.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public final class DnsPlugin extends AbstractPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(DnsPlugin.class);
    /**
     * </P>
     * The protocol name that is tested by this plugin.
     * </P>
     */
    private final static String PROTOCOL_NAME = "DNS";

    /**
     * </P>
     * The default port on which the host is checked to see if it supports DNS.
     * </P>
     */
    private final static int DEFAULT_PORT = 53;

    /**
     * Default number of retries for DNS requests
     */
    private final static int DEFAULT_RETRY = 3;

    /**
     * Default timeout (in milliseconds) for DNS requests.
     */
    private final static int DEFAULT_TIMEOUT = 3000; // in milliseconds

    /**
     * Default DNS lookup
     */
    private final static String DEFAULT_LOOKUP = "localhost";

    /**
     * 
     * @param nserver
     *            The address for the name server test.
     * @param port
     *            The port to test for name resolution
     * @param timeout
     *            Timeout in milliseconds
     * @param lookup
     *            Host name to be used in DNS lookup packet
     * 
     * @return True if server, false if not.
     */
    private boolean isServer(InetAddress nserver, int port, int retries, int timeout, String lookup) {
        boolean isAServer = false;

        // Allocate a communication socket
        //
        DatagramSocket socket = null;
        try {
            // Allocate a socket
            //
            socket = new DatagramSocket();
            socket.setSoTimeout(timeout);

            // Allocate a receive buffer
            //
            byte[] data = new byte[512];

            for (int count = 0; count < retries && !isAServer; count++) {
                try {
                    // Construct a new DNS Address Request
                    //
                    DNSAddressRequest request = new DNSAddressRequest(lookup);

                    // build the datagram packet used to request the address.
                    //
                    byte[] rdata = request.buildRequest();
                    DatagramPacket outpkt = new DatagramPacket(rdata, rdata.length, nserver, port);
                    rdata = null;

                    // send the output packet
                    //
                    socket.send(outpkt);

                    // receive a resposne
                    //
                    DatagramPacket inpkt = new DatagramPacket(data, data.length);
                    socket.receive(inpkt);
                    if (inpkt.getAddress().equals(nserver)) {
                        try {
                            request.verifyResponse(inpkt.getData(), inpkt.getLength());
                            isAServer = true;
                        } catch (IOException ex) {
                            LOG.debug("Failed to match response to request, an IOException occured", ex);
                        }
                    }
                } catch (InterruptedIOException ex) {
                    // discard this exception, do next loop
                    //
                }
            }
        } catch (IOException ex) {
            LOG.warn("isServer: An I/O exception during DNS resolution test.", ex);
        } finally {
            if (socket != null)
                socket.close();
        }

        return isAServer;
    }

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     *
     * @return The protocol name for this plugin.
     */
    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address) {
        return isServer(address, DEFAULT_PORT, DEFAULT_RETRY, DEFAULT_TIMEOUT, DEFAULT_LOOKUP);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     * </p>
     *
     * <p>
     * In addition, the input qualifiers map also provides information about how
     * the plugin should contact the remote server. The plugin may check the
     * qualifier map for specific elements and then adjust its behavior as
     * necessary
     * </p>
     */
    @Override
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        int port = DEFAULT_PORT;
        int timeout = DEFAULT_TIMEOUT;
        int retries = DEFAULT_RETRY;
        String lookup = DEFAULT_LOOKUP;
        if (qualifiers != null) {
            port = ParameterMap.getKeyedInteger(qualifiers, "port", DEFAULT_PORT);
            timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
            retries = ParameterMap.getKeyedInteger(qualifiers, "retry", ParameterMap.getKeyedInteger(qualifiers, "retries", DEFAULT_RETRY));
            lookup = ParameterMap.getKeyedString(qualifiers, "lookup", DEFAULT_LOOKUP);
        }

        boolean result = isServer(address, port, retries, timeout, lookup);
        if (result && qualifiers != null && !qualifiers.containsKey("port"))
            qualifiers.put("port", port);

        return result;
    }
}
