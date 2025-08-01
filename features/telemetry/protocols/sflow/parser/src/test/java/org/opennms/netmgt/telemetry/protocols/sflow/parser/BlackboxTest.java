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
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

@RunWith(Parameterized.class)
public class BlackboxTest implements SampleDatagramEnrichment {
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
        packet.writeBson(jsonWriter, this);
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

            final ByteBuf buf = Unpooled.wrappedBuffer(buffer);

            do {
                final SampleDatagram packet = new SampleDatagram(buf);

                dumpPacket(packet);
                assertThat(packet.version.version.value, is(0x0005));
            } while (buf.isReadable());
        }
    }

    @Override
    public Optional<String> getHostnameFor(InetAddress srcAddress) {
        return Optional.empty();
    }
}
