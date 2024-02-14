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
package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.time.Instant;
import java.util.Map;

import org.junit.Test;
import org.openbmp.api.parsed.message.BaseAttributePojo;
import org.openbmp.api.parsed.message.CollectorPojo;
import org.openbmp.api.parsed.message.PeerPojo;
import org.openbmp.api.parsed.message.RouterPojo;
import org.openbmp.api.parsed.message.UnicastPrefixPojo;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Record;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.BaseAttribute;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Collector;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Peer;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Router;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Stat;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.UnicastPrefix;

public class RecordSerdesTest {

    @Test
    public void canSerdesCollectorRecords() {
        // Build
        org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Collector collectorRecord = new org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Collector();
        collectorRecord.action = Collector.Action.STARTED;
        collectorRecord.sequence = 42L;

        // Serialize
        final String recordAdStr = toTSV(collectorRecord);

        // De-serialize
        org.openbmp.api.parsed.processor.Collector collectorMsg = new org.openbmp.api.parsed.processor.Collector(recordAdStr);
        assertThat(collectorMsg.records, hasSize(1));

        // Compare
        CollectorPojo collectorPojo = collectorMsg.records.get(0);
        assertThat(collectorPojo.getSequence().longValue(), equalTo(collectorRecord.sequence));
    }

    @Test
    public void canSerdesRouterRecords() {
        // Build
        org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Router routerRecord = new org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Router();
        routerRecord.action = Router.Action.FIRST;
        routerRecord.sequence = 42L;

        // Serialize
        final String recordAdStr = toTSV(routerRecord);

        // De-serialize
        org.openbmp.api.parsed.processor.Router routerMsg = new org.openbmp.api.parsed.processor.Router(recordAdStr);
        assertThat(routerMsg.records, hasSize(1));

        // Compare
        RouterPojo routerPojo = routerMsg.records.get(0);
        assertThat(routerPojo.getSequence().longValue(), equalTo(routerRecord.sequence));
    }

    @Test
    public void canSerdesPeerRecords() {
        // Build
        org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Peer peerRecord = new org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Peer();
        peerRecord.action = Peer.Action.FIRST;
        peerRecord.sequence = 42L;

        // Serialize
        final String recordAdStr = toTSV(peerRecord);

        // De-serialize
        org.openbmp.api.parsed.processor.Peer peerMsg = new org.openbmp.api.parsed.processor.Peer(recordAdStr);
        assertThat(peerMsg.records, hasSize(1));

        // Compare
        PeerPojo peerPojo = peerMsg.records.get(0);
        assertThat(peerPojo.getSequence().longValue(), equalTo(peerRecord.sequence));
    }

    @Test
    public void canSerdesStatRecords() {
        // Build
        org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Stat statRecord = new org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Stat();
        statRecord.action = Stat.Action.ADD;
        statRecord.sequence = 42L;
        statRecord.routerHash = "oops";
        statRecord.routerIp = InetAddressUtils.getLocalHostAddress();
        statRecord.peerHash = "ahh";
        statRecord.peerIp = InetAddressUtils.getLocalHostAddress();
        statRecord.peerAsn = 1L;
        statRecord.timestamp = Instant.ofEpochSecond(99L);

        statRecord.prefixesRejected = 1;
        statRecord.knownDupPrefixes = 2;
        statRecord.knownDupWithdraws = 3;
        statRecord.invalidClusterList = 4;
        statRecord.invalidAsPath = 5;
        statRecord.invalidOriginatorId = 6;
        statRecord.invalidAsConfed = 7;
        statRecord.prefixesPrePolicy = 8L;
        statRecord.prefixesPostPolicy = 9L;

        // Serialize
        final String recordAdStr = toTSV(statRecord);

        // De-serialize
        org.openbmp.api.parsed.message.BmpStat statMsg = new org.openbmp.api.parsed.message.BmpStat(recordAdStr);
        assertThat(statMsg.getRowMap(), hasSize(1));

        // Compare
        Map<String,Object> row = statMsg.getRowMap().get(0);
        assertThat(row.get("pre_policy"), equalTo("8"));
    }

    @Test
    public void canSerdesBaseAttributeRecords() {
        // Build
        org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.BaseAttribute baseAttributeRecord = new org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.BaseAttribute();
        baseAttributeRecord.action = BaseAttribute.Action.ADD;
        baseAttributeRecord.sequence = 42L;

        // Serialize
        final String recordAdStr = toTSV(baseAttributeRecord);

        // De-serialize
        org.openbmp.api.parsed.processor.BaseAttribute baseAttributeMsg = new org.openbmp.api.parsed.processor.BaseAttribute(recordAdStr);
        assertThat(baseAttributeMsg.records, hasSize(1));

        // Compare
        BaseAttributePojo baseAttributePojo = baseAttributeMsg.records.get(0);
        assertThat(baseAttributePojo.getSequence().longValue(), equalTo(baseAttributeRecord.sequence));
    }

    @Test
    public void canSerdesUnicastPrefixRecords() {
        // Build
        org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.UnicastPrefix unicastPrefixRecord = new org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.UnicastPrefix();
        unicastPrefixRecord.action = UnicastPrefix.Action.ADD;
        unicastPrefixRecord.sequence = 42L;

        // Serialize
        final String recordAdStr = toTSV(unicastPrefixRecord);

        // De-serialize
        org.openbmp.api.parsed.processor.UnicastPrefix unicastPrefixMsg = new org.openbmp.api.parsed.processor.UnicastPrefix(recordAdStr);
        assertThat(unicastPrefixMsg.records, hasSize(1));

        // Compare
        UnicastPrefixPojo unicastPrefixPojo = unicastPrefixMsg.records.get(0);
        assertThat(unicastPrefixPojo.getSequence().longValue(), equalTo(unicastPrefixRecord.sequence));
    }

    private static String toTSV(Record record) {
        final StringBuffer buffer = new StringBuffer();
        record.serialize(buffer);
        return buffer.toString();
    }
}
