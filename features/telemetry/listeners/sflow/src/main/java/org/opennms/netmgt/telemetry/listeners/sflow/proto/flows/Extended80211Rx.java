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

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.AsciiString;

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

public class Extended80211Rx {
    public final AsciiString ssid;
    public final Mac bssid;
    public final Ieee80211Version version;
    public final long channel;
    public final UnsignedLong speed;
    public final long rsni;
    public final long rcpi;
    public final DurationUs packet_duration;

    public Extended80211Rx(final ByteBuffer buffer) throws InvalidPacketException {
        this.ssid = new AsciiString(buffer, Optional.empty());
        this.bssid = new Mac(buffer);
        this.version = Ieee80211Version.from(buffer);
        this.channel = BufferUtils.uint32(buffer);
        this.speed = BufferUtils.uint64(buffer);
        this.rsni = BufferUtils.uint32(buffer);
        this.rcpi = BufferUtils.uint32(buffer);
        this.packet_duration = new DurationUs(buffer);
    }
}
