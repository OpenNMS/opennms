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

import org.bson.BsonBinary;
import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.AsciiString;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Opaque;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

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

    public HostDescr(final ByteBuf buffer) throws InvalidPacketException {
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
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("hostname", this.hostname.value);
        bsonWriter.writeBinaryData("uuid", new BsonBinary(this.uuid.value));
        bsonWriter.writeName("machine_type");
        this.machine_type.writeBson(bsonWriter);
        bsonWriter.writeName("os_name");
        this.os_name.writeBson(bsonWriter, enr);
        bsonWriter.writeString("os_release", this.os_release.value);
        bsonWriter.writeEndDocument();
    }
}
