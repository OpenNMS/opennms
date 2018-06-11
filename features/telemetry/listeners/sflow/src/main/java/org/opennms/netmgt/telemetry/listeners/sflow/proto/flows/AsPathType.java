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

// union as_path_type switch (as_path_segment_type type) {
//    case AS_SET:
//       unsigned int as_set<>;
//    case AS_SEQUENCE:
//       unsigned int as_sequence<>;
// };

public class AsPathType {
    public final AsPathSegmentType type;
    public final Array<Long> asSet;
    public final Array<Long> asSequence;

    public AsPathType(final ByteBuffer buffer) throws InvalidPacketException {
        this.type = AsPathSegmentType.from(buffer);
        switch (this.type) {
            case AS_SET:
                this.asSet = new Array<>(buffer, Optional.empty(), BufferUtils::uint32);
                this.asSequence = null;
                break;
            case AS_SEQUENCE:
                this.asSet = null;
                this.asSequence = new Array<>(buffer, Optional.empty(), BufferUtils::uint32);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", this.type)
                .add("asSet", this.asSet)
                .add("asSequence", this.asSequence)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("type");
        this.type.writeBson(bsonWriter);

        switch (this.type) {
            case AS_SET:
                bsonWriter.writeStartArray("asSet");
                for (final Long longValue : this.asSet) {
                    bsonWriter.writeInt64(longValue);
                }
                bsonWriter.writeEndArray();
                break;
            case AS_SEQUENCE:
                bsonWriter.writeStartArray("asSequence");
                for (final Long longValue : this.asSequence) {
                    bsonWriter.writeInt64(longValue);
                }
                bsonWriter.writeEndArray();
                break;
            default:
                throw new IllegalStateException();
        }

        bsonWriter.writeEndDocument();
    }
}
