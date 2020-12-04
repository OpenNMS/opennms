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

package org.opennms.netmgt.telemetry.protocols.bmp.parser;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerAccessor;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerHeader;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ParserTest {
    private final static Path FILE_NMS_12643 = Paths.get("src/test/resources/NMS-12643.raw");
    private final static Path FILE_NMS_12649 = Paths.get("src/test/resources/NMS-12649.raw");
    private final static Path FILE_NMS_12671 = Paths.get("src/test/resources/NMS-12671.raw");

    private void checkFile(final Path file) throws Exception {
        try (final FileChannel channel = FileChannel.open(file)) {
            final ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();
            final ByteBuf buf = Unpooled.wrappedBuffer(buffer);
            while(buf.readableBytes() > 0) {
                final Header header = new Header(slice(buf, Header.SIZE));
                final Packet packet = header.parsePayload(slice(buf, header.length - Header.SIZE), new PeerAccessor() {
                    @Override
                    public Optional<PeerInfo> getPeerInfo(final PeerHeader peerHeader) {
                        return Optional.empty();
                    }
                });
            }
        }
    }

    @Test
    public void testNMS12643() throws Exception {
        checkFile(FILE_NMS_12643);
    }

    @Test
    public void testNMS12649() throws Exception {
        checkFile(FILE_NMS_12649);
    }

    @Test
    public void testNMS12671() throws Exception {
        checkFile(FILE_NMS_12671);
    }
}
