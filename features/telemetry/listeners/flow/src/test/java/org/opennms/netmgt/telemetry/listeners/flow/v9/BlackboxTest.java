/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.listeners.flow.v9;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.netmgt.telemetry.listeners.flow.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.flow.session.TcpSession;
import org.opennms.netmgt.telemetry.listeners.flow.session.TemplateManager;
import org.opennms.netmgt.telemetry.listeners.flow.v9.proto.Header;
import org.opennms.netmgt.telemetry.listeners.flow.v9.proto.Packet;

@RunWith(Parameterized.class)
public class BlackboxTest {
    private final static File folder = new File("src/test/resources/flows");
    private final static String PROTOCOL = "netflow9";
    private final File file;

    @Parameterized.Parameters(name = "file: {0}")
    public static Iterable<Object[]> data() throws IOException {
        return Arrays.stream(folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(PROTOCOL) && name.endsWith(".dat");
            }
        })).map(f -> new Object[]{f.getAbsoluteFile()}).collect(Collectors.toList());
    }

    public BlackboxTest(final File file) {
        this.file = file;
    }

    @Test
    public void testFiles() throws Exception {
        try (final FileChannel channel = FileChannel.open(file.toPath())) {
            final ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();
            final TemplateManager templateManager = new TcpSession();

            do {
                final Header header = new Header(BufferUtils.slice(buffer, Header.SIZE));
                final Packet packet = new Packet(templateManager, header, buffer);
                assertThat(packet.header.versionNumber, is(0x0009));
            } while (buffer.hasRemaining());
        }
    }
}
