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

// struct mib2_udp_group {
//   unsigned int udpInDatagrams;
//   unsigned int udpNoPorts;
//   unsigned int udpInErrors;
//   unsigned int udpOutDatagrams;
//   unsigned int udpRcvbufErrors;
//   unsigned int udpSndbufErrors;
//   unsigned int udpInCsumErrors;
// };

public class Mib2UdpGroup implements CounterData {
    public final long udpInDatagrams;
    public final long udpNoPorts;
    public final long udpInErrors;
    public final long udpOutDatagrams;
    public final long udpRcvbufErrors;
    public final long udpSndbufErrors;
    public final long udpInCsumErrors;

    public Mib2UdpGroup(final ByteBuf buffer) throws InvalidPacketException {
        this.udpInDatagrams = BufferUtils.uint32(buffer);
        this.udpNoPorts = BufferUtils.uint32(buffer);
        this.udpInErrors = BufferUtils.uint32(buffer);
        this.udpOutDatagrams = BufferUtils.uint32(buffer);
        this.udpRcvbufErrors = BufferUtils.uint32(buffer);
        this.udpSndbufErrors = BufferUtils.uint32(buffer);
        this.udpInCsumErrors = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("udpInDatagrams", this.udpInDatagrams)
                .add("udpNoPorts", this.udpNoPorts)
                .add("udpInErrors", this.udpInErrors)
                .add("udpOutDatagrams", this.udpOutDatagrams)
                .add("udpRcvbufErrors", this.udpRcvbufErrors)
                .add("udpSndbufErrors", this.udpSndbufErrors)
                .add("udpInCsumErrors", this.udpInCsumErrors)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("udpInDatagrams", this.udpInDatagrams);
        bsonWriter.writeInt64("udpNoPorts", this.udpNoPorts);
        bsonWriter.writeInt64("udpInErrors", this.udpInErrors);
        bsonWriter.writeInt64("udpOutDatagrams", this.udpOutDatagrams);
        bsonWriter.writeInt64("udpRcvbufErrors", this.udpRcvbufErrors);
        bsonWriter.writeInt64("udpSndbufErrors", this.udpSndbufErrors);
        bsonWriter.writeInt64("udpInCsumErrors", this.udpInCsumErrors);
        bsonWriter.writeEndDocument();
    }

}
