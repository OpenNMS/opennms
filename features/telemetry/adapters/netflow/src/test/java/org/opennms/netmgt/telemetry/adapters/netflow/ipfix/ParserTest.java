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

package org.opennms.netmgt.telemetry.adapters.netflow.ipfix;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

import org.junit.Test;

import com.google.common.base.Throwables;

public class ParserTest {

    @Test
    public void canReadValidIPFIX() throws IOException, URISyntaxException {
        execute("/flows/ipfix.dat", c -> {
            try {
//                final Packet packet1 = Packet.parse(c);
//
//                assertThat(packet1.isValid(), is(true));
//
//                assertThat(packet1.header.versionNumber, is(0x000a));
//                assertThat(packet1.header.observationDomainId, is(0L));
//                assertThat(packet1.header.exportTime, is(1431516026L)); // "2015-05-13T11:20:26.000Z"
//
//                final Packet packet2 = Packet.parse(c);
//
//                assertThat(packet2.isValid(), is(true));
//
//                assertThat(packet2.header.versionNumber, is(0x000a));
//                assertThat(packet2.header.observationDomainId, is(0L));
//                assertThat(packet2.header.exportTime, is(1431516026L)); // "2015-05-13T11:20:26.000Z"
//
//                final Packet packet3 = Packet.parse(c);
//
//                assertThat(packet3.isValid(), is(true));
//
//                assertThat(packet3.header.versionNumber, is(0x000a));
//                assertThat(packet3.header.observationDomainId, is(0L));
//                assertThat(packet3.header.exportTime, is(1431516028L)); // "2015-05-13T11:20:26.000Z"

            } catch (final Exception e) {
                throw Throwables.propagate(e);
            }
        });
    }


    public void execute(final String resource, final Consumer<FileChannel> consumer) {
        Objects.requireNonNull(resource);
        Objects.requireNonNull(consumer);

        final URL resourceURL = getClass().getResource(resource);
        Objects.requireNonNull(resourceURL);

        try {
            try (final FileChannel channel = FileChannel.open(Paths.get(resourceURL.toURI()))) {
                consumer.accept(channel);
            }

        } catch (final URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
