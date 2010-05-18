//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc. All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2004 May 05: Remove use of SocketChannel and use timed Socket.connect
// 2004 Apr 27: Created this file.
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

package org.opennms.netmgt.capsd;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;

// TODO need to completely javadoc this class

/**
 * Implements the basic functionality of a Tcp based servicethat can be
 * discovered by OpenNMS. It extends the AbstractPlugin class and provides
 * methods for creating the sockets and dealing with timeouts and reteries.
 * 
 * @author Matt Brozowski
 * 
 */
public abstract class AbstractTcpPlugin extends AbstractPlugin {

    int m_defaultPort;

    int m_defaultRetry;

    int m_defaultTimeout;

    String m_pluginName;

    String m_protocolName;

    protected AbstractTcpPlugin(String protocol, int defaultTimeout, int defaultRetry) {
        this(protocol, -1, defaultTimeout, defaultRetry);
    }

    protected AbstractTcpPlugin(String protocol, int defaultPort, int defaultTimeout, int defaultRetry) {
        super();
        if (protocol == null)
            throw new NullPointerException("protocol is null");

        m_protocolName = protocol;
        m_defaultPort = defaultPort;
        m_defaultTimeout = defaultTimeout;
        m_defaultRetry = defaultRetry;
    }

    /**
     * <P>
     * Test to see if the passed host-port pair is the endpoint for an Citrix
     * server. If there is an Citrix server at that destination then a value of
     * true is returned from the method. Otherwise a false value is returned to
     * the caller.
     * </P>
     * 
     * @param config
     * 
     * @return True if server supports Citrix on the specified port, false
     *         otherwise
     */
    final protected boolean checkConnection(ConnectionConfig config) {
        // get a log to send errors
        //
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        // don't let the user set the timeout to 0, an infinite loop will occur
        // if the server is down
        int timeout = (config.getTimeout() == 0 ? 10 : config.getTimeout());

        boolean isAServer = false;
        for (int attempts = 0; attempts <= config.getRetry() && !isAServer; attempts++) {

            if (!preconnectCheck(config)) {
                // No chance of supporting this protocol just bail
                break;
            }

            Socket socket = null;
            try {

                // create a connected socket
                //
                socket = new Socket();
                socket.connect(config.getSocketAddress(), timeout);
                socket.setSoTimeout(timeout);
                log.debug(getPluginName() + ": connected to host: " + config.getInetAddress() + " on port: " + config.getPort());

                socket = wrapSocket(socket, config);

                isAServer = checkProtocol(socket, config);

            } catch (ConnectException cE) {
                // Connection refused!! Continue to retry.
                //
                log.debug(getPluginName() + ": connection refused to " + config.getInetAddress().getHostAddress() + ":" + config.getPort());
                isAServer = false;
            } catch (NoRouteToHostException e) {
                // No route to host!! No need to perform retries.
                e.fillInStackTrace();
                log.info(getPluginName() + ": Unable to test host " + config.getInetAddress().getHostAddress() + ", no route available", e);
                isAServer = false;
                throw new UndeclaredThrowableException(e);
            } catch (InterruptedIOException e) {
                log.debug(getPluginName() + ": did not connect to host within timeout: " + timeout + " attempt: " + attempts);
                isAServer = false;
            } catch (IOException e) {
                log.info(getPluginName() + ": Error communicating with host " + config.getInetAddress().getHostAddress(), e);
                isAServer = false;
            } catch (Throwable t) {
                log.warn(getPluginName() + ": Undeclared throwable exception caught contacting host " + config.getInetAddress().getHostAddress(), t);
                isAServer = false;
            } finally {
                if (socket != null)
                    closeSocket(socket, config);
            }
        }

        //
        // return the success/failure of this
        // attempt to contact an ftp server.
        //
        return isAServer;
    }

    protected void closeSocket(Socket socket, ConnectionConfig config) {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {

        }
    }

    protected abstract boolean checkProtocol(Socket socket, ConnectionConfig config) throws Exception;

    protected ConnectionConfig createConnectionConfig(InetAddress address, int port) {
        return new ConnectionConfig(address, port);
    }

    protected List<ConnectionConfig> getConnectionConfigList(Map<String, Object> qualifiers, InetAddress address) {
        if (m_defaultPort == -1)
            throw new IllegalStateException("m_defaultPort == -1");

        int port = getKeyedInteger(qualifiers, "port", m_defaultPort);
        return Collections.singletonList(createConnectionConfig(address, port));
    }

    final protected int getKeyedInteger(Map<String, Object> qualifiers, String key, int defaultVal) {
        if (qualifiers == null)
            return defaultVal;
        else
            return ParameterMap.getKeyedInteger(qualifiers, key, defaultVal);
    }

    final protected int[] getKeyedIntegerArray(Map<String, Object> qualifiers, String key, int[] defaultVal) {
        if (qualifiers == null)
            return defaultVal;
        else
            return ParameterMap.getKeyedIntegerArray(qualifiers, key, defaultVal);
    }

    /**
     * @return Returns the pluginName.
     */
    final public String getPluginName() {
        if (m_pluginName == null) {
            String fullName = this.getClass().getName();
            int idx = fullName.lastIndexOf('.');
            m_pluginName = (idx < 0 ? fullName : fullName.substring(idx + 1));
        }
        return m_pluginName;
    }

    /**
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     * 
     * @return The protocol name for this plugin.
     */
    final public String getProtocolName() {
        return m_protocolName;
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * 
     * @param address
     *            The address to check for support.
     * 
     * @return True if the protocol is supported by the address.
     */
    final public boolean isProtocolSupported(InetAddress address) {
        return isProtocolSupported(address, null);
    }

    /**
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     * The qualifier map passed to the method is used by the plugin to return
     * additional information by key-name. These key-value pairs can be added to
     * service events if needed.
     * 
     * @param address
     *            The address to check for support.
     * @param qualifiers
     *            The map where qualification are set by the plugin.
     * 
     * @return True if the protocol is supported by the address.
     */
    final public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {

        List<ConnectionConfig> connList = getConnectionConfigList(qualifiers, address);

        for(ConnectionConfig config : connList) {
            populateConnectionConfig(config, qualifiers);
            if (checkConnection(config)) {
                if (qualifiers != null)
                    saveConfig(qualifiers, config);
                return true;
            }
        }

        return false;

    }

    protected void populateConnectionConfig(ConnectionConfig config, Map<String, Object> qualifiers) {
        config.setQualifiers(qualifiers);
        config.setTimeout(getKeyedInteger(qualifiers, "timeout", m_defaultTimeout));
        config.setRetry(getKeyedInteger(qualifiers, "retry", m_defaultRetry));
    }

    protected boolean preconnectCheck(ConnectionConfig config) {
        return true;
    }

    protected void saveConfig(Map<String, Object> qualifiers, ConnectionConfig config) {
        saveKeyedInteger(qualifiers, "port", config.getPort());
    }

    final protected void saveKeyedInteger(Map<String, Object> qualifiers, String key, int value) {
        if (qualifiers != null && !qualifiers.containsKey(key))
            qualifiers.put(key, new Integer(value));
    }

    /**
     * @param pluginName
     *            The pluginName to set.
     */
    final public void setPluginName(String pluginName) {
        m_pluginName = pluginName;
    }

    protected Socket wrapSocket(Socket socket, ConnectionConfig config) throws Exception {
        return socket;
    }
}