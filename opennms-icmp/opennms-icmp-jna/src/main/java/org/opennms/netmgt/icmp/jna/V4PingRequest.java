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
package org.opennms.netmgt.icmp.jna;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.opennms.jicmp.ip.ICMPEchoPacket;
import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;

class V4PingRequest extends ICMPEchoPacket {
    
    // The below long is equivalent to the next line and is more efficient than
    // manipulation as a string
    // StandardCharsets.US_ASCII.encode("OpenNMS!").getLong(0);
    public static final int PACKET_LENGTH = 64;
    public static final long COOKIE = 0x4F70656E4E4D5321L;
    public static final int OFFSET_COOKIE = 0;
    public static final int OFFSET_TIMESTAMP = 8;
    public static final int OFFSET_THREAD_ID = 16;
    public static final int DATA_LENGTH = 8*3;

    public V4PingRequest() {
        super(PACKET_LENGTH);
        setType(Type.EchoRequest);
        setCode(0);
    }

    public V4PingRequest(int id, int seqNum, long threadId, int packetSize) {
        super(packetSize);
        
        // header fields
        setType(Type.EchoRequest);
        setCode(0);
        setIdentifier(id);
        setSequenceNumber(seqNum);
        
        // data fields
        setThreadId(threadId);
        setCookie();
        // timestamp is set later

        // fill buffer with 'interesting' data
        ByteBuffer buf = getContentBuffer();
        for(int b = DATA_LENGTH; b < buf.limit(); b++) {
            buf.put(b, (byte)b);
        }
    }

    public V4PingRequest(int id, int seqNum, long threadId) {
        super(PACKET_LENGTH);
        
        // header fields
        setType(Type.EchoRequest);
        setCode(0);
        setIdentifier(id);
        setSequenceNumber(seqNum);
        
        // data fields
        setThreadId(threadId);
        setCookie();
        // timestamp is set later

        // fill buffer with 'interesting' data
        ByteBuffer buf = getContentBuffer();
        for(int b = DATA_LENGTH; b < buf.limit(); b++) {
            buf.put(b, (byte)b);
        }
    }
    
    public long getThreadId() {
        return getContentBuffer().getLong(OFFSET_THREAD_ID);
    }
    
    public void setThreadId(long threadId) {
        getContentBuffer().putLong(OFFSET_THREAD_ID, threadId);
    }
    
    public void setCookie() {
        getContentBuffer().putLong(OFFSET_COOKIE, COOKIE);
    }

    @Override
    public NativeDatagramPacket toDatagramPacket(InetAddress destinationAddress) {
        getContentBuffer().putLong(OFFSET_TIMESTAMP, System.nanoTime());
        return super.toDatagramPacket(destinationAddress);
    }

    public void send(NativeDatagramSocket socket, InetAddress addr) {
        socket.send(toDatagramPacket(addr));
    }
}
