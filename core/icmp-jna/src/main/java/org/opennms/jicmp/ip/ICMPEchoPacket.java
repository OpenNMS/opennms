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

import java.nio.ByteBuffer;


/**
 * ICMPEchoReply
 *
 * @author brozow
 */
public class ICMPEchoPacket extends ICMPPacket {
    
    public ICMPEchoPacket(int size) {
        super(size);
    }

    public ICMPEchoPacket(ICMPPacket icmpPacket) {
        super(icmpPacket);
    }
    
    public ByteBuffer getContentBuffer() {
        ByteBuffer content = m_packetData.duplicate();
        content.position(8);
        return content.slice();
    }
    
    public int getPacketLength() {
        return m_packetData.limit();
    }

    public int getIdentifier() {
        return getUnsignedShort(4);
    }
    
    public void setIdentifier(int id) {
        setUnsignedShort(4, id);
    }
    
    public int getSequenceNumber() {
        return getUnsignedShort(6);
    }
    
    public void setSequenceNumber(int sn) {
        setUnsignedShort(6, sn);
    }

}
