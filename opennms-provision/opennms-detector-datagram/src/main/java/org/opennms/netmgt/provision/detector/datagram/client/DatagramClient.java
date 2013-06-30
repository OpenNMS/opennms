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

package org.opennms.netmgt.provision.detector.datagram.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.opennms.netmgt.provision.support.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>DatagramClient class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class DatagramClient implements Client<DatagramPacket, DatagramPacket> {

    private static final Logger LOG = LoggerFactory.getLogger(DatagramClient.class);    
    private DatagramSocket m_socket;
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.Client#close()
     */
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        m_socket.close();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.Client#connect(java.net.InetAddress, int, int)
     */
    /** {@inheritDoc} */
    @Override
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException {
        LOG.debug("Address: {}, port: {}, timeout: {}", address, port, timeout);

        m_socket = new DatagramSocket();
        m_socket.setSoTimeout(timeout);
        m_socket.connect(address, port);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.Client#receiveBanner()
     */
    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link java.net.DatagramPacket} object.
     * @throws java.io.IOException if any.
     */
    @Override
    public DatagramPacket receiveBanner() throws IOException {
        throw new UnsupportedOperationException("Client<DatagramPacket,DatagramPacket>.receiveBanner is not yet implemented");
        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.Client#sendRequest(java.lang.Object)
     */
    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link java.net.DatagramPacket} object.
     * @return a {@link java.net.DatagramPacket} object.
     * @throws java.io.IOException if any.
     */
    @Override
    public DatagramPacket sendRequest(final DatagramPacket request) throws IOException {

        m_socket.send(request);

        final byte[] data = new byte[512];
        DatagramPacket response = new DatagramPacket(data, data.length);
        m_socket.receive(response);

        return response;
    }
}
