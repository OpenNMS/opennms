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

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.capsd.AbstractPlugin;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existance of an SMTP server on remote interfaces. The class implements the
 * Plugin interface that allows it to be used along with other plugins by the
 * daemon.
 * </P>
 *
 * @author <a href="mailto:sowmya@opennms.org">Sowmya</a>
 * @author <a href="mailto:weave@oculan.com">Weave</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public final class SmtpPlugin extends AbstractPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(SmtpPlugin.class);

    /**
     * The regular expression test used to determine if the reply is a multi
     * line reply. A multi line reply is one that each line, but the last, is in
     * the form of "ddd-" where 'ddd' is the result code.
     * 
     */
    private static final RE MULTILINE_RESULT;

    /**
     * <P>
     * The capability name of the plugin.
     * </P>
     */
    private static final String PROTOCOL_NAME = "SMTP";

    /**
     * <P>
     * The default port on which the host is checked to see if it supports SMTP.
     * </P>
     */
    private static final int DEFAULT_PORT = 25;

    /**
     * Default number of retries for SMTP requests.
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for SMTP requests.
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds

    /**
     * The name of the local host.
     */
    private static final String LOCALHOST_NAME = InetAddressUtils.getLocalHostName();

    static {
        try {
            MULTILINE_RESULT = new RE("^[1-5][0-9]{2}-");
        } catch (RESyntaxException re) {
            throw new java.lang.reflect.UndeclaredThrowableException(re);
        }
    }

    /**
     * <P>
     * Test to see if the passed host-port pair is the endpoint for an SMTP
     * server. If there is an SMTP server at that destination then a value of
     * true is returned from the method. Otherwise a false value is returned to
     * the caller.
     * </P>
     * 
     * @param host
     *            The remote host to connect to.
     * @param port
     *            The remote port on the host.
     * 
     * @return True if server supports SMTP on the specified port, false
     *         otherwise
     */
    private boolean isServer(InetAddress host, int port, int retries, int timeout) {
        // get a log to send errors
        //

        boolean isAServer = false;
        for (int attempts = 0; attempts <= retries && !isAServer; attempts++) {
            Socket socket = null;
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), timeout);
                socket.setSoTimeout(timeout);
                LOG.debug("SmtpPlugin: connected to host: {} on port: {}", port, host);

                // Allocate a line reader
                //
                BufferedReader lineRdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Read responses from the server. The initial line should just
                // be a banner, but go ahead and check for multiline response.
                //
                String result = null;
                do {
                    result = lineRdr.readLine();

                } while (result != null && result.length() > 0 && MULTILINE_RESULT.match(result));

                if (result == null || result.length() == 0) {
                    LOG.info("Received truncated response from SMTP server {}", InetAddressUtils.str(host));
                    continue;
                }

                // Tokenize the last line result
                //
                StringTokenizer t = new StringTokenizer(result);
                int rc = Integer.parseInt(t.nextToken());
                if (rc == 220) {
                    //
                    // Send the HELO command
                    //
                    String cmd = "HELO " + LOCALHOST_NAME + "\r\n";
                    socket.getOutputStream().write(cmd.getBytes());

                    // Response from HELO command may be a multi-line response
                    // (but
                    // most likely will be single-line)..
                    // We are expecting to get a response with an integer return
                    // code in the first token. We can't ge sure that the first
                    // response will give us what we want. Consider the
                    // following
                    // reponse for example:
                    //
                    // 250-First line
                    // 250-Second line
                    // 250 Requested mail action okay, completed
                    //
                    // In this case the final line of the response contains the
                    // return
                    // code we are looking for.
                    do {
                        result = lineRdr.readLine();

                    } while (result != null && result.length() > 0 && MULTILINE_RESULT.match(result));

                    if (result == null || result.length() == 0) {
                        LOG.info("Received truncated response from SMTP server {}", InetAddressUtils.str(host));
                        continue;
                    }

                    t = new StringTokenizer(result);
                    rc = Integer.parseInt(t.nextToken());
                    if (rc == 250) {
                        //
                        // Send the QUIT command
                        //
                        cmd = "QUIT\r\n";
                        socket.getOutputStream().write(cmd.getBytes());

                        // Response from QUIT command may be a multi-line
                        // response.
                        // We are expecting to get a response with an integer
                        // return
                        // code in the first token. We can't ge sure that the
                        // first
                        // response will give us what we want. Consider the
                        // following
                        // reponse for example:
                        //
                        // 221-First line
                        // 221-Second line
                        // 221 <domain> Service closing transmission channel.
                        //
                        // In this case the final line of the response contains
                        // the return
                        // code we are looking for.
                        do {
                            result = lineRdr.readLine();

                        } while (result != null && result.length() > 0 && MULTILINE_RESULT.match(result));

                        if (result == null || result.length() == 0) {
                            LOG.info("Received truncated response from SMTP server {}", InetAddressUtils.str(host));
                            continue;
                        }

                        t = new StringTokenizer(result);
                        rc = Integer.parseInt(t.nextToken());

                        if (rc == 221)
                            isAServer = true;
                    }
                }
            } catch (NumberFormatException e) {
                LOG.info("SmtpPlugin: received invalid result code from server {}", InetAddressUtils.str(host), e);
                isAServer = false;
            } catch (ConnectException cE) {
                // Connection refused!! Continue to retry.
                //
                LOG.debug("SmtpPlugin: connection refused to {}: {}", port, InetAddressUtils.str(host));
                isAServer = false;
            } catch (NoRouteToHostException e) {
                // No route to host!! No need to perform retries.
                e.fillInStackTrace();
                LOG.info("SmtpPlugin: Unable to test host {}, no route available", InetAddressUtils.str(host), e);
                isAServer = false;
                throw new UndeclaredThrowableException(e);
            } catch (InterruptedIOException e) {
                LOG.debug("SmtpPlugin: did not connect to host within timeout: {} attempt: {}", attempts, timeout);
                isAServer = false;
            } catch (IOException e) {
                LOG.info("SmtpPlugin: Error communicating with host {}", InetAddressUtils.str(host), e);
                isAServer = false;
            } catch (Throwable t) {
                LOG.warn("SmtpPlugin: Undeclared throwable exception caught contacting host {}", InetAddressUtils.str(host), t);
                isAServer = false;
            } finally {
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                }
            }
        }

        //
        // return the success/failure of this
        // attempt to contact an SMTP server.
        //
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
