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

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// union sample_datagram_type switch (datagram_version version) {
//    case VERSION5:
//       sample_datagram_v5 datagram;
// };

public class SampleDatagramType {
    public final DatagramVersion version;
    public final SampleDatagramV5 datagram;

    public SampleDatagramType(final ByteBuf buffer) throws InvalidPacketException {
        this.version = DatagramVersion.from(buffer);
        switch (this.version) {
            case VERSION5: {
                this.datagram = new SampleDatagramV5(buffer);
                break;
            }
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("version", this.version)
                .add("datagram", this.datagram)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("version");
        this.version.writeBson(bsonWriter, enr);
        bsonWriter.writeName("datagram");
        this.datagram.writeBson(bsonWriter, enr);
        bsonWriter.writeEndDocument();
    }

    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
        datagram.visit(visitor);
    }
}
