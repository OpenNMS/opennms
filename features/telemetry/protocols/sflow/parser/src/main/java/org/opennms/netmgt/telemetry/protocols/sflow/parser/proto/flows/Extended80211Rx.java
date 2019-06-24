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

import java.nio.ByteBuffer;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.AsciiString;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;

// struct extended_80211_rx {
//    string ssid<32>;            /* SSID string */
//    mac  bssid;                 /* BSSID */
//    ieee80211_version version;  /* version */
//    unsigned int channel;       /* channel number */
//    unsigned hyper speed;
//    unsigned int rsni;          /* received signal to noise ratio,
//                                   see dot11FrameRprtRSNI */
//    unsigned int rcpi;          /* received channel power,
//                                   see dot11FrameRprtLastRCPI */
//    duration_us packet_duration; /* amount of time that the successfully
//                                   received packet occupied the RF medium. */
// };

public class Extended80211Rx implements FlowData {
    public final AsciiString ssid;
    public final Mac bssid;
    public final Ieee80211Version version;
    public final long channel;
    public final UnsignedLong speed;
    public final long rsni;
    public final long rcpi;
    public final DurationUs packet_duration;

    public Extended80211Rx(final ByteBuffer buffer) throws InvalidPacketException {
        this.ssid = new AsciiString(buffer);
        this.bssid = new Mac(buffer);
        this.version = Ieee80211Version.from(buffer);
        this.channel = BufferUtils.uint32(buffer);
        this.speed = BufferUtils.uint64(buffer);
        this.rsni = BufferUtils.uint32(buffer);
        this.rcpi = BufferUtils.uint32(buffer);
        this.packet_duration = new DurationUs(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ssid", this.ssid)
                .add("bssid", this.bssid)
                .add("version", this.version)
                .add("channel", this.channel)
                .add("speed", this.speed)
                .add("rsni", this.rsni)
                .add("rcpi", this.rcpi)
                .add("packet_duration", this.packet_duration)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("ssid", this.ssid.value);

        bsonWriter.writeName("bssid");
        this.bssid.writeBson(bsonWriter);
        bsonWriter.writeName("version");
        this.version.writeBson(bsonWriter);

        bsonWriter.writeInt64("channel", this.channel);
        bsonWriter.writeInt64("speed", this.speed.longValue());
        bsonWriter.writeInt64("rsni", this.rsni);
        bsonWriter.writeInt64("rcpi", this.rcpi);

        bsonWriter.writeName("packet_duration");
        this.packet_duration.writeBson(bsonWriter);
        bsonWriter.writeEndDocument();
    }
}
