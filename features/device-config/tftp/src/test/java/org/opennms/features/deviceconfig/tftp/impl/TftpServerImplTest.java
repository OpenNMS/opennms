/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.tftp.impl;

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

            assertThat(receiver.received, hasSize(1));
            assertThat(receiver.received.get(0).getKey(), equalTo(fileName));
            assertThat(receiver.received.get(0).getValue(), is(bytes));
        }

    }

}
