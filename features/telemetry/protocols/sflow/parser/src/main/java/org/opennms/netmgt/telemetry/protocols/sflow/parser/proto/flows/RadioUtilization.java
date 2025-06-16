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

// struct radio_utilization {
//    unsigned int elapsed_time;         /* elapsed time in ms */
//    unsigned int on_channel_time;      /* time in ms spent on channel */
//    unsigned int on_channel_busy_time; /* time in ms spent on channel
//                                          and busy */
// };

public class RadioUtilization implements CounterData {
    public final long elapsed_time;
    public final long on_channel_time;
    public final long on_channel_busy_time;

    public RadioUtilization(final ByteBuf buffer) throws InvalidPacketException {
        this.elapsed_time = BufferUtils.uint32(buffer);
        this.on_channel_time = BufferUtils.uint32(buffer);
        this.on_channel_busy_time = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("elapsed_time", this.elapsed_time)
                .add("on_channel_time", this.on_channel_time)
                .add("on_channel_busy_time", this.on_channel_busy_time)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("elapsed_time", this.elapsed_time);
        bsonWriter.writeInt64("on_channel_time", this.on_channel_time);
        bsonWriter.writeInt64("on_channel_busy_time", this.on_channel_busy_time);
        bsonWriter.writeEndDocument();
    }
}
