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
import java.net.Socket;

import org.opennms.netmgt.capsd.AbstractTcpPlugin;
import org.opennms.netmgt.capsd.ConnectionConfig;

/**
 * <P>
 * This class is designed to be used by the capabilities daemon to test for the
 * existance of an IMAP server on remote interfaces. The class implements the
 * Plugin interface that allows it to be used along with other plugins by the
 * daemon.
 * </P>
 *
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</A>
 * @author <a href="mailto:weave@oculan.com">Weave</a>
 * @author <a href="http://www.opennms.org">OpenNMS</A>
 */
public final class ImapPlugin extends AbstractTcpPlugin {

    /**
     * <P>
     * The default port on which the host is checked to see if it supports IMAP.
     * </P>
     */
    private static final int DEFAULT_PORT = 143;

    /**
     * Default number of retries for IMAP requests
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for IMAP requests
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds

    /**
     * The BYE response received from the server in response to the logout
     */
    private static String IMAP_BYE_RESPONSE_PREFIX = "* BYE ";

    /**
     * The LOGOUT request sent to the server to close the connection
     */
    private static String IMAP_LOGOUT_REQUEST = "ONMSCAPSD LOGOUT\r\n";

    /**
     * The LOGOUT response received from the server in response to the logout
     */
    private static String IMAP_LOGOUT_RESPONSE_PREFIX = "ONMSCAPSD OK ";

    /**
     * The start of the initial banner received from the server
     */
    private static String IMAP_START_RESPONSE_PREFIX = "* OK ";

    /**
     * The name of the protocol supported by this plugin.
     */
    private static final String PROTOCOL_NAME = "IMAP";

    /**
     * <p>Constructor for ImapPlugin.</p>
     */
    public ImapPlugin() {
        super(PROTOCOL_NAME, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRY);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean checkProtocol(Socket socket, ConnectionConfig config) throws IOException {

        boolean isAServer = false;
        // Allocate a line reader
        //
        BufferedReader lineRdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        //
        // Check the banner line for a valid return.
        //
        String banner = lineRdr.readLine();
        if (banner != null && banner.startsWith(IMAP_START_RESPONSE_PREFIX)) {
            //
            // Send the LOGOUT
            //
            socket.getOutputStream().write(IMAP_LOGOUT_REQUEST.getBytes());

            //
            // get the returned string, tokenize, and
            // verify the correct output.
            //
            String response = lineRdr.readLine();
            if (response != null && response.startsWith(IMAP_BYE_RESPONSE_PREFIX)) {
                response = lineRdr.readLine();
                if (response != null && response.startsWith(IMAP_LOGOUT_RESPONSE_PREFIX)) {
                    isAServer = true;
                }
            }
        }
        return isAServer;
    }
}
