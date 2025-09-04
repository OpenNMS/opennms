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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.TcpSession;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

@RunWith(Parameterized.class)
public class BlackboxTest {
    private final static Path FOLDER = Paths.get("src/test/resources/flows");

    private InformationElementDatabase database = new InformationElementDatabase(new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementProvider(), new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.InformationElementProvider());

    @Parameterized.Parameters(name = "file: {0}")
    public static Iterable<Object[]> data() throws IOException {
        return Arrays.asList(
                new Object[]{Arrays.asList("ipfix.dat")},
                new Object[]{Arrays.asList("ipfix_test_openbsd_pflow_tpl.dat", "ipfix_test_openbsd_pflow_data.dat")},
                new Object[]{Arrays.asList("ipfix_test_mikrotik_tpl.dat", "ipfix_test_mikrotik_data258.dat", "ipfix_test_mikrotik_data259.dat")},
                new Object[]{Arrays.asList("ipfix_test_vmware_vds_tpl.dat", "ipfix_test_vmware_vds_data264.dat", "ipfix_test_vmware_vds_data266.dat", "ipfix_test_vmware_vds_data266_267.dat")},
                new Object[]{Arrays.asList("ipfix_test_barracuda_tpl.dat", "ipfix_test_barracuda_data256.dat")},
                new Object[]{Arrays.asList("ipfix_test_yaf_tpls_option_tpl.dat", "ipfix_test_yaf_tpl45841.dat", "ipfix_test_yaf_data45841.dat", "ipfix_test_yaf_data45873.dat", "ipfix_test_yaf_data53248.dat")}
        );
    }

    private final List<String> files;

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("karaf.etc", "src/test/resources");
    }

    public BlackboxTest(final List<String> files) {
        this.files = files;
    }

    @Test
    public void testFiles() throws Exception {
        final Session session = new TcpSession(InetAddress.getLoopbackAddress(), () -> new SequenceNumberTracker(32));

        for (final String file : this.files) {
            try (final FileChannel channel = FileChannel.open(FOLDER.resolve(file))) {
                final ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
                channel.read(buffer);
                buffer.flip();

                final ByteBuf buf = Unpooled.wrappedBuffer(buffer);

                do {
                    final Header header = new Header(slice(buf, Header.SIZE));
                    final Packet packet = new Packet(database, session, header, slice(buf, header.length - Header.SIZE));

                    assertThat(packet.header.versionNumber, is(0x000a));

                } while (buf.isReadable());
            }
        }
    }
}
