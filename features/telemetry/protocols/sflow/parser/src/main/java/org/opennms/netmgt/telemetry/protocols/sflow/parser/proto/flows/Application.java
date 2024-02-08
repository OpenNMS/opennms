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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Array;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// typedef utf8string application<32>;

public class Application {
    public final Array<Utf8string> application;

    public Application(final ByteBuf buffer) throws InvalidPacketException {
        this.application = new Array(buffer, Optional.empty(), Utf8string::new);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("application", this.application)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartArray();
        for (final Utf8string utf8string : this.application) {
            utf8string.writeBson(bsonWriter, enr);
        }
        bsonWriter.writeEndArray();
    }
}
