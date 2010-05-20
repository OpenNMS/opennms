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

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.ntp.NtpMessage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class NtpClient implements Client<NtpMessage, DatagramPacket> {

    private DatagramSocket m_socket;
    private int m_port;
    private InetAddress m_address;
    
    public void close() {
        m_socket.close();
    }

    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        if (log().isDebugEnabled()) {
            log().debug("Address: " + address + ", port: " + port + ", timeout: " + timeout);
        }
        m_socket = new DatagramSocket();
        m_socket.setSoTimeout(timeout);
        setAddress(address);
        setPort(port);
    }

    public DatagramPacket receiveBanner() throws IOException, Exception {
        throw new UnsupportedOperationException("Client<NtpMessage,DatagramPacket>.receiveBanner is not yet implemented");
    }

    public DatagramPacket sendRequest(NtpMessage request) throws IOException, Exception {
        
        byte[] buf = new NtpMessage().toByteArray();
        DatagramPacket outpkt = new DatagramPacket(buf, buf.length, getAddress(), getPort());
        m_socket.send(outpkt);

        byte[] data = new byte[512];
        DatagramPacket response = new DatagramPacket(data, data.length);

        m_socket.receive(response);

        return response;
    }

    protected void setAddress(InetAddress address) {
        m_address = address;
    }

    protected InetAddress getAddress() {
        return m_address;
    }

    protected void setPort(int port) {
        m_port = port;
    }

    protected int getPort() {
        return m_port;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(NtpClient.class);
    }

}
