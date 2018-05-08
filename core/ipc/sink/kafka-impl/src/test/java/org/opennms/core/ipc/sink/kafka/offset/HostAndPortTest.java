/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.kafka.offset;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

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
