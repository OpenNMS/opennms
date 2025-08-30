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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.TcpSession;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;

import com.google.common.base.Throwables;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ParserTest {

    private InformationElementDatabase database = new InformationElementDatabase(new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementProvider(), new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.InformationElementProvider());

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("karaf.etc", "src/test/resources");
    }

    @Test
    public void canReadValidIPFIX() throws IOException, URISyntaxException {
        execute("/flows/ipfix.dat", buffer -> {
            try {

                final Session session = new TcpSession(InetAddress.getLoopbackAddress(), () -> new SequenceNumberTracker(32));

                final Header h1 = new Header(slice(buffer, Header.SIZE));
                final Packet p1 = new Packet(database, session, h1, slice(buffer, h1.length - Header.SIZE));

                assertThat(p1.header.versionNumber, is(0x000a));
                assertThat(p1.header.observationDomainId, is(0L));
                assertThat(p1.header.exportTime, is(1431516026L)); // "2015-05-13T11:20:26.000Z"

                final Header h2 = new Header(slice(buffer, Header.SIZE));
                final Packet p2 = new Packet(database, session, h2, slice(buffer, h2.length - Header.SIZE));

                assertThat(p2.header.versionNumber, is(0x000a));
                assertThat(p2.header.observationDomainId, is(0L));
                assertThat(p2.header.exportTime, is(1431516026L)); // "2015-05-13T11:20:26.000Z"

                final Header h3 = new Header(slice(buffer, Header.SIZE));
                final Packet p3 = new Packet(database, session, h3, slice(buffer, h3.length - Header.SIZE));

                assertThat(p3.header.versionNumber, is(0x000a));
                assertThat(p3.header.observationDomainId, is(0L));
                assertThat(p3.header.exportTime, is(1431516028L)); // "2015-05-13T11:20:26.000Z"

                assertThat(buffer.isReadable(), is(false));

            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
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
