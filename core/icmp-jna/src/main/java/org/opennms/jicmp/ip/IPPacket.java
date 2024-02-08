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
