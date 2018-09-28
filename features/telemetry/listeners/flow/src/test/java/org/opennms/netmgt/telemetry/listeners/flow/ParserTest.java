/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.flow;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils.slice;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.Test;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.Header;
import org.opennms.netmgt.telemetry.listeners.flow.ipfix.proto.Packet;
import org.opennms.netmgt.telemetry.listeners.flow.session.TcpSession;
import org.opennms.netmgt.telemetry.listeners.flow.session.Session;

import com.google.common.base.Throwables;

public class ParserTest {

    @Test
    public void canReadValidIPFIX() throws IOException, URISyntaxException {
        execute("/flows/ipfix.dat", buffer -> {
            try {
                final InetSocketAddress sender = InetSocketAddress.createUnresolved("localhost", 4711);

                final Session session = new TcpSession();

                final Header h1 = new Header(slice(buffer, Header.SIZE));
                final Packet p1 = new Packet(session, sender, h1, slice(buffer, h1.length - Header.SIZE));

                assertThat(p1.header.versionNumber, is(0x000a));
                assertThat(p1.header.observationDomainId, is(0L));
                assertThat(p1.header.exportTime, is(1431516026L)); // "2015-05-13T11:20:26.000Z"

                final Header h2 = new Header(slice(buffer, Header.SIZE));
                final Packet p2 = new Packet(session, sender, h2, slice(buffer, h2.length - Header.SIZE));

                assertThat(p2.header.versionNumber, is(0x000a));
                assertThat(p2.header.observationDomainId, is(0L));
                assertThat(p2.header.exportTime, is(1431516026L)); // "2015-05-13T11:20:26.000Z"

                final Header h3 = new Header(slice(buffer, Header.SIZE));
                final Packet p3 = new Packet(session, sender, h3, slice(buffer, h3.length - Header.SIZE));

                assertThat(p3.header.versionNumber, is(0x000a));
                assertThat(p3.header.observationDomainId, is(0L));
                assertThat(p3.header.exportTime, is(1431516028L)); // "2015-05-13T11:20:26.000Z"

                assertThat(buffer.hasRemaining(), is(false));

            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
        });
    }

    public void execute(final String resource, final Consumer<ByteBuffer> consumer) {
        Objects.requireNonNull(resource);
        Objects.requireNonNull(consumer);

        final URL resourceURL = getClass().getResource(resource);
        Objects.requireNonNull(resourceURL);

        try {
            try (final FileChannel channel = FileChannel.open(Paths.get(resourceURL.toURI()))) {
                final ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
                channel.read(buffer);
                buffer.flip();

                consumer.accept(buffer);
            }

        } catch (final URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
