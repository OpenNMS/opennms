/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 */

package org.opennms.netmgt.provision.detector.datagram.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.support.Client;


/**
 * <p>DatagramClient class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
public class DatagramClient implements Client<DatagramPacket, DatagramPacket> {
    
    private DatagramSocket m_socket;
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.Client#close()
     */
    /**
     * <p>close</p>
     */
    public void close() {
        m_socket.close();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.provision.detector.Client#connect(java.net.InetAddress, int, int)
     */
    /** {@inheritDoc} */
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException {
        LogUtils.debugf(this, "Address: %s, port: %d, timeout: %d", address, port, timeout);

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
    public DatagramPacket sendRequest(final DatagramPacket request) throws IOException {

        m_socket.send(request);

        final byte[] data = new byte[512];
        DatagramPacket response = new DatagramPacket(data, data.length);
        m_socket.receive(response);

        return response;
    }
}
