/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
                        new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Packet(session, h1, buffer);
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
