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

// struct sample_record {
//    data_format sample_type;       /* Specifies the type of sample data */
//    opaque sample_data<>;          /* A structure corresponding to the
//                                      sample_type */
// };

public class SampleRecord extends Record<SampleData> {
    private static Map<DataFormat, Opaque.Parser<SampleData>> sampleDataFormats = ImmutableMap.<DataFormat, Opaque.Parser<SampleData>>builder()
            .put(DataFormat.from(1), FlowSample::new)
            .put(DataFormat.from(2), CountersSample::new)
            .put(DataFormat.from(3), FlowSampleExpanded::new)
            .put(DataFormat.from(4), CountersSampleExpanded::new)
            .build();

    public SampleRecord(final ByteBuf buffer) throws InvalidPacketException {
        super(buffer, sampleDataFormats);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();

        bsonWriter.writeString("format", this.dataFormat.toId());

        if (data.value != null) {
            bsonWriter.writeName("data");
            this.data.value.writeBson(bsonWriter, enr);
        }

        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
        if (data.value != null) {
            data.value.visit(visitor);
        }
    }
}
