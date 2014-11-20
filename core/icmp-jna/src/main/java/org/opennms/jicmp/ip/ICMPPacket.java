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

package org.opennms.jicmp.ip;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.opennms.jicmp.jna.NativeDatagramPacket;

/**
 * ICMPPacket
 *
 * @author brozow
 */
public class ICMPPacket {
    
    public static final int CHECKSUM_INDEX = 2;
    
    public enum Type {
        EchoReply(0),
        DestUnreachable(3),
        SourceQuench(4),
        Redirect(5),
        EchoRequest(8),
        TimeExceeded(11),
        Traceroute(30),
        
        // this is used to represent a type code that we have not handled
        Other(-1);

        
        private int m_code;
        private Type(int code) {
            m_code = code;
        }
        
        public int getCode() {
            return m_code;
        }
        
        public static Type toType(int code) {
            for(Type p : Type.values()) {
                if (code == p.getCode()) {
                    return p;
                }
            }
            return Other;
        }
        
    }

    ByteBuffer m_packetData;
    
    public ICMPPacket(ByteBuffer ipPayload) {
        m_packetData = ipPayload;
    }
    
    public ICMPPacket(ICMPPacket icmpPacket) {
        this(icmpPacket.m_packetData.duplicate());
    }
    
    public ICMPPacket(int size) {
        this(ByteBuffer.allocate(size));
        //this(ByteBuffer.allocateDirect(size));
    }
    
    public Type getType() {
        return Type.toType(m_packetData.get(0));
    }
    
    public void setType(Type t) {
        m_packetData.put(0, ((byte)(t.getCode())));
    }
    
    public int getCode() {
        return 0xff & m_packetData.get(1);
    }

    public void setCode(int code) {
        m_packetData.put(1, ((byte)code));
    }
    
    public int getChecksum() {
        return getUnsignedShort(2);
    }
    
    public void setChecksum() {
        setUnsignedShort(2, computeChecksum());
    }
    
    public int computeChecksum() {
        
        int sum = 0;
        int count = m_packetData.remaining();
        int index = 0;
        while(count > 1) {
            if (index != CHECKSUM_INDEX) {
                sum += getUnsignedShort(index);
            }
            index += 2;
            count -= 2;
        }

        if (count > 0) {
            
            sum += makeUnsignedShort(m_packetData.get((m_packetData.remaining()-1)), (byte)0);
        }
        
        int sumLo = sum & 0xffff;
        int sumHi = (sum >> 16) & 0xffff;
        
        sum = sumLo + sumHi;
        
        sumLo = sum & 0xffff;
        sumHi = (sum >> 16) & 0xffff;

        sum = sumLo + sumHi;
        
        return (~sum) & 0xffff;
        
    }
    
    /**
     * @param index The byte offset into the packet where the bytes will
     * be inserted
     */
    public void setBytes(int index, byte[] b) {
        ByteBuffer payload = m_packetData;
        int oldPos = payload.position();
        try {
            payload.position(index);
            payload.put(b);
        } finally {
            payload.position(oldPos);
        }
    }

    public int makeUnsignedShort(byte b1, byte b0) {
        return 0xffff & (((b1 & 0xff) << 8) | 
                         ((b0 & 0xff) << 0));
    }

    /**
     * @param index The byte offset into the packet where the value
     * can be found
     */
    public int getUnsignedShort(int index) {
        return m_packetData.getShort(index) & 0xffff;
    }

    /**
     * @param index The byte offset into the packet where the value
     * can be found
     * @param us Unsigned short value to insert into the buffer
     */
    public void setUnsignedShort(int index, int us) {
        m_packetData.putShort(index, ((short)(us & 0xffff)));
    }

    public NativeDatagramPacket toDatagramPacket(InetAddress destinationAddress) {
        // compute and set the ICMP Header checksum
        setChecksum();
        return new NativeDatagramPacket(m_packetData.duplicate(), destinationAddress, 0);
    }

}
