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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;

// TODO need to completely javadoc this class

/**
 * Implements the basic functionality of a Tcp based servicethat can be
 * discovered by OpenNMS. It extends the AbstractPlugin class and provides
 * methods for creating the sockets and dealing with timeouts and reteries.
 *
 * @author Matt Brozowski
 * @version $Id: $
 */
public abstract class AbstractTcpPlugin extends AbstractPlugin {

    int m_defaultPort;

    int m_defaultRetry;

    int m_defaultTimeout;

    String m_pluginName;

    String m_protocolName;

    /**
     * <p>Constructor for AbstractTcpPlugin.</p>
     *
     * @param protocol a {@link java.lang.String} object.
     * @param defaultTimeout a int.
     * @param defaultRetry a int.
     */
    protected AbstractTcpPlugin(String protocol, int defaultTimeout, int defaultRetry) {
        this(protocol, -1, defaultTimeout, defaultRetry);
    }

    /**
     * <p>Constructor for AbstractTcpPlugin.</p>
     *
     * @param protocol a {@link java.lang.String} object.
     * @param defaultPort a int.
     * @param defaultTimeout a int.
     * @param defaultRetry a int.
     */
    protected AbstractTcpPlugin(String protocol, int defaultPort, int defaultTimeout, int defaultRetry) {
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
     * @param config a {@link org.opennms.netmgt.capsd.ConnectionConfig} object.
     * @return True if server supports Citrix on the specified port, false
     *         otherwise
     */
    final protected boolean checkConnection(ConnectionConfig config) {
        // get a log to send errors
        //
        Category log = ThreadCategory.getInstance(getClass());

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

    /**
     * <p>closeSocket</p>
     *
     * @param socket a {@link java.net.Socket} object.
     * @param config a {@link org.opennms.netmgt.capsd.ConnectionConfig} object.
     */
    protected void closeSocket(Socket socket, ConnectionConfig config) {
        try {
            if (socket != null)
                socket.close();
        } catch (IOException e) {

        }
    }

    /**
     * <p>checkProtocol</p>
     *
     * @param socket a {@link java.net.Socket} object.
     * @param config a {@link org.opennms.netmgt.capsd.ConnectionConfig} object.
     * @return a boolean.
     * @throws java.lang.Exception if any.
     */
    protected abstract boolean checkProtocol(Socket socket, ConnectionConfig config) throws Exception;

    /**
     * <p>createConnectionConfig</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param port a int.
     * @return a {@link org.opennms.netmgt.capsd.ConnectionConfig} object.
     */
    protected ConnectionConfig createConnectionConfig(InetAddress address, int port) {
        return new ConnectionConfig(address, port);
    }

    /**
     * <p>getConnectionConfigList</p>
     *
     * @param qualifiers a {@link java.util.Map} object.
     * @param address a {@link java.net.InetAddress} object.
     * @return a {@link java.util.List} object.
     */
    protected List<ConnectionConfig> getConnectionConfigList(Map<String, Object> qualifiers, InetAddress address) {
        if (m_defaultPort == -1)
            throw new IllegalStateException("m_defaultPort == -1");

        int port = getKeyedInteger(qualifiers, "port", m_defaultPort);
        return Collections.singletonList(createConnectionConfig(address, port));
    }

    /**
     * <p>getKeyedInteger</p>
     *
     * @param qualifiers a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param defaultVal a int.
     * @return a int.
     */
    final protected int getKeyedInteger(Map<String, Object> qualifiers, String key, int defaultVal) {
        if (qualifiers == null)
            return defaultVal;
        else
            return ParameterMap.getKeyedInteger(qualifiers, key, defaultVal);
    }

    /**
     * <p>getKeyedIntegerArray</p>
     *
     * @param qualifiers a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param defaultVal an array of int.
     * @return an array of int.
     */
    final protected int[] getKeyedIntegerArray(Map<String, Object> qualifiers, String key, int[] defaultVal) {
        if (qualifiers == null)
            return defaultVal;
        else
            return ParameterMap.getKeyedIntegerArray(qualifiers, key, defaultVal);
    }

    /**
     * <p>getPluginName</p>
     *
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
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     */
    final public boolean isProtocolSupported(InetAddress address) {
        return isProtocolSupported(address, null);
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

    /**
     * <p>populateConnectionConfig</p>
     *
     * @param config a {@link org.opennms.netmgt.capsd.ConnectionConfig} object.
     * @param qualifiers a {@link java.util.Map} object.
     */
    protected void populateConnectionConfig(ConnectionConfig config, Map<String, Object> qualifiers) {
        config.setQualifiers(qualifiers);
        config.setTimeout(getKeyedInteger(qualifiers, "timeout", m_defaultTimeout));
        config.setRetry(getKeyedInteger(qualifiers, "retry", m_defaultRetry));
    }

    /**
     * <p>preconnectCheck</p>
     *
     * @param config a {@link org.opennms.netmgt.capsd.ConnectionConfig} object.
     * @return a boolean.
     */
    protected boolean preconnectCheck(ConnectionConfig config) {
        return true;
    }

    /**
     * <p>saveConfig</p>
     *
     * @param qualifiers a {@link java.util.Map} object.
     * @param config a {@link org.opennms.netmgt.capsd.ConnectionConfig} object.
     */
    protected void saveConfig(Map<String, Object> qualifiers, ConnectionConfig config) {
        saveKeyedInteger(qualifiers, "port", config.getPort());
    }

    /**
     * <p>saveKeyedInteger</p>
     *
     * @param qualifiers a {@link java.util.Map} object.
     * @param key a {@link java.lang.String} object.
     * @param value a int.
     */
    final protected void saveKeyedInteger(Map<String, Object> qualifiers, String key, int value) {
        if (qualifiers != null && !qualifiers.containsKey(key))
            qualifiers.put(key, new Integer(value));
    }

    /**
     * <p>setPluginName</p>
     *
     * @param pluginName
     *            The pluginName to set.
     */
    final public void setPluginName(String pluginName) {
        m_pluginName = pluginName;
    }

    /**
     * <p>wrapSocket</p>
     *
     * @param socket a {@link java.net.Socket} object.
     * @param config a {@link org.opennms.netmgt.capsd.ConnectionConfig} object.
     * @return a {@link java.net.Socket} object.
     * @throws java.lang.Exception if any.
     */
    protected Socket wrapSocket(Socket socket, ConnectionConfig config) throws Exception {
        return socket;
    }
}
