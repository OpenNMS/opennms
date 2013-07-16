/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.dhcp.capsd;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.dhcpd.Dhcpd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test if a
 * remote interface is running a functional DHCP server as defined by RFC 2131.
 *
 * This class relies on the DHCP API provided by JDHCP v1.1.1. (please refer to
 * http://www.dhcp.org/javadhcp).
 *
 * The class implements the Plugin interface that allows it to be used along
 * with other plugins by the daemon.
 * </P>
 *
 * @author <a href="mailto:mike@opennms.org">Mike</a>
 * @author <a href="mailto:weave@oculan.com">Weave</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public final class DhcpPlugin extends AbstractPlugin {
	
	private static final Logger LOG = LoggerFactory.getLogger(DhcpPlugin.class);

    /**
     * The port where the DHCP server is detected. This is a well known port and
     * this integer is always returned in the qualifier map.
     */
    private final static Integer PORT_NUMBER = 67;

    /**
     * <P>
     * The protocol name of the plugin.
     * </P>
     */
    private final static String PROTOCOL_NAME = "DHCP";

    /**
     * Default number of retries for DHCP requests
     */
    private final static int DEFAULT_RETRY = 3;

    /**
     * Default timeout (in milliseconds) for DHCP requests
     */
    private final static int DEFAULT_TIMEOUT = 3000; // in milliseconds

    /**
     * This method is used to test a passed address for DHCP server support. If
     * the target system is running a DHCP server and responds to the request
     * then a value of true is returned.
     * 
     * @param host
     *            The host address to check
     * @param retries
     *            The maximum number of attempts to try.
     * @param timeout
     *            The time to wait for a response to each request.
     * 
     * @return True if the remote host supports DHCP.
     */
    private boolean isServer(InetAddress host, int retries, int timeout) {
        boolean isAServer = false;
        long responseTime = -1;

        try {
            // Dhcpd.isServer() returns the response time in milliseconds
            // if the remote host is a DHCP server or -1 if the remote
            // host is not a DHCP server.
            responseTime = Dhcpd.isServer(host, timeout, retries);
        } catch (final InterruptedIOException ioE) {
            ioE.fillInStackTrace();
            LOG.debug("isServer: The DHCP discovery operation was interrupted", ioE);
        } catch (final IOException ioE) {
            LOG.warn("isServer: An I/O exception occured during DHCP discovery", ioE);
            isAServer = false;
        } catch (final Throwable t) {
            LOG.error("isServer: An undeclared throwable exception was caught during test", t);
            isAServer = false;
        }

        // If response time is equal to or greater than zero
        // the remote host IS a DHCP server.
        if (responseTime >= 0)
            isAServer = true;

        // return the success/failure of this
        // attempt to contact a DHCP server.
        //
        return isAServer;
    }

    /**
     * This method returns the name of the protocol supported by this plugin.
     *
     * @return The name of the protocol for the plugin.
     */
    @Override
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * This method is used to test the passed host for DHCP server support. The
     * remote host is queried using the DHCP protocol by sending a formatted
     * datagram to the DHCP server port. If a response is received by the DHCP
     * listenter that matches our original request then a value of true is
     * returned to the caller.
     */
    @Override
    public boolean isProtocolSupported(InetAddress host) {
        return isServer(host, DEFAULT_RETRY, DEFAULT_TIMEOUT);
    }

    /**
     * {@inheritDoc}
     *
     * This method is used to test the passed host for DHCP server support. The
     * remote host is queried using the DHCP protocol by sending a formatted
     * datagram to the DHCP server port. If a response is received by the DHCP
     * listenter that matches our original request then a value of true is
     * returned to the caller.
     */
    @Override
    public boolean isProtocolSupported(InetAddress host, Map<String, Object> qualifiers) {
        int retries = DEFAULT_RETRY;
        int timeout = DEFAULT_TIMEOUT;

        if (qualifiers != null) {
            retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
            timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
        }

        boolean isAServer = isServer(host, retries, timeout);
        if (isAServer && qualifiers != null)
            qualifiers.put("port", PORT_NUMBER);

        return isAServer;
    }
}
