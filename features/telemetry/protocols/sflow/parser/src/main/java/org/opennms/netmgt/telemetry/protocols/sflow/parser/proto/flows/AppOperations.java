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

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct app_operations {
//   application application;
//   unsigned int success;
//   unsigned int other;
//   unsigned int timeout;
//   unsigned int internal_error;
//   unsigned int bad_request;
//   unsigned int forbidden;
//   unsigned int too_large;
//   unsigned int not_implemented;
//   unsigned int not_found;
//   unsigned int unavailable;
//   unsigned int unauthorized;
// };

public class AppOperations implements CounterData {
    public final Application application;
    public final long success;
    public final long other;
    public final long timeout;
    public final long internal_error;
    public final long bad_request;
    public final long forbidden;
    public final long too_large;
    public final long not_implemented;
    public final long not_found;
    public final long unavailable;
    public final long unauthorized;

    public AppOperations(final ByteBuf buffer) throws InvalidPacketException {
        this.application = new Application(buffer);
        this.success = BufferUtils.uint32(buffer);
        this.other = BufferUtils.uint32(buffer);
        this.timeout = BufferUtils.uint32(buffer);
        this.internal_error = BufferUtils.uint32(buffer);
        this.bad_request = BufferUtils.uint32(buffer);
        this.forbidden = BufferUtils.uint32(buffer);
        this.too_large = BufferUtils.uint32(buffer);
        this.not_implemented = BufferUtils.uint32(buffer);
        this.not_found = BufferUtils.uint32(buffer);
        this.unavailable = BufferUtils.uint32(buffer);
        this.unauthorized = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("application", this.application)
                .add("success", this.success)
                .add("other", this.other)
                .add("timeout", this.timeout)
                .add("internal_error", this.internal_error)
                .add("bad_request", this.bad_request)
                .add("forbidden", this.forbidden)
                .add("too_large", this.too_large)
                .add("not_implemented", this.not_implemented)
                .add("not_found", this.not_found)
                .add("unavailable", this.unavailable)
                .add("unauthorized", this.unauthorized)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("application");
        this.application.writeBson(bsonWriter, enr);
        bsonWriter.writeInt64("success", this.success);
        bsonWriter.writeInt64("other", this.other);
        bsonWriter.writeInt64("timeout", this.timeout);
        bsonWriter.writeInt64("internal_error", this.internal_error);
        bsonWriter.writeInt64("bad_request", this.bad_request);
        bsonWriter.writeInt64("forbidden", this.forbidden);
        bsonWriter.writeInt64("too_large", this.too_large);
        bsonWriter.writeInt64("not_implemented", this.not_implemented);
        bsonWriter.writeInt64("not_found", this.not_found);
        bsonWriter.writeInt64("unavailable", this.unavailable);
        bsonWriter.writeInt64("unauthorized", this.unauthorized);
        bsonWriter.writeEndDocument();
    }
}
