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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramVisitor;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.AsciiString;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;

import io.netty.buffer.ByteBuf;

// struct extended_80211_tx {
//    string ssid<32>;              /* SSID string */
//    mac  bssid;                   /* BSSID */
//    ieee80211_version version;    /* version */
//    unsigned int transmissions;   /* number of transmissions for sampled
//                                     packet.
//                                     0 = unkown
//                                     1 = packet was successfully transmitted
//                                         on first attempt
//                                     n > 1 = n - 1 retransmissions */
//    duration_us packet_duration;  /* amount of time that the successfully
//                                     transmitted packet occupied the
//                                     RF medium */
//    duration_us retrans_duration; /* amount of time that failed transmission
//                                     attempts occupied the RF medium */
//    unsigned int channel;         /* channel number */
//    unsigned hyper speed;
//    unsigned int power;           /* transmit power in mW. */
// };

public class Extended80211Tx implements FlowData {
    public final AsciiString ssid;
    public final Mac bssid;
    public final Ieee80211Version version;
    public final long transmissions;
    public final DurationUs packet_duration;
    public final DurationUs retrans_duration;
    public final long channel;
    public final UnsignedLong speed;
    public final long power;

    public Extended80211Tx(final ByteBuf buffer) throws InvalidPacketException {
        this.ssid = new AsciiString(buffer);
        this.bssid = new Mac(buffer);
        this.version = Ieee80211Version.from(buffer);
        this.transmissions = BufferUtils.uint32(buffer);
        this.packet_duration = new DurationUs(buffer);
        this.retrans_duration = new DurationUs(buffer);
        this.channel = BufferUtils.uint32(buffer);
        this.speed = BufferUtils.uint64(buffer);
        this.power = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ssid", this.ssid)
                .add("bssid", this.bssid)
                .add("version", this.version)
                .add("transmissions", this.transmissions)
                .add("packet_duration", this.packet_duration)
                .add("retrans_duration", this.retrans_duration)
                .add("channel", this.channel)
                .add("speed", this.speed)
                .add("power", this.power)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("ssid", this.ssid.value);

        bsonWriter.writeName("bssid");
        this.bssid.writeBson(bsonWriter, enr);
        bsonWriter.writeName("version");
        this.version.writeBson(bsonWriter, enr);

        bsonWriter.writeInt64("transmissions", this.transmissions);
        bsonWriter.writeName("packet_duration");
        this.packet_duration.writeBson(bsonWriter, enr);
        bsonWriter.writeName("retrans_duration");
        this.retrans_duration.writeBson(bsonWriter, enr);

        bsonWriter.writeInt64("channel", this.channel);
        bsonWriter.writeInt64("speed", this.speed.longValue());
        bsonWriter.writeInt64("power", this.power);

        bsonWriter.writeEndDocument();
    }

    @Override
    public void visit(SampleDatagramVisitor visitor) {
        visitor.accept(this);
    }
}
