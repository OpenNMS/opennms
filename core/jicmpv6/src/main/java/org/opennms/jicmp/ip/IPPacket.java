/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.jicmp.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import org.opennms.jicmp.jna.NativeDatagramPacket;

/**
 * IPPacket
 *
 * @author brozow
 */
public class IPPacket {
    
    public enum Protocol {
        ICMP(1),
        TCP(6),
        UDP(17),
        V6_OVER_V4(41);
        
        private int m_code;
        private Protocol(int code) {
            m_code = code;
        }
        
        public int getCode() {
            return m_code;
        }
        
        public static Protocol toProtocol(int code) {
            for(Protocol p : Protocol.values()) {
                if (code == p.getCode()) {
                    return p;
                }
            }
            throw new IllegalArgumentException(String.format("Unabled to find Protocol with code %d", code));
        }
        
    }
    
    private ByteBuffer m_buffer;
    
    public IPPacket(IPPacket p) {
        this(p.m_buffer.duplicate());
    }
    
    public IPPacket(ByteBuffer buffer) {
        m_buffer = buffer;
    }
    
    public IPPacket(byte[] data, int offset, int length) {
        this(ByteBuffer.wrap(data, offset, length).slice());
    }

    public IPPacket(NativeDatagramPacket datagram) {
        this(datagram.getContent());
    }
    
    /**
     * Returns the version of the IP Packet which must be '4'
     */
    public int getVersion() {
        return ((m_buffer.get(0) & 0xf0) >> 4);
    }
    
    /**
     * Returns the length of the header in bytes
     */
    public int getHeaderLength() {
        // Specifies the length of the IP packet header in 32 bit words. The minimum value for a valid header is 5.
        // This is stored in bits 4-7 of header (low bits of first byte)
        return (m_buffer.get(0) & 0xf) << 2; // shift effectively does * 4 (4 bytes per 32 bit word)
    }
    
    private InetAddress getAddrAtOffset(int offset) {
        byte[] addr = new byte[4];
        int oldPos = m_buffer.position();
        try {
            m_buffer.position(offset);
            m_buffer.get(addr);
        } finally {
            m_buffer.position(oldPos);
        }
        
        InetAddress result = null;
        try {
            result = InetAddress.getByAddress(addr);
        } catch (UnknownHostException e) {
            // this can't happen
        }
        
        return result;

    }
    
    public int getTimeToLive() {
        return 0xff & m_buffer.get(8);
    }
    
    public Protocol getProtocol() {
        return Protocol.toProtocol(m_buffer.get(9));
    }
    
    public InetAddress getSourceAddress() {
        return getAddrAtOffset(12);
    }
    
    public InetAddress getDestinationAddress() {
        return getAddrAtOffset(16);
    }
    
    public ByteBuffer getPayload() {
        ByteBuffer data = m_buffer.duplicate();
        data.position(getHeaderLength());
        return data.slice();
    }
    public int getPayloadLength() {
        return getPayload().remaining();
    }
    
}
