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
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.AsciiString;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct extended_mpls_vc {
//    string vc_instance_name<>;  /* VC instance name */
//    unsigned int vll_vc_id;     /* VLL/VC instance ID */
//    unsigned int vc_label_cos;  /* VC Label COS value */
// };

public class ExtendedMplsVc implements FlowData {
    public final AsciiString vc_instance_name;
    public final long vll_vc_id;
    public final long vc_label_cos;

    public ExtendedMplsVc(final ByteBuf buffer) throws InvalidPacketException {
        this.vc_instance_name = new AsciiString(buffer);
        this.vll_vc_id = BufferUtils.uint32(buffer);
        this.vc_label_cos = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("vc_instance_name", this.vc_instance_name)
                .add("vll_vc_id", this.vll_vc_id)
                .add("vc_label_cos", this.vc_label_cos)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("vc_instance_name", this.vc_instance_name.value);
        bsonWriter.writeInt64("vll_vc_id", this.vll_vc_id);
        bsonWriter.writeInt64("vc_label_cos", this.vc_label_cos);
        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(final SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
