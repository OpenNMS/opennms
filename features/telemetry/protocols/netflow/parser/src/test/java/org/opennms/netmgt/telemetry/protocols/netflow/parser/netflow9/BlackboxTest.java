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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9;

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
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.proto.Packet;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.SequenceNumberTracker;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.TcpSession;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.session.Session;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

@RunWith(Parameterized.class)
public class BlackboxTest {
    private final static Path FOLDER = Paths.get("src/test/resources/flows");

    @Parameterized.Parameters(name = "file: {0}")
    public static Iterable<Object[]> data() throws IOException {
        return Arrays.asList(
                new Object[]{Arrays.asList("netflow9_test_valid01.dat")},
                new Object[]{Arrays.asList("netflow9_test_macaddr_tpl.dat", "netflow9_test_macaddr_data.dat")},
                new Object[]{Arrays.asList("netflow9_test_cisco_asa_1_tpl.dat", "netflow9_test_cisco_asa_1_data.dat")},
                new Object[]{Arrays.asList("netflow9_test_nprobe_tpl.dat", "netflow9_test_softflowd_tpl_data.dat", "netflow9_test_nprobe_data.dat")},
                new Object[]{Arrays.asList("netflow9_test_cisco_asa_2_tpl_26x.dat", "netflow9_test_cisco_asa_2_tpl_27x.dat", "netflow9_test_cisco_asa_2_data.dat")},
                new Object[]{Arrays.asList("netflow9_test_ubnt_edgerouter_tpl.dat", "netflow9_test_ubnt_edgerouter_data1024.dat", "netflow9_test_ubnt_edgerouter_data1025.dat")},
                new Object[]{Arrays.asList("netflow9_test_nprobe_dpi.dat")},
                new Object[]{Arrays.asList("netflow9_test_fortigate_fortios_521_tpl.dat", "netflow9_test_fortigate_fortios_521_data256.dat", "netflow9_test_fortigate_fortios_521_data257.dat")},
                new Object[]{Arrays.asList("netflow9_test_streamcore_tpl_data256.dat", "netflow9_test_streamcore_tpl_data260.dat")},
                new Object[]{Arrays.asList("netflow9_test_juniper_srx_tplopt.dat")},
                new Object[]{Arrays.asList("netflow9_test_0length_fields_tpl_data.dat")},
                new Object[]{Arrays.asList("netflow9_test_cisco_asr9k_opttpl256.dat", "netflow9_test_cisco_asr9k_data256.dat")},
                new Object[]{Arrays.asList("netflow9_test_cisco_asr9k_tpl260.dat", "netflow9_test_cisco_asr9k_data260.dat")},
                new Object[]{Arrays.asList("netflow9_test_cisco_nbar_opttpl260.dat")},
                new Object[]{Arrays.asList("netflow9_test_cisco_nbar_tpl262.dat", "netflow9_test_cisco_nbar_data262.dat")},
                new Object[]{Arrays.asList("netflow9_test_cisco_wlc_tpl.dat", "netflow9_test_cisco_wlc_data261.dat")},
                new Object[]{Arrays.asList("netflow9_test_cisco_wlc_8510_tpl_262.dat")},
                new Object[]{Arrays.asList("netflow9_test_cisco_1941K9.dat")},
                new Object[]{Arrays.asList("netflow9_cisco_asr1001x_tpl259.dat")},
                new Object[]{Arrays.asList("netflow9_test_paloalto_panos_tpl.dat", "netflow9_test_paloalto_panos_data.dat")},
                new Object[]{Arrays.asList("netflow9_test_juniper_data_b4_tmpl.dat")},
                new Object[]{Arrays.asList("nms-14130.dat")}
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
                    final Packet packet = new Packet(session, header, buf);

                    assertThat(packet.header.versionNumber, is(0x0009));

                } while (buf.isReadable());
            }
        }
    }
}
