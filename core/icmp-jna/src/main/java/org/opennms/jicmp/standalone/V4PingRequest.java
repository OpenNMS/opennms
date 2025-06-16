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
package org.opennms.jicmp.standalone;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.opennms.jicmp.ip.ICMPEchoPacket;
import org.opennms.jicmp.jna.NativeDatagramPacket;
import org.opennms.jicmp.jna.NativeDatagramSocket;

class V4PingRequest extends ICMPEchoPacket {
    
    public V4PingRequest() {
        super(64);
        setType(Type.EchoRequest);
        setCode(0);
    }
    
    public V4PingRequest(int id, int seqNum) {
        super(64);
        setType(Type.EchoRequest);
        setCode(0);
        setIdentifier(id);
        setSequenceNumber(seqNum);
        ByteBuffer buf = getContentBuffer();
        for(int b = 0; b < 56; b++) {
            buf.put((byte)b);
        }
    }

    @Override
    public NativeDatagramPacket toDatagramPacket(InetAddress destinationAddress) {
        ByteBuffer contentBuffer = getContentBuffer();
        contentBuffer.putLong(V4PingReply.COOKIE);
        contentBuffer.putLong(System.nanoTime());
        return super.toDatagramPacket(destinationAddress);
    }

    public void send(NativeDatagramSocket socket, InetAddress addr) {
        socket.send(toDatagramPacket(addr));
    }
}
