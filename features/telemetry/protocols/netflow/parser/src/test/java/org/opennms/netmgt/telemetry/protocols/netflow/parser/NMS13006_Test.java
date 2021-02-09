/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
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
import org.junit.Test;
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
                final Packet packet = new Packet(session, header, buf);

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
