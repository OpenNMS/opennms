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

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct context {
//   application application;
//   operation operation;
//   attributes attributes;
// };

public class Context {
    public final Application application;
    public final Operation operation;
    public final Attributes attributes;

    public Context(final ByteBuf buffer) throws InvalidPacketException {
        this.application = new Application(buffer);
        this.operation = new Operation(buffer);
        this.attributes = new Attributes(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("application", this.application)
                .add("operation", this.operation)
                .add("attributes", this.attributes)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("application");
        this.application.writeBson(bsonWriter, enr);
        bsonWriter.writeName("operation");
        this.operation.writeBson(bsonWriter, enr);
        bsonWriter.writeName("attributes");
        this.attributes.writeBson(bsonWriter, enr);
        bsonWriter.writeEndDocument();
    }
}
