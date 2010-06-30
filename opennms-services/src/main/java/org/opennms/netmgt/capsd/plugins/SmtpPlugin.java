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
import java.net.UnknownHostException;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.AbstractPlugin;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existance of an SMTP server on remote interfaces. The class implements the
 * Plugin interface that allows it to be used along with other plugins by the
 * daemon.
 * </P>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * @version $Id: $
 */
public final class SmtpPlugin extends AbstractPlugin {

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
    private static String LOCALHOST_NAME;

    static {
        try {
            LOCALHOST_NAME = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException uhE) {
            ThreadCategory.getInstance(SmtpPlugin.class).error("Failed to resolve localhost name, using localhost");
            LOCALHOST_NAME = "localhost";
        }

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
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        boolean isAServer = false;
        for (int attempts = 0; attempts <= retries && !isAServer; attempts++) {
            Socket socket = null;
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), timeout);
                socket.setSoTimeout(timeout);
                log.debug("SmtpPlugin: connected to host: " + host + " on port: " + port);

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
                    log.info("Received truncated response from SMTP server " + host.getHostAddress());
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
                        log.info("Received truncated response from SMTP server " + host.getHostAddress());
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
                            log.info("Received truncated response from SMTP server " + host.getHostAddress());
                            continue;
                        }

                        t = new StringTokenizer(result);
                        rc = Integer.parseInt(t.nextToken());

                        if (rc == 221)
                            isAServer = true;
                    }
                }
            } catch (NumberFormatException e) {
                log.info("SmtpPlugin: received invalid result code from server " + host.getHostAddress(), e);
                isAServer = false;
            } catch (ConnectException cE) {
                // Connection refused!! Continue to retry.
                //
                log.debug("SmtpPlugin: connection refused to " + host.getHostAddress() + ":" + port);
                isAServer = false;
            } catch (NoRouteToHostException e) {
                // No route to host!! No need to perform retries.
                e.fillInStackTrace();
                log.info("SmtpPlugin: Unable to test host " + host.getHostAddress() + ", no route available", e);
                isAServer = false;
                throw new UndeclaredThrowableException(e);
            } catch (InterruptedIOException e) {
                log.debug("SmtpPlugin: did not connect to host within timeout: " + timeout + " attempt: " + attempts);
                isAServer = false;
            } catch (IOException e) {
                log.info("SmtpPlugin: Error communicating with host " + host.getHostAddress(), e);
                isAServer = false;
            } catch (Throwable t) {
                log.warn("SmtpPlugin: Undeclared throwable exception caught contacting host " + host.getHostAddress(), t);
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
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If the
     * protocol is not supported then a false value is returned to the caller.
     */
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
