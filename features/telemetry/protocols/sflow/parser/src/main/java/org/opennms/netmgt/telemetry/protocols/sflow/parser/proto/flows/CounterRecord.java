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
package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import java.util.Map;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Opaque;

import com.google.common.collect.ImmutableMap;

import io.netty.buffer.ByteBuf;

// struct counter_record {
//    data_format counter_format;     /* The format of counter_data */
//    opaque counter_data<>;          /* A block of counters uniquely defined
//                                       by the counter_format. */
// };

public class CounterRecord extends Record<CounterData> {
    private static Map<DataFormat, Opaque.Parser<CounterData>> counterDataFormats = ImmutableMap.<DataFormat, Opaque.Parser<CounterData>>builder()
            .put(DataFormat.from(0, 1), IfCounters::new)
            .put(DataFormat.from(0, 2), EthernetCounters::new)
            .put(DataFormat.from(0, 3), TokenringCounters::new)
            .put(DataFormat.from(0, 4), VgCounters::new)
            .put(DataFormat.from(0, 5), VlanCounters::new)
            .put(DataFormat.from(0, 1001), Processor::new)
            .put(DataFormat.from(0, 6), Ieee80211Counters::new)
            .put(DataFormat.from(0, 1002), RadioUtilization::new)
            .put(DataFormat.from(4413, 3), HwTables::new)
            .put(DataFormat.from(4413, 1), BstDeviceBuffers::new)
            .put(DataFormat.from(4413, 2), BstPortBuffers::new)
            .put(DataFormat.from(0, 1004), OfPort::new)
            .put(DataFormat.from(0, 1005), PortName::new)
            .put(DataFormat.from(0, 2000), HostDescr::new)
            .put(DataFormat.from(0, 2001), HostAdapters::new)
            .put(DataFormat.from(0, 2002), HostParent::new)
            .put(DataFormat.from(0, 2003), HostCpu::new)
            .put(DataFormat.from(0, 2004), HostMemory::new)
            .put(DataFormat.from(0, 2005), HostDiskIo::new)
            .put(DataFormat.from(0, 2006), HostNetIo::new)
            .put(DataFormat.from(0, 2100), VirtNode::new)
            .put(DataFormat.from(0, 2101), VirtCpu::new)
            .put(DataFormat.from(0, 2102), VirtMemory::new)
            .put(DataFormat.from(0, 2103), VirtDiskIo::new)
            .put(DataFormat.from(0, 2104), VirtNetIo::new)
            .put(DataFormat.from(0, 2007), Mib2IpGroup::new)
            .put(DataFormat.from(0, 2008), Mib2IcmpGroup::new)
            .put(DataFormat.from(0, 2009), Mib2TcpGroup::new)
            .put(DataFormat.from(0, 2010), Mib2UdpGroup::new)
            .put(DataFormat.from(0, 2105), JvmRuntime::new)
            .put(DataFormat.from(0, 2106), JvmStatistics::new)
            .put(DataFormat.from(0, 2200), MemcacheOperation::new)
            .put(DataFormat.from(0, 2204), MemcacheCounters::new)
            .put(DataFormat.from(0, 2201), HttpCounters::new)
            .put(DataFormat.from(0, 2202), AppOperations::new)
            .put(DataFormat.from(0, 2203), AppResources::new)
            .put(DataFormat.from(0, 2206), AppWorkers::new)
            .put(DataFormat.from(5703, 1), NvidiaGpu::new)
            .build();

    public CounterRecord(final ByteBuf buffer) throws InvalidPacketException {
        super(buffer, counterDataFormats);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        if (data.value != null) {
            this.data.value.writeBson(bsonWriter, enr);
        } else {
            bsonWriter.writeNull();
        }
    }

    @Override
    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
