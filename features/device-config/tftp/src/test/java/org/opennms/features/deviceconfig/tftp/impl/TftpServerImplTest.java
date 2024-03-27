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
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.net.tftp.TFTP;
import org.apache.commons.net.tftp.TFTPClient;
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
    public void test() throws Exception {
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

}
