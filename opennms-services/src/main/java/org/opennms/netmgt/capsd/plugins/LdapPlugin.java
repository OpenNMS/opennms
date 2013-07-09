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
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.Map;

import org.opennms.core.utils.DefaultSocketWrapper;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.SocketWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.utils.TimeoutSocketFactory;
import org.opennms.netmgt.capsd.AbstractPlugin;

import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSocketFactory;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existance of an LDAP server on remote interfaces. The class implements the
 * Plugin interface that allows it to be used along with other plugins by the
 * daemon.
 * </P>
 *
 * @author <a href="mailto:jason@opennms.org">Jason Johns</a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public class LdapPlugin extends AbstractPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(LdapPlugin.class);

    private static final String PROTOCOL_NAME = "LDAP";

    /**
     * <P>
     * The default ports on which the host is checked to see if it supports
     * LDAP.
     * </P>
     */
    private static final int[] DEFAULT_PORTS = { LDAPConnection.DEFAULT_PORT };

    /**
     * Default number of retries for HTTP requests.
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for HTTP requests.
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds

    /**
     * A class to add a timeout to the socket that the LDAP code uses to access
     * an LDAP server
     */
    private class TimeoutLDAPSocket extends TimeoutSocketFactory implements LDAPSocketFactory {
        public TimeoutLDAPSocket(int timeout) {
            super(timeout, getSocketWrapper());
        }
    }

    protected SocketWrapper getSocketWrapper() {
        return new DefaultSocketWrapper();
    }

    protected int[] determinePorts(final Map<String, Object> parameters) {
        return ParameterMap.getKeyedIntegerArray(parameters, "port", DEFAULT_PORTS);
    }

    /**
     * <P>
     * Test to see if the passed host-port pair is the endpoint for an LDAP
     * server. If there is an LDAP server at that destination then a value of
     * true is returned from the method. Otherwise a false value is returned to
     * the caller.
     * </P>
     * 
     * @param host
     *            The remote host to connect to.
     * @param port
     *            The remote port to connect to.
     * 
     * @return True if server supports HTTP on the specified port, false
     *         otherwise
     */
    private boolean isServer(InetAddress host, int port, int retries, int timeout) {

        boolean isAServer = false;

        // first just try a connection to the box via socket. Just in case there
        // is
        // a no way to route to the address, don't iterate through the retries,
        // as a
        // NoRouteToHost exception will only be thrown after about 5 minutes,
        // thus tying
        // up the thread
        Socket socket = null;

        try {

            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), timeout);
            socket.setSoTimeout(timeout);

            LOG.debug("LDAPPlugin.isServer: connect successful");

            // now go ahead and attempt to determine if LDAP is on this host
            for (int attempts = 0; attempts <= retries && !isAServer; attempts++) {
                LOG.debug("LDAPPlugin.isServer: attempt {} to connect host {}", InetAddressUtils.str(host), attempts);
                LDAPConnection lc = null;
                try {
                    lc = new LDAPConnection(new TimeoutLDAPSocket(timeout));
                    lc.connect(InetAddressUtils.str(host), port);
                    isAServer = true;
                } catch (LDAPException e) {
                    isAServer = false;
                } finally {
                    try {
                        if (lc != null)
                            lc.disconnect();
                    } catch (LDAPException e) {
                    }
                }
            }
        } catch (ConnectException e) {
            // Connection refused!! No need to perform retries.
            //
            LOG.debug("{}: connection refused to {}:{}", getClass().getName(), InetAddressUtils.str(host), port);
        } catch (NoRouteToHostException e) {
            // No route to host!! No need to perform retries.
            e.fillInStackTrace();
            LOG.info("{}: No route to host {}", getClass().getName(), InetAddressUtils.str(host), e);
            throw new UndeclaredThrowableException(e);
        } catch (InterruptedIOException e) {
            // Connection failed, retry until attempts exceeded
            LOG.debug("LDAPPlugin: failed to connect within specified timeout");
        } catch (Throwable t) {
            LOG.warn("{}: An undeclared throwable exception caught contacting host {}", getClass().getName(), InetAddressUtils.str(host), t);
        } finally {
            try {
                // close the socket channel
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
            }
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
        for (int i = 0; i < DEFAULT_PORTS.length; i++) {
            if (isServer(address, DEFAULT_PORTS[i], DEFAULT_RETRY, DEFAULT_TIMEOUT))
                return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     */
    @Override
    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        int retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
        int timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
        int[] ports = determinePorts(qualifiers);

        for (int i = 0; i < ports.length; i++) {
            if (isServer(address, ports[i], retries, timeout)) {
                qualifiers.put("port", ports[i]);
                return true;
            }
        }

        return false;
    }
}
