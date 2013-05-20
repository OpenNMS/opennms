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
 * existance of an Citrix server on remote interfaces. The class implements the
 * Plugin interface that allows it to be used along with other plugins by the
 * daemon.
 * </P>
 *
 * @author <a href="mailto:jason@opennms.org">Jason</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public final class CitrixPlugin extends AbstractTcpPlugin {

    /**
     * <P>
     * The default port on which the host is checked to see if it supports
     * Citrix.
     * </P>
     */
    private static final int DEFAULT_PORT = 1494;

    /**
     * Default number of retries for Citrix requests.
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for Citrix requests.
     */
    private final static int DEFAULT_TIMEOUT = 5000; // in milliseconds

    /**
     * <P>
     * The capability name of the plugin.
     * </P>
     */
    private static final String PROTOCOL_NAME = "Citrix";

    /**
     * <P>
     * End of input stream value
     * </P>
     */
    private static final int EOF = -1;
    
    /**
     * <p>Constructor for CitrixPlugin.</p>
     */
    public CitrixPlugin() {
        super(PROTOCOL_NAME, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRY);
    }

    /** {@inheritDoc} */
    @Override
    protected boolean checkProtocol(Socket socket, ConnectionConfig config) throws IOException {
        boolean isAServer = false;
        // Allocate a line reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        StringBuffer buffer = new StringBuffer();

        while (!isAServer) {
            int val = reader.read();
            // checking if end of input stream has been reached
            if(val == EOF ) {
                break;
            }
            buffer.append((char) val);
            if (buffer.toString().indexOf("ICA") > -1) {
                isAServer = true;
            }
        }
        return isAServer;
    }
}
