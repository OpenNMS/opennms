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

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import java.nio.ByteBuffer;
import java.util.Map;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Opaque;

import com.google.common.collect.ImmutableMap;

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

    public CounterRecord(final ByteBuffer buffer) throws InvalidPacketException {
        super(buffer, counterDataFormats);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        if (data.value != null) {
            this.data.value.writeBson(bsonWriter);
        } else {
            bsonWriter.writeNull();
        }
    }
}
