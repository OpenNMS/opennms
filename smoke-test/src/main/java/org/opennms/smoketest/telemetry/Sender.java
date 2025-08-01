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
package org.opennms.smoketest.telemetry;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

public interface Sender {
    void send(final byte[] payload) throws IOException;

    static Sender udp(final InetSocketAddress destinationAddress) {
        Objects.requireNonNull(destinationAddress);

        return payload -> {
            Objects.requireNonNull(payload);

            try (DatagramSocket serverSocket = new DatagramSocket()) {
                final DatagramPacket sendPacket = new DatagramPacket(payload, payload.length, destinationAddress);
                serverSocket.send(sendPacket);
            }
        };
    }

    static Sender tcp(final InetSocketAddress destinationAddress) throws IOException {
        Objects.requireNonNull(destinationAddress);

        final Socket socket = new Socket();
        socket.connect(destinationAddress);

        return stream(socket.getOutputStream());
    }

    static Sender stream(final OutputStream stream) {
        return stream::write;
    }
}
