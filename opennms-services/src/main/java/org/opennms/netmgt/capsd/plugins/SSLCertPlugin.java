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

import javax.net.ssl.SSLSocket;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.SocketWrapper;
import org.opennms.core.utils.SslSocketWrapper;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existance of an TCP server on remote interfaces. The class implements the
 * Plugin interface that allows it to be used along with other plugins by the
 * daemon.
 * </P>
 *
 * @author <a href="mailto:ronald.roskens@gmail.com">Ronald Roskens</a>
 */
public final class SSLCertPlugin extends AbstractPlugin {
    
    private static final Logger LOG = LoggerFactory.getLogger(SSLCertPlugin.class);

    /**
     * The protocol supported by the plugin
     */
    private final static String PROTOCOL_NAME = "SSLCert";

    /**
     * Default number of retries for TCP requests
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for TCP requests
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds

    /**
     * Default port to test for a valid SSL certificate.
     */
    private final static int DEFAULT_PORT = -1;

    public static final String PARAMETER_TIMEOUT = "timeout";
    public static final String PARAMETER_RETRY = "retry";
    public static final String PARAMETER_PORT = "port";

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
        throw new UnsupportedOperationException("Undirected SSL certificate checking not supported");
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
        int retries = ParameterMap.getKeyedInteger(qualifiers, PARAMETER_RETRY, DEFAULT_RETRY);
        int timeout = ParameterMap.getKeyedInteger(qualifiers, PARAMETER_TIMEOUT, DEFAULT_TIMEOUT);
        int port    = ParameterMap.getKeyedInteger(qualifiers, PARAMETER_PORT, DEFAULT_PORT);

        // verify the port
        //
        if (port == -1) {
            throw new IllegalArgumentException("The port must be specified when doing SSL certificate discovery");
        }

        boolean hasSSLCert = false;
        for (int attempts = 0; attempts <= retries && !hasSSLCert; attempts++) {
            Socket socket = null;
            try {
                // create a connected socket
                //
                socket = new Socket();
                socket.connect(new InetSocketAddress(address, port), timeout);
                socket.setSoTimeout(timeout);
                LOG.debug("Connected to host: {} on port: {}", address, port);
                SSLSocket sslSocket = (SSLSocket) getSocketWrapper().wrapSocket(socket);
                hasSSLCert = sslSocket.getSession().isValid();
            } catch (ConnectException e) {
                // Connection refused!! Continue to retry.
                //
                LOG.debug("Connection refused to {}:{}", InetAddressUtils.str(address), port);
                hasSSLCert = false;
            } catch (NoRouteToHostException e) {
                // No Route to host!!!
                //
                e.fillInStackTrace();
                LOG.info("Could not connect to host {}, no route to host", InetAddressUtils.str(address), e);
                hasSSLCert = false;
                throw new UndeclaredThrowableException(e);
            } catch (InterruptedIOException e) {
                // This is an expected exception
                //
                LOG.debug("Did not connect to host within timeout: {}, attempt: {}", timeout, attempts);
                hasSSLCert = false;
            } catch (IOException e) {
                LOG.info("An expected I/O exception occured connecting to host {} on port {}", InetAddressUtils.str(address), port, e);
                hasSSLCert = false;
            } catch (Throwable t) {
                hasSSLCert = false;
                LOG.warn("An undeclared throwable exception was caught connecting to host {} on port {}", InetAddressUtils.str(address), port, t);
            } finally {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        return hasSSLCert;
    }

    /**
     * <p>wrapSocket</p>
     *
     * @param socket a {@link java.net.Socket} object.
     * @return a {@link java.net.Socket} object.
     * @throws java.io.IOException if any.
     */
    protected SocketWrapper getSocketWrapper() {
        return new SslSocketWrapper();
    }
}
