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

package org.opennms.netmgt.telemetry.protocols.sflow.parser;

import org.bson.json.JsonWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.Record;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampleDatagram;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampleRecord;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class BlackboxTest {
    private final static Path FOLDER = Paths.get("src/test/resources/flows");
    private final static Record.DataFormat DATA_FORMAT0_1 = Record.DataFormat.from(0, 1);
    private final static Record.DataFormat DATA_FORMAT0_3 = Record.DataFormat.from(0, 3);

    @Parameterized.Parameters(name = "file: {0}")
    public static Iterable<String> data() throws IOException {
        return Arrays.<String>asList("sflow1.dat", "sflow2.dat", "sflow3.dat", "sflow4.dat");
    }

    private final String file;

    public BlackboxTest(final String file) {
        this.file = file;
    }

    private void dumpPacket(SampleDatagram packet) {
        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(stringWriter);
        int total = 0;
        packet.writeBson(jsonWriter);
        System.out.println(stringWriter.toString());

        for (SampleRecord sampleRecord : packet.version.datagram.samples.values) {
            if (DATA_FORMAT0_1.equals(sampleRecord.dataFormat) || DATA_FORMAT0_3.equals(sampleRecord.dataFormat)) {
                total++;
            }
        }

        System.out.printf("%d total flow(s)\n", total);
    }

    @Test
    public void testFiles() throws Exception {
        try (final FileChannel channel = FileChannel.open(FOLDER.resolve(this.file))) {
            final ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();

            do {
                final SampleDatagram packet = new SampleDatagram(buffer);

                dumpPacket(packet);
                assertThat(packet.version.version.value, is(0x0005));
            } while (buffer.hasRemaining());
        }
    }
}
