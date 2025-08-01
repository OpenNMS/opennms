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
