/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.datagram.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ntp.NtpMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>NtpClient class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class NtpClient implements Client<NtpMessage, DatagramPacket> {

    private DatagramSocket m_socket;
    private int m_port;
    private InetAddress m_address;
    
    /**
     * <p>close</p>
     */
    public void close() {
        m_socket.close();
    }

    /** {@inheritDoc} */
    public void connect(final InetAddress address, final int port, final int timeout) throws IOException, Exception {
        LogUtils.debugf(this, "Address: %s, port: %d, timeout: %d", address, port, timeout);
        m_socket = new DatagramSocket();
        m_socket.setSoTimeout(timeout);
        setAddress(address);
        setPort(port);
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link java.net.DatagramPacket} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    public DatagramPacket receiveBanner() throws IOException, Exception {
        throw new UnsupportedOperationException("Client<NtpMessage,DatagramPacket>.receiveBanner is not yet implemented");
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.support.ntp.NtpMessage} object.
     * @return a {@link java.net.DatagramPacket} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    public DatagramPacket sendRequest(final NtpMessage request) throws IOException, Exception {
        
        final byte[] buf = new NtpMessage().toByteArray();
        m_socket.send(new DatagramPacket(buf, buf.length, getAddress(), getPort()));

        final byte[] data = new byte[512];
        final DatagramPacket packet = new DatagramPacket(data, data.length);
        m_socket.receive(packet);

        return packet;
    }

    /**
     * <p>setAddress</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     */
    protected void setAddress(final InetAddress address) {
        m_address = address;
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    protected InetAddress getAddress() {
        return m_address;
    }

    /**
     * <p>setPort</p>
     *
     * @param port a int.
     */
    protected void setPort(final int port) {
        m_port = port;
    }

    /**
     * <p>getPort</p>
     *
     * @return a int.
     */
    protected int getPort() {
        return m_port;
    }
}
