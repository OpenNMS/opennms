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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Array;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;

import io.netty.buffer.ByteBuf;

// struct app_operation {
//   context context;             /* attributes describing the operation */
//   utf8string status_descr<64>; /* additional text describing status
//                                   (e.g. "unknown client") */
//   unsigned hyper req_bytes;    /* size of request body (exclude headers) */
//   unsigned hyper resp_bytes;   /* size of response body (exclude headers) */
//   unsigned int uS;             /* duration of the operation (microseconds) */
//   status status;               /* status code */
// };

public class AppOperation implements FlowData {
    public final Context context;
    public final Array<Utf8string> status_descr;
    public final UnsignedLong req_bytes;
    public final UnsignedLong resp_bytes;
    public final long uS;
    public final Status status;

    public AppOperation(final ByteBuf buffer) throws InvalidPacketException {
        this.context = new Context(buffer);
        this.status_descr = new Array(buffer, Optional.empty(), Utf8string::new);
        this.req_bytes = BufferUtils.uint64(buffer);
        this.resp_bytes = BufferUtils.uint64(buffer);
        this.uS = BufferUtils.uint32(buffer);
        this.status = Status.from(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("context", this.context)
                .add("status_descr", this.status_descr)
                .add("req_bytes", this.req_bytes)
                .add("resp_bytes", this.resp_bytes)
                .add("uS", this.uS)
                .add("", this.status)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();

        bsonWriter.writeName("context");
        this.context.writeBson(bsonWriter, enr);

        bsonWriter.writeStartArray("status_descr");
        for (final Utf8string utf8string : this.status_descr) {
            utf8string.writeBson(bsonWriter, enr);
        }
        bsonWriter.writeEndArray();

        bsonWriter.writeInt64("req_bytes", this.req_bytes.longValue());

        bsonWriter.writeInt64("resp_bytes", this.resp_bytes.longValue());

        bsonWriter.writeInt64("uS", this.uS);

        bsonWriter.writeName("status");
        this.status.writeBson(bsonWriter, enr);

        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
