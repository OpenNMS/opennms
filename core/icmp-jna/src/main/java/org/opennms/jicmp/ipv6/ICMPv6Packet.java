/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.jicmp.ipv6;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.opennms.jicmp.jna.NativeDatagramPacket;

/**
 * ICMPPacket
 *
 * @author brozow
 */
public class ICMPv6Packet {
    
    public static final int CHECKSUM_INDEX = 2;
    
    public enum Type {
        DestinationUnreachable(1),
        TimeExceeded(3),
        EchoRequest(128),
        EchoReply(129),

        // this is used to represent a type code that we have not handled
        Other(-1);
        
        
        private int m_code;
        private Type(int code) {
            m_code = code;
        }
        
        public int getCode() {
            return m_code;
        }
        
        public static Type toType(byte typeCode) {
            int code = (typeCode & 0xff);
            for(Type p : Type.values()) {
                if (code == p.getCode()) {
                    return p;
                }
            }
            return Other;
        }
        
    }

    ByteBuffer m_packetData;
    
    public ICMPv6Packet(ByteBuffer ipPayload) {
        m_packetData = ipPayload;
    }
    
    public ICMPv6Packet(ICMPv6Packet icmpPacket) {
        this(icmpPacket.m_packetData.duplicate());
    }
    
    public ICMPv6Packet(int size) {
        this(ByteBuffer.allocate(size));
        //this(ByteBuffer.allocateDirect(size));
    }
    
    public int getPacketLength() {
        return m_packetData.limit();
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

    public int getUnsignedShort(int index) {
        return m_packetData.getShort(index) & 0xffff;
    }

    public void setUnsignedShort(int index, int us) {
        m_packetData.putShort(index, ((short)(us & 0xffff)));
    }

    public NativeDatagramPacket toDatagramPacket(InetAddress destinationAddress) {
        return new NativeDatagramPacket(m_packetData.duplicate(), destinationAddress, 0);
    }

}
