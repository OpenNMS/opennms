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
package org.opennms.netmgt.syslogd.api;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.opennms.core.ipc.sink.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogConnection implements Message {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogConnection.class);

    /**
     * This size is used as the size of each {@link ByteBuffer} used to capture syslog
     * messages.
     */
    public static final int MAX_PACKET_SIZE = 4096;

    private final InetSocketAddress source;
    private final ByteBuffer buffer;

    public SyslogConnection(DatagramPacket pkt, boolean copy) {
        this.source = new InetSocketAddress(pkt.getAddress(), pkt.getPort());
        final byte[] data = copy ? Arrays.copyOf(pkt.getData(), pkt.getLength()) : pkt.getData();
        this.buffer = ByteBuffer.wrap(data, 0, pkt.getLength());
    }

    public SyslogConnection(InetSocketAddress source, ByteBuffer buffer) {
        this.source = source;
        this.buffer = buffer;
    }

    public InetSocketAddress getSource() {
        return source;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public static DatagramPacket copyPacket(final DatagramPacket packet) {
        byte[] message = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), 0, message, 0, packet.getLength());
        InetAddress addr = null;
        try {
            addr = InetAddress.getByAddress(packet.getAddress().getHostName(), packet.getAddress().getAddress());
            DatagramPacket retPacket = new DatagramPacket(
                message,
                packet.getOffset(),
                packet.getLength(),
                addr,
                packet.getPort()
            );
            return retPacket;
        } catch (UnknownHostException e) {
            LOG.warn("unable to clone InetAddress object for {}", packet.getAddress());
        }
        return null;
    }

    public static DatagramPacket copyPacket(final InetAddress sourceAddress, final int sourcePort, final ByteBuffer buffer) {
        byte[] message = new byte[MAX_PACKET_SIZE];
        int i = 0;
        // Copy the buffer into the byte array
        while (buffer.hasRemaining()) {
            message[i++] = buffer.get();
        }
        return copyPacket(sourceAddress, sourcePort, message, i);
    }

    private static DatagramPacket copyPacket(final InetAddress sourceAddress, final int sourcePort, final byte[] buffer, final int length) {
        DatagramPacket retPacket = new DatagramPacket(
            buffer,
            0,
            length,
            sourceAddress,
            sourcePort
        );
        return retPacket;
    }
}
