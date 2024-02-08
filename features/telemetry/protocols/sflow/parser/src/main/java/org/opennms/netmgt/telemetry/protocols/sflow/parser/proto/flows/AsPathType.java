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

    public AsPathType(final ByteBuf buffer) throws InvalidPacketException {
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

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("type");
        this.type.writeBson(bsonWriter, enr);

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
