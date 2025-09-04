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

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.TcpSession;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow9MessageBuilder;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class NMS13006_Test {
    private final static Path FOLDER = Paths.get("src/test/resources/flows");
    private InformationElementDatabase database = new InformationElementDatabase(new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementProvider(), new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.InformationElementProvider());

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("karaf.etc", "src/test/resources");
    }

    @Test
    public void firstAndLastSwitchedTest() throws Exception {
        final RecordEnrichment enrichment = (address -> Optional.empty());
        final List<Value<?>> record = new ArrayList<>();
        record.add(new UnsignedValue("@unixSecs", 1000));
        record.add(new UnsignedValue("@sysUpTime", 1000));
        record.add(new UnsignedValue("FIRST_SWITCHED", 2000));
        record.add(new UnsignedValue("LAST_SWITCHED", 3000));
        final Netflow9MessageBuilder builder = new Netflow9MessageBuilder();
        final FlowMessage flowMessage = builder.buildMessage(record, enrichment).build();

        Assert.assertEquals(1001000L, flowMessage.getFirstSwitched().getValue());
        Assert.assertEquals(1002000L, flowMessage.getLastSwitched().getValue());
        Assert.assertEquals(1001000L, flowMessage.getDeltaSwitched().getValue());
    }

    @Test
    public void flowStartAndEndMsTest() throws Exception {
        final RecordEnrichment enrichment = (address -> Optional.empty());
        final List<Value<?>> record = new ArrayList<>();
        record.add(new UnsignedValue("@unixSecs", 1000));
        record.add(new UnsignedValue("@sysUpTime", 1000));
        record.add(new UnsignedValue("flowStartMilliseconds", 2001000));
        record.add(new UnsignedValue("flowEndMilliseconds", 2002000));
        final Netflow9MessageBuilder builder = new Netflow9MessageBuilder();
        final FlowMessage flowMessage = builder.buildMessage(record, enrichment).build();

        Assert.assertEquals(2001000L, flowMessage.getFirstSwitched().getValue());
        Assert.assertEquals(2002000L, flowMessage.getLastSwitched().getValue());
        Assert.assertEquals(2001000L, flowMessage.getDeltaSwitched().getValue());
    }

    @Test
    public void captureFileTest() throws Exception {
        testFile("nms-13006.dat");
    }

    public void testFile(final String filename) throws Exception {
        final Session session = new TcpSession(InetAddress.getLoopbackAddress(), () -> new SequenceNumberTracker(32));

        try (final FileChannel channel = FileChannel.open(FOLDER.resolve(filename))) {
            final ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();

            final ByteBuf buf = Unpooled.wrappedBuffer(buffer);

            do {
                final Header header = new Header(slice(buf, Header.SIZE));
                final Packet packet = new Packet(database, session, header, buf);

                final RecordEnrichment enrichment = (address -> Optional.empty());

                packet.getRecords().forEach(r -> {
                            final Netflow9MessageBuilder builder = new Netflow9MessageBuilder();
                            final FlowMessage flowMessage = builder.buildMessage(r, enrichment).build();

                            Assert.assertEquals(true, flowMessage.hasFirstSwitched());
                            Assert.assertEquals(true, flowMessage.hasLastSwitched());
                            Assert.assertEquals(true, flowMessage.hasDeltaSwitched());
                        }
                );

                assertThat(packet.header.versionNumber, is(0x0009));

            } while (buf.isReadable());
        }
    }
}
