/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.jicmp.jna;

import java.net.InetAddress;
import java.nio.ByteBuffer;

/**
 * NativeDatagramPacketBase
 *
 * @author brozow
 */
public class NativeDatagramPacket {

    private ByteBuffer m_data;
    private InetAddress m_address;
    private int m_port;
    
    public NativeDatagramPacket(ByteBuffer data, InetAddress address, int port) {
        m_data = data;
        m_address = address;
        m_port = port;
    }
    
    public NativeDatagramPacket(int size) {
        this(ByteBuffer.allocate(size), null, -1);
    }
    
    public NativeDatagramPacket(byte[] data, InetAddress host, int port) {
        this(ByteBuffer.wrap(data), host, port);
    }

    public InetAddress getAddress() {
        return m_address;
    }

    public void setAddress(InetAddress addr) {
        m_address = addr;
    }

    public int getPort() {
        return m_port;
    }

    public void setPort(int port) {
        m_port = port;
    }

    public int getLength() {
        return m_data.limit();
    }

    public void setLength(int length) {
        m_data.limit(length);
    }

    public ByteBuffer getContent() {
        return m_data.duplicate();
    }

    @Override
    public String toString() {
    
        final StringBuilder buf = new StringBuilder();
        
        buf.append("Address: ");
        buf.append(m_address);
        buf.append(" Port: ");
        buf.append(m_port);
        buf.append("\nData: ");
        
        ByteBuffer data = m_data.duplicate();
        
        buf.append(data.limit());
        buf.append(" Bytes\n");
        
        final int bytesPerRow = 4;
        final int limit = data.limit();
        final int rows = (limit + bytesPerRow) / bytesPerRow; 
        int index = 0;
        for(int i = 0; i < rows && index < limit; i++) {
            for(int j = 0; j < bytesPerRow && index < limit; j++) {
                buf.append(String.format("%02X", data.get(index++)));
            }
            buf.append("\n");
        }
        
        buf.append("\n");
            
        return buf.toString();
    }

}
