//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2004 May 05: Remove use of SocketChannel and use timed Socket.connect
// 2003 Jul 21: Explicitly close sockets.
// 2003 Jul 18: Fixed exception to enable retries.
// 2003 Jan 31: Cleaned up some unused imports.
// 2003 Jan 29: Added response time
// 2002 Nov 14: Used non-blocking I/O for speed improvements.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.capsd.plugins;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.model.discovery.IPAddress;

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
public final class LdapPlugin extends AbstractPlugin {

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
    private class TimeoutLDAPSocket implements LDAPSocketFactory {

        private int m_timeout;

        public TimeoutLDAPSocket(final int timeout) {
            m_timeout = timeout;
        }

        public Socket createSocket(final String host, final int port) throws IOException, UnknownHostException {
        	final Socket socket = new Socket(host, port);
            socket.setSoTimeout(m_timeout);
            return socket;
        }
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
    private boolean isServer(final IPAddress address, final int port, final int retries, final int timeout) {
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
            socket.connect(new InetSocketAddress(address.toInetAddress(), port), timeout);
            socket.setSoTimeout(timeout);

            LogUtils.debugf(this, "LDAPPlugin.isServer: connect successful");

            // now go ahead and attempt to determine if LDAP is on this host
            for (int attempts = 0; attempts <= retries && !isAServer; attempts++) {
            	LogUtils.debugf(this, "LDAPPlugin.isServer: attempt %d to connect to host %s", attempts, address);
                LDAPConnection lc = null;
                try {
                    lc = new LDAPConnection(new TimeoutLDAPSocket(timeout));
                    lc.connect(address.toDbString(), port);
                    isAServer = true;
                } catch (final LDAPException e) {
                	LogUtils.debugf(this, e, "Error while making LDAP connection.");
                    isAServer = false;
                } finally {
                    try {
                        if (lc != null)
                            lc.disconnect();
                    } catch (LDAPException e) {
                    	LogUtils.debugf(this, e, "Exception while closing LDAP socket.");
                    }
                }
            }
        } catch (final ConnectException e) {
            // Connection refused!! No need to perform retries.
        	LogUtils.debugf(this, e, "connection refused to %s:%d", address.toDbString(), port);
        } catch (final NoRouteToHostException e) {
            // No route to host!! No need to perform retries.
            e.fillInStackTrace();
            LogUtils.infof(this, e, "No route to host %s", address);
            throw new UndeclaredThrowableException(e);
        } catch (final InterruptedIOException e) {
            // Connection failed, retry until attempts exceeded
            LogUtils.debugf(this, "failed to connect to %s within specified timeout (%d)", address, timeout);
        } catch (final Throwable t) {
            LogUtils.warnf(this, t, "An undeclared throwable exception caught contacting host %s", address);
        } finally {
            try {
                // close the socket channel
                if (socket != null) socket.close();
            } catch (final IOException e) {
            	LogUtils.debugf(this, e, "Exception while closing LDAP socket.");
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
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     */
    public boolean isProtocolSupported(final IPAddress ipAddress) {
        for (int i = 0; i < DEFAULT_PORTS.length; i++) {
            if (isServer(ipAddress, DEFAULT_PORTS[i], DEFAULT_RETRY, DEFAULT_TIMEOUT))
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
    public boolean isProtocolSupported(final IPAddress ipAddress, final Map<String, Object> qualifiers) {
    	final int retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
        final int timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
        final int[] ports = ParameterMap.getKeyedIntegerArray(qualifiers, "port", DEFAULT_PORTS);

        for (int i = 0; i < ports.length; i++) {
            if (isServer(ipAddress, ports[i], retries, timeout)) {
                qualifiers.put("port", ports[i]);
                return true;
            }
        }

        return false;
    }
}
