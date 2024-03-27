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

import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Array;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct host_adapter {
//    unsigned int ifIndex;     /* ifIndex associated with adapter
//                                 Must match ifIndex of vSwitch
//                                 port if vSwitch is exporting sFlow
//                                 0 = unknown */
//    mac mac_address<>;        /* Adapter MAC address(es) */
// };

public class HostAdapter {
    public final long ifIndex;
    public final Array<Mac> mac_address;

    public HostAdapter(final ByteBuf buffer) throws InvalidPacketException {
        this.ifIndex = BufferUtils.uint32(buffer);
        this.mac_address = new Array(buffer, Optional.empty(), Mac::new);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ifIndex", this.ifIndex)
                .add("mac_address", this.mac_address)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("host_adapter");
        bsonWriter.writeInt64(this.ifIndex);
        bsonWriter.writeStartArray("mac_address");
        for (final Mac mac : this.mac_address) {
            mac.writeBson(bsonWriter, enr);
        }
        bsonWriter.writeEndArray();
        bsonWriter.writeEndDocument();
    }
}
