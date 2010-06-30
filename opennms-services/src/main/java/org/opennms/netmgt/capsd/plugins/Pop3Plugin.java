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
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existance of MS Exchange server on remote interfaces. The class implements
 * the Plugin interface that allows it to be used along with other plugins by
 * the daemon.
 * </P>
 *
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * @version $Id: $
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
        Category log = ThreadCategory.getInstance(getClass());

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
                log.debug("Pop3Plugin: Connection refused to " + host.getHostAddress() + ":" + port);
                isAServer = false;
            } catch (NoRouteToHostException e) {
                // No Route to host!!!
                //
                e.fillInStackTrace();
                log.info("Pop3Plugin: No route to host " + host.getHostAddress() + " was available", e);
                isAServer = false;
                throw new UndeclaredThrowableException(e);
            } catch (InterruptedIOException e) {
                // expected exception
                log.debug("Pop3Plugin: did not connect to host within timeout: " + timeout + " attempt: " + attempts);
                isAServer = false;
            } catch (IOException e) {
                isAServer = false;
                log.info("Pop3Plugin: An unexpected I/O exception occured contacting host " + host.getHostAddress(), e);
            } catch (Throwable t) {
                isAServer = false;
                log.error("Pop3Plugin: An undeclared throwable exception was caught contacting host " + host.getHostAddress(), t);
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
            qualifiers.put("port", new Integer(port));

        return result;
    }

}
