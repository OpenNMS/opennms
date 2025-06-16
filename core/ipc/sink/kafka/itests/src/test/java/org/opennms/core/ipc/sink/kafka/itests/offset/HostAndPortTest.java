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
package org.opennms.core.ipc.sink.kafka.itests.offset;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opennms.core.ipc.sink.kafka.server.offset.HostAndPort;

public class HostAndPortTest {

    @Test
    public void testHostAndPortWithMultipleBrokers() {
        String kafkaHostString = "kafka:9092 , 127.0.0.1:9093 , 128.0.0.1:9094";
        HostAndPort hostAndPort = HostAndPort.fromString(kafkaHostString);
        assertEquals(9092, hostAndPort.getPort());
        assertEquals("kafka", hostAndPort.getHost());
        hostAndPort = HostAndPort.getNextHostAndPort(hostAndPort);
        assertEquals(9093, hostAndPort.getPort());
        assertEquals("127.0.0.1", hostAndPort.getHost());
        hostAndPort = HostAndPort.getNextHostAndPort(hostAndPort);
        assertEquals(9094, hostAndPort.getPort());
        assertEquals("128.0.0.1", hostAndPort.getHost());
        hostAndPort = HostAndPort.getNextHostAndPort(hostAndPort);
        assertEquals(9092, hostAndPort.getPort());
        assertEquals("kafka", hostAndPort.getHost());
        kafkaHostString = "kafka:9092";
        hostAndPort = HostAndPort.fromString(kafkaHostString);
        assertEquals(9092, hostAndPort.getPort());
        assertEquals("kafka", hostAndPort.getHost());
        hostAndPort = HostAndPort.getNextHostAndPort(hostAndPort);
        assertEquals(9092, hostAndPort.getPort());
        assertEquals("kafka", hostAndPort.getHost());
    }
}
