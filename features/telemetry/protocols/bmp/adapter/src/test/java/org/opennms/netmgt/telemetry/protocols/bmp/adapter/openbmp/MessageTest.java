/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Message;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Collector;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Peer;

import com.google.common.collect.ImmutableList;

public class MessageTest {

    @Test
    public void canCollectorSerializeMessageWithHeader() {
        final Collector collector = new Collector();
        collector.action = Collector.Action.CHANGE;
        collector.sequence = 8L;
        collector.adminId = "collector";
        collector.hash = "91e3a7ff9f5676ed6ae6fcd8a6b455ec";
        collector.routers = Collections.singletonList(InetAddressUtils.addr("10.10.10.10"));

        long timeMicros = 1_582_456_123_795_452L;
        collector.timestamp = Instant.EPOCH.plus(timeMicros, ChronoUnit.MICROS);

        Message msg = new Message("91e3a7ff9f5676ed6ae6fcd8a6b455ec", Type.COLLECTOR, ImmutableList.of(collector));

        final StringBuffer buffer = new StringBuffer();
        msg.serialize(buffer);
        assertThat(buffer.toString(), equalTo("V: 1.7\n" +
                "C_HASH_ID: 91e3a7ff9f5676ed6ae6fcd8a6b455ec\n" +
                "T: collector\n" +
                "L: 93\n" +
                "R: 1\n" +
                "\n" +
                "change\t8\tcollector\t91e3a7ff9f5676ed6ae6fcd8a6b455ec\t10.10.10.10\t1\t2020-02-23 11:08:43.795452\n"));
    }

    @Test
    public void canSerializePeerMessageWithHeader() {
        final Peer peer = new Peer();
        peer.action = Peer.Action.UP;
        peer.sequence = 8L;
        peer.hash = "93cd8f2759d4cf2e033c7a0584d0bfe7";
        peer.routerHash = "93cd8f2759d4cf2e033c7a0584d0bfe8";
        peer.name = "ie1-fd1";
        peer.remoteBgpId = InetAddressUtils.addr("10.10.10.10");
        peer.routerIp = InetAddressUtils.addr("10.10.10.1");
        long timeMicros = 1_582_466_123_795_452L;
        peer.timestamp = Instant.EPOCH.plus(timeMicros, ChronoUnit.MICROS);
        peer.peerRd = "0:0";
        peer.remotePort = 179;
        peer.localAsn = 651337L;

        Message msg = new Message("91e3a7ff9f5676ed6ae6fcd8a6b455ec", Type.PEER, ImmutableList.of(peer));

        final StringBuffer buffer = new StringBuffer();
        msg.serialize(buffer);
        assertThat(buffer.toString(), equalTo("V: 1.7\n" +
                "C_HASH_ID: 91e3a7ff9f5676ed6ae6fcd8a6b455ec\n" +
                "T: peer\n" +
                "L: 176\n" +
                "R: 1\n" +
                "\n" +
                "up\t8\t93cd8f2759d4cf2e033c7a0584d0bfe7\t93cd8f2759d4cf2e033c7a0584d0bfe8\tie1-fd1\t10.10.10.10\t10.10.10.1\t2020-02-23 13:55:23.795452\t0\t\t0:0\t179\t651337\t\t0\t\t\t\t\t0\t0\t0\t0\t0\t\t0\t0\t0\t0\t0\t\n"));
    }
}
