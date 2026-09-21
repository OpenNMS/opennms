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
package org.opennms.features.deviceconfig.tftp.impl;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.net.tftp.TFTP;
import org.apache.commons.net.tftp.TFTPClient;
import org.apache.commons.net.tftp.TFTPPacket;
import org.junit.Test;
import org.opennms.features.deviceconfig.tftp.TftpFileReceiver;

public class TftpServerImplTest {

    private static class Receiver implements TftpFileReceiver {
        public List<Pair<String, byte[]>> received = new ArrayList<>();
        
        @Override
        public void onFileReceived(InetAddress address, String fileName, byte[] content) {
            received.add(Pair.of(fileName, content));
        }
    }
    
    @Test
    public void testTftpServerImpl() throws Exception {
        try(var server = new TftpServerImpl()) {
            var port = 6903;
            server.setPort(port);
            server.launch();
            var receiver = new Receiver();
            server.register(receiver);
            var client = new TFTPClient();
            var bytes = new byte[10000];
            new Random().nextBytes(bytes);
            var fileName = "test";
            try {
                client.open();
                client.sendFile(fileName, TFTP.BINARY_MODE, new ByteArrayInputStream(bytes), "localhost", port);
            } finally {
                client.close();
            }

            await().untilAsserted(() -> assertThat(receiver.received, hasSize(1)));
            assertThat(receiver.received.get(0).getKey(), equalTo(fileName));
            assertThat(receiver.received.get(0).getValue(), is(bytes));

            var statistics = server.getStatistics();
            assertThat(statistics.bytesReceived(), is(Long.valueOf(bytes.length)));
            assertThat(statistics.warnings(), is(0));
            assertThat(statistics.errors(), is(0));
        }

    }

    @Test
    public void testBlksizeNegotiation() throws Exception {
        try (var server = new TftpServerImpl()) {
            var port = 6904;
            server.setPort(port);
            server.launch();

            var receiver = new Receiver();
            server.register(receiver);

            var bytes = new byte[2500];
            new Random().nextBytes(bytes);
            var fileName = "test-blksize";
            var blksize = 1024;

            try (var socket = new DatagramSocket()) {
                socket.setSoTimeout(5000);
                var localhost = InetAddress.getByName("localhost");

                // Send WRQ with blksize option
                var wrq = buildWrqWithBlksize(fileName, "octet", blksize, localhost, port);
                socket.send(wrq);

                // Expect OACK echoing our blksize
                var resp = new DatagramPacket(new byte[512], 512);
                socket.receive(resp);
                var respData = resp.getData();
                var respLength = resp.getLength();
                // opcode 6 = OACK
                assertThat((int) respData[0] << 8 | (respData[1] & 0xFF), is(TFTPPacket.OACK));

                var oackBlksize = "";
                var idx = 2;
                while (idx < respLength) {
                    var optionStart = idx;
                    while (idx < respLength && respData[idx] != 0) {
                        idx++;
                    }
                    var option = new String(respData, optionStart, idx - optionStart, StandardCharsets.US_ASCII);
                    idx++;

                    var valueStart = idx;
                    while (idx < respLength && respData[idx] != 0) {
                        idx++;
                    }
                    var value = new String(respData, valueStart, idx - valueStart, StandardCharsets.US_ASCII);
                    idx++;

                    if ("blksize".equalsIgnoreCase(option)) {
                        oackBlksize = value;
                        break;
                    }
                }
                assertThat(oackBlksize != "", is(true));
                assertThat(oackBlksize, is(String.valueOf(blksize)));
                // The server's transfer thread replies from its own port - use that for the rest
                var srvAddr = resp.getAddress();
                var srvPort = resp.getPort();

                // Send data blocks at the negotiated blksize
                var offset = 0;
                var block = 1;
                while (offset < bytes.length) {
                    var chunkSize = Math.min(blksize, bytes.length - offset);
                    var chunk = new byte[chunkSize];
                    System.arraycopy(bytes, offset, chunk, 0, chunkSize);
                    offset += chunkSize;

                    socket.send(buildDataPacket(block, chunk, srvAddr, srvPort));

                    var ack = new DatagramPacket(new byte[64], 64);
                    socket.receive(ack);
                    // opcode 4 = ACK, then 2-byte block number
                    assertThat((int) ack.getData()[1] & 0xFF, is(TFTPPacket.ACKNOWLEDGEMENT));
                    assertThat((ack.getData()[2] & 0xFF) << 8 | (ack.getData()[3] & 0xFF), is(block));

                    block++;
                }

                // If the last chunk was exactly blksize, send a zero-length terminator
                if (bytes.length % blksize == 0) {
                    socket.send(buildDataPacket(block, new byte[0], srvAddr, srvPort));
                    var ack = new DatagramPacket(new byte[64], 64);
                    socket.receive(ack);
                }
            }

            await().untilAsserted(() -> assertThat(receiver.received, hasSize(1)));
            assertThat(receiver.received.get(0).getKey(), equalTo(fileName));
            assertThat(receiver.received.get(0).getValue(), is(bytes));
        }
    }
    // -- Packet builder helpers ------------------------------------------------

    /**
     * Build a TFTP WRQ (opcode 2) with an RFC2348 blksize option.
     *
     *  2 bytes   string  1 byte  string  1 byte  string   1 byte  string  1 byte
     *  | 0x02 | filename | 0x00 | mode  | 0x00 | blksize  | 0x00 | value | 0x00 |
     */
    private static DatagramPacket buildWrqWithBlksize(String file, String mode, int blksize,
                                                      InetAddress addr, int port) throws Exception {
        var bos = new ByteArrayOutputStream();
        bos.write(0);
        bos.write(TFTPPacket.WRITE_REQUEST);
        bos.write(file.getBytes(StandardCharsets.US_ASCII));
        bos.write(0);
        bos.write(mode.getBytes(StandardCharsets.US_ASCII));
        bos.write(0);
        bos.write("blksize".getBytes(StandardCharsets.US_ASCII));
        bos.write(0);
        bos.write(String.valueOf(blksize).getBytes(StandardCharsets.US_ASCII));
        bos.write(0);
        var data = bos.toByteArray();
        return new DatagramPacket(data, data.length, addr, port);
    }

    /**
     * Build a TFTP DATA packet (opcode 3).
     *
     *  2 bytes  2 bytes   n bytes
     *  | 0x03 | block# |  data  |
     */
    private static DatagramPacket buildDataPacket(int block, byte[] payload,
                                                  InetAddress addr, int port) {
        var pkt = new byte[4 + payload.length];
        pkt[0] = 0;
        pkt[1] = TFTPPacket.DATA;
        pkt[2] = (byte) ((block >> 8) & 0xFF);
        pkt[3] = (byte) (block & 0xFF);
        System.arraycopy(payload, 0, pkt, 4, payload.length);
        return new DatagramPacket(pkt, pkt.length, addr, port);
    }

}
