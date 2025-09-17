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
package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.TcpSession;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

public class PayloadTest {

    @Test
    public void outputPayloadTest() throws IOException, URISyntaxException {
        execute("/flows/nf9_broken.dat", buffer -> {
            try {
                final Session session = new TcpSession(InetAddress.getLoopbackAddress(), () -> new SequenceNumberTracker(32));
                final org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Header h1 =
                        new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Header(slice(buffer, org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Header.SIZE));
                final org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Packet p1 =
                        new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Packet(null, session, h1, buffer);
            } catch (final Exception e) {
                assertThat(e instanceof InvalidPacketException, is(true));
                assertThat(e.getMessage(), containsString("Invalid template ID: 8, Offset: [0x001E], Payload:"));
                assertThat(e.getMessage(), containsString("|00000000| 00 09 00 01 23 bc 9f 78 5f 1e 2e 03 05 cc 4e f2 |....#..x_.....N.|"));
                assertThat(e.getMessage(), containsString("|00000070| 00 12 00 04 00 3d 00 01                         |.....=..        |"));
                return;
            }
            fail();
        });
    }

    public void execute(final String resource, final Consumer<ByteBuf> consumer) {
        Objects.requireNonNull(resource);
        Objects.requireNonNull(consumer);

        final URL resourceURL = getClass().getResource(resource);
        Objects.requireNonNull(resourceURL);

        try {
            try (final FileChannel channel = FileChannel.open(Paths.get(resourceURL.toURI()))) {
                final ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
                channel.read(buffer);
                buffer.flip();

                consumer.accept(Unpooled.wrappedBuffer(buffer));
            }
        } catch (final URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
