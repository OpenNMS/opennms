/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
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
 * existance of an FTP server on remote interfaces. The class implements the
 * Plugin interface that allows it to be used along with other plugins by the
 * daemon.
 * </P>
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * 
 * 
 */
public final class FtpPlugin extends AbstractTcpPlugin {
    /**
     * The default port on which the host is checked to see if it supports FTP.
     */
    private static final int DEFAULT_PORT = 21;

    /**
     * Default number of retries for FTP requests.
     */
    private final static int DEFAULT_RETRY = 0;

    /**
     * Default timeout (in milliseconds) for FTP requests.
     */
    private final static int DEFAULT_TIMEOUT = 5000;

    /**
     * The capability name of the plugin.
     */
    private static final String PROTOCOL_NAME = "FTP";

    /**
     * @param protocol
     * @param defaultPort
     * @param defaultTimeout
     * @param defaultRetries
     */
    public FtpPlugin() {
        super(PROTOCOL_NAME, DEFAULT_PORT, DEFAULT_TIMEOUT, DEFAULT_RETRY);
    }

    /**
     * @param socket
     * @param config
     * @param log
     * @param isAServer
     * @return
     * @throws IOException
     */
    protected boolean checkProtocol(Socket socket, ConnectionConfig config) throws IOException {
        BufferedReader rdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  
        FtpResponse connectResponse = FtpResponse.readResponse(rdr);
        if (!connectResponse.isCodeValid()) {
            return false;
        }
        
        FtpResponse.sendCommand(socket, "QUIT");

        FtpResponse quitResponse = FtpResponse.readResponse(rdr);
        if (!quitResponse.isCodeValid()) {
            return false;
        }
        
        return true;
    }
}
