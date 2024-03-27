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
