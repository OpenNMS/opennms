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

import com.google.common.base.MoreObjects;

// enum header_protocol {
//    ETHERNET_ISO88023    = 1,
//    ISO88024_TOKENBUS    = 2,
//    ISO88025_TOKENRING   = 3,
//    FDDI                 = 4,
//    FRAME_RELAY          = 5,
//    X25                  = 6,
//    PPP                  = 7,
//    SMDS                 = 8,                
//    AAL5                 = 9,
//    AAL5_IP              = 10, /* e.g. Cisco AAL5 mux */
//    IPv4                 = 11,
//    IPv6                 = 12,
//    MPLS                 = 13,
//    POS                  = 14,  /* RFC 1662, 2615 */
//    IEEE80211MAC         = 15,  /* 802.11 MAC */
//    IEEE80211-AMPDU      = 16,  /* 802.11n Aggregated MPDU (A-MPDU)
//                                   starting with MPDU delimiter */
//    IEEE80211-AMSDU-Subframe = 17
// };

public enum HeaderProtocol {
    ETHERNET_ISO88023(1),
    ISO88024_TOKENBUS(2),
    ISO88025_TOKENRING(3),
    FDDI(4),
    FRAME_RELAY(5),
    X25(6),
    PPP(7),
    SMDS(8),
    AAL5(9),
    AAL5_IP(10),
    IPv4(11),
    IPv6(12),
    MPLS(13),
    POS(14),
    IEEE80211MAC(15),
    IEEE80211_AMPDU(16),
    IEEE80211_AMSDU_Subframe(17);

    public final int value;

    HeaderProtocol(final int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("value", this.value)
                .toString();
    }

    public static HeaderProtocol from(final ByteBuffer buffer) throws InvalidPacketException {
        final int value = (int) BufferUtils.uint32(buffer);
        switch (value) {
            case 1:
                return ETHERNET_ISO88023;
            case 2:
                return ISO88024_TOKENBUS;
            case 3:
                return ISO88025_TOKENRING;
            case 4:
                return FDDI;
            case 5:
                return FRAME_RELAY;
            case 6:
                return X25;
            case 7:
                return PPP;
            case 8:
                return SMDS;
            case 9:
                return AAL5;
            case 10:
                return AAL5_IP;
            case 11:
                return IPv4;
            case 12:
                return IPv6;
            case 13:
                return MPLS;
            case 14:
                return POS;
            case 15:
                return IEEE80211MAC;
            case 16:
                return IEEE80211_AMPDU;
            case 17:
                return IEEE80211_AMSDU_Subframe;
            default:
                throw new InvalidPacketException(buffer, "Unknown value: {}", value);
        }
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeInt32(this.value);
    }
}
