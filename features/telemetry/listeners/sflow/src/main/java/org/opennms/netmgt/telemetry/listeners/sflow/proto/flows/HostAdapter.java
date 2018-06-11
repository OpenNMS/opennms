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

package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Array;

import com.google.common.base.MoreObjects;

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

    public HostAdapter(final ByteBuffer buffer) throws InvalidPacketException {
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

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64(this.ifIndex);
        bsonWriter.writeStartArray("mac_address");
        for (final Mac mac : this.mac_address) {
            mac.writeBson(bsonWriter);
        }
        bsonWriter.writeEndArray();
        bsonWriter.writeEndDocument();
    }
}
