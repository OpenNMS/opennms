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

    public SampleRecord(final ByteBuffer buffer) throws InvalidPacketException {
        super(buffer, sampleDataFormats);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();

        bsonWriter.writeString("format", this.dataFormat.toId());

        if (data.value != null) {
            bsonWriter.writeName("data");
            this.data.value.writeBson(bsonWriter);
        }

        bsonWriter.writeEndDocument();
    }
}
