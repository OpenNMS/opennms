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
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

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

    public static HeaderProtocol from(final ByteBuf buffer) throws InvalidPacketException {
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
