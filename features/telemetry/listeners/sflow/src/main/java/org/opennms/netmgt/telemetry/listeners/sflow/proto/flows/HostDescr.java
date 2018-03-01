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

import org.bson.BsonBinary;
import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.AsciiString;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Opaque;

import com.google.common.base.MoreObjects;

// struct host_descr {
//    string hostname<64>;       /* hostname, empty if unknown */
//    opaque uuid<16>;           /* 16 bytes binary UUID, empty if unknown */
//    machine_type machine_type; /* the processor family */
//    os_name os_name;           /* Operating system */
//    string os_release<32>;     /* e.g. 2.6.9-42.ELsmp,xp-sp3, empty if unknown */
// };

public class HostDescr implements CounterData {
    public final AsciiString hostname;
    public final Opaque<byte[]> uuid;
    public final MachineType machine_type;
    public final OsName os_name;
    public final AsciiString os_release;

    public HostDescr(final ByteBuffer buffer) throws InvalidPacketException {
        this.hostname = new AsciiString(buffer);
        this.uuid = new Opaque(buffer, Optional.of(16), Opaque::parseBytes);
        this.machine_type = MachineType.from(buffer);
        this.os_name = OsName.from(buffer);
        this.os_release = new AsciiString(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("hostname", this.hostname)
                .add("uuid", this.uuid)
                .add("machine_type", this.machine_type)
                .add("os_name", this.os_name)
                .add("os_release", this.os_release)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("hostname", this.hostname.value);
        bsonWriter.writeBinaryData("uuid", new BsonBinary(this.uuid.value));
        bsonWriter.writeName("machine_type");
        this.machine_type.writeBson(bsonWriter);
        bsonWriter.writeName("os_name");
        this.os_name.writeBson(bsonWriter);
        bsonWriter.writeString("os_release", this.os_release.value);
        bsonWriter.writeEndDocument();
    }
}
