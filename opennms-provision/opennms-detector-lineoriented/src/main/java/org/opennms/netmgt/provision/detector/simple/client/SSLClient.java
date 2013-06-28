/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.simple.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.opennms.core.utils.SocketUtils;
import org.opennms.netmgt.provision.detector.simple.request.LineOrientedRequest;
import org.opennms.netmgt.provision.detector.simple.response.MultilineOrientedResponse;
import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>SSLClient class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class SSLClient extends MultilineOrientedClient implements Client<LineOrientedRequest, MultilineOrientedResponse> {
    
    private static final Logger LOG = LoggerFactory.getLogger(SSLClient.class);
    

    /** {@inheritDoc} */
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException {
        m_socket = getWrappedSocket(address, port, timeout);
        setOutput(m_socket.getOutputStream());
        setInput(new BufferedReader(new InputStreamReader(m_socket.getInputStream())));
    }
    
    /**
     * <p>getWrappedSocket</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param port a int.
     * @param timeout a int.
     * @return a {@link java.net.Socket} object.
     * @throws java.io.IOException if any.
     */
    protected Socket getWrappedSocket(final InetAddress address, final int port, final int timeout) throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(address, port), timeout);
        socket.setSoTimeout(timeout);
        try {
            return SocketUtils.wrapSocketInSslContext(socket);
        } catch (final Exception e) {
            LOG.debug("Unable to wrap socket in SSL.", e);
            return null;
        }
    }

}
