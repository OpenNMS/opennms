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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.util.Map;
import java.util.StringTokenizer;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.AbstractPlugin;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existance of MS Exchange server on remote interfaces. The class implements
 * the Plugin interface that allows it to be used along with other plugins by
 * the daemon.
 * </P>
 *
 * @author <a href="mailto:mike@opennms.org">Mike</a>
 * @author <a href="mailto:weave@oculan.com">Weave</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public final class Pop3Plugin extends AbstractPlugin {

    /**
     * <P>
     * The capability name for the plugin.
     * </P>
     */
    private static final String PROTOCOL_NAME = "POP3";

    /**
     * <P>
     * The default port on which to check for POP3 service.
     * </P>
     */
    private static final int DEFAULT_PORT = 110;

    /**
     * Default number of retries.
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds).
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds

    /**
     * <P>
     * Test to see if the passed host is running Microsoft Exchange server. If
     * the remote host is running POP3, IMAP or MAPI and we are able to retreive
     * a banner from any of the ports these services listen on wich include the
     * text "Microsoft Exchange" then this method will return true. Otherwise a
     * false value is returned to the caller.
     * </P>
     * 
     * @param host
     *            The remote host to test.
     * @param port
     *            The remote port to test.
     * 
     * @return True if server is running MS Exchange, false otherwise
     */
    private boolean isServer(InetAddress host, int port, int retries, int timeout) {
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        boolean isAServer = false;
        for (int attempts = 0; attempts <= retries && !isAServer; attempts++) {
            Socket socket = null;

            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), timeout);
                socket.setSoTimeout(timeout);
                log.debug("Pop3Plugin: connected to host: " + host + " on port: " + port);

                // Allocate a line reader
                //
                BufferedReader lineRdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Tokenize the Banner Line, and check the first
                // line for a valid return.
                //
                // Server response should start with: "+OK"
                //
                StringTokenizer t = new StringTokenizer(lineRdr.readLine());
                if (t.nextToken().equals("+OK")) {
                    // POP3 server should recognize the QUIT command
                    //
                    String cmd = "QUIT\r\n";
                    socket.getOutputStream().write(cmd.getBytes());

                    //
                    // Token the response to the QUIT command
                    //
                    // Server response should start with: "+OK"
                    //
                    t = new StringTokenizer(lineRdr.readLine());
                    if (t.nextToken().equals("+OK"))
                        isAServer = true;
                }
            } catch (ConnectException cE) {
                // Connection refused!! Continue to retry.
                //
                log.debug("Pop3Plugin: Connection refused to " + InetAddressUtils.str(host) + ":" + port);
                isAServer = false;
            } catch (NoRouteToHostException e) {
                // No Route to host!!!
                //
                e.fillInStackTrace();
                log.info("Pop3Plugin: No route to host " + InetAddressUtils.str(host) + " was available", e);
                isAServer = false;
                throw new UndeclaredThrowableException(e);
            } catch (InterruptedIOException e) {
                // expected exception
                log.debug("Pop3Plugin: did not connect to host within timeout: " + timeout + " attempt: " + attempts);
                isAServer = false;
            } catch (IOException e) {
                isAServer = false;
                log.info("Pop3Plugin: An unexpected I/O exception occured contacting host " + InetAddressUtils.str(host), e);
            } catch (Throwable t) {
                isAServer = false;
                log.error("Pop3Plugin: An undeclared throwable exception was caught contacting host " + InetAddressUtils.str(host), t);
            } finally {
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                }
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
        return isServer(address, DEFAULT_PORT, DEFAULT_RETRY, DEFAULT_TIMEOUT);
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
        int retries = DEFAULT_RETRY;
        int timeout = DEFAULT_TIMEOUT;
        int port = DEFAULT_PORT;

        if (qualifiers != null) {
            retries = ParameterMap.getKeyedInteger(qualifiers, "retry", DEFAULT_RETRY);
            timeout = ParameterMap.getKeyedInteger(qualifiers, "timeout", DEFAULT_TIMEOUT);
            port = ParameterMap.getKeyedInteger(qualifiers, "port", DEFAULT_PORT);
        }

        boolean result = isServer(address, port, retries, timeout);
        if (result && qualifiers != null && !qualifiers.containsKey("port"))
            qualifiers.put("port", port);

        return result;
    }

}
