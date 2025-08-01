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

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.AsciiString;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct jvm_runtime {
//   string vm_name<64>;      /* vm name */
//   string vm_vendor<32>;    /* the vendor for the JVM */
//   string vm_version<32>;   /* the version for the JVM */
// };

public class JvmRuntime implements CounterData {
    public final AsciiString vm_name;
    public final AsciiString vm_vendor;
    public final AsciiString vm_version;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("vm_name", this.vm_name)
                .add("vm_vendor", this.vm_vendor)
                .add("vm_version", this.vm_version)
                .toString();
    }

    public JvmRuntime(final ByteBuf buffer) throws InvalidPacketException {
        this.vm_name = new AsciiString(buffer);
        this.vm_vendor = new AsciiString(buffer);
        this.vm_version = new AsciiString(buffer);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("vm_name", this.vm_name.value);
        bsonWriter.writeString("vm_vendor", this.vm_vendor.value);
        bsonWriter.writeString("vm_version", this.vm_version.value);
        bsonWriter.writeEndDocument();
    }
}
