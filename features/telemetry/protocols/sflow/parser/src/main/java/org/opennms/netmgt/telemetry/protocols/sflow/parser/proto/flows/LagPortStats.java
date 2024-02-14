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

import org.bson.BsonBinary;
import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.Opaque;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

// struct lag_port_stats {
//   mac dot3adAggPortActorSystemID;
//   mac dot3adAggPortPartnerOperSystemID;
//   unsigned int dot3adAggPortAttachedAggID;
//   opaque dot3adAggPortState[4]; /*
//                              Bytes are assigned in following order:
//                              byte 0, value dot3adAggPortActorAdminState
//                              byte 1, value dot3adAggPortActorOperState
//                              byte 2, value dot3adAggPortPartnerAdminState
//                              byte 3, value dot3adAggPortPartnerOperState
//                                  */
//   unsigned int dot3adAggPortStatsLACPDUsRx;
//   unsigned int dot3adAggPortStatsMarkerPDUsRx;
//   unsigned int dot3adAggPortStatsMarkerResponsePDUsRx;
//   unsigned int dot3adAggPortStatsUnknownRx;
//   unsigned int dot3adAggPortStatsIllegalRx;
//   unsigned int dot3adAggPortStatsLACPDUsTx;
//   unsigned int dot3adAggPortStatsMarkerPDUsTx;
//   unsigned int dot3adAggPortStatsMarkerResponsePDUsTx;
// };

public class LagPortStats {
    public final Mac dot3adAggPortActorSystemID;
    public final Mac dot3adAggPortPartnerOperSystemID;
    public final long dot3adAggPortAttachedAggID;
    public final Opaque<byte[]> dot3adAggPortState;
    public final long dot3adAggPortStatsLACPDUsRx;
    public final long dot3adAggPortStatsMarkerPDUsRx;
    public final long dot3adAggPortStatsMarkerResponsePDUsRx;
    public final long dot3adAggPortStatsUnknownRx;
    public final long dot3adAggPortStatsIllegalRx;
    public final long dot3adAggPortStatsLACPDUsTx;
    public final long dot3adAggPortStatsMarkerPDUsTx;
    public final long dot3adAggPortStatsMarkerResponsePDUsTx;

    public LagPortStats(final ByteBuf buffer) throws InvalidPacketException {
        this.dot3adAggPortActorSystemID = new Mac(buffer);
        this.dot3adAggPortPartnerOperSystemID = new Mac(buffer);
        this.dot3adAggPortAttachedAggID = BufferUtils.uint32(buffer);
        this.dot3adAggPortState = new Opaque(buffer, Optional.of(4), Opaque::parseBytes);
        this.dot3adAggPortStatsLACPDUsRx = BufferUtils.uint32(buffer);
        this.dot3adAggPortStatsMarkerPDUsRx = BufferUtils.uint32(buffer);
        this.dot3adAggPortStatsMarkerResponsePDUsRx = BufferUtils.uint32(buffer);
        this.dot3adAggPortStatsUnknownRx = BufferUtils.uint32(buffer);
        this.dot3adAggPortStatsIllegalRx = BufferUtils.uint32(buffer);
        this.dot3adAggPortStatsLACPDUsTx = BufferUtils.uint32(buffer);
        this.dot3adAggPortStatsMarkerPDUsTx = BufferUtils.uint32(buffer);
        this.dot3adAggPortStatsMarkerResponsePDUsTx = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("dot3adAggPortActorSystemID", this.dot3adAggPortActorSystemID)
                .add("dot3adAggPortPartnerOperSystemID", this.dot3adAggPortPartnerOperSystemID)
                .add("dot3adAggPortAttachedAggID", this.dot3adAggPortAttachedAggID)
                .add("dot3adAggPortState", this.dot3adAggPortState)
                .add("dot3adAggPortStatsLACPDUsRx", this.dot3adAggPortStatsLACPDUsRx)
                .add("dot3adAggPortStatsMarkerPDUsRx", this.dot3adAggPortStatsMarkerPDUsRx)
                .add("dot3adAggPortStatsMarkerResponsePDUsRx", this.dot3adAggPortStatsMarkerResponsePDUsRx)
                .add("dot3adAggPortStatsUnknownRx", this.dot3adAggPortStatsUnknownRx)
                .add("dot3adAggPortStatsIllegalRx", this.dot3adAggPortStatsIllegalRx)
                .add("dot3adAggPortStatsLACPDUsTx", this.dot3adAggPortStatsLACPDUsTx)
                .add("dot3adAggPortStatsMarkerPDUsTx", this.dot3adAggPortStatsMarkerPDUsTx)
                .add("dot3adAggPortStatsMarkerResponsePDUsTx", this.dot3adAggPortStatsMarkerResponsePDUsTx)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("dot3adAggPortActorSystemID");
        this.dot3adAggPortActorSystemID.writeBson(bsonWriter, enr);
        bsonWriter.writeName("dot3adAggPortPartnerOperSystemID");
        this.dot3adAggPortPartnerOperSystemID.writeBson(bsonWriter, enr);
        bsonWriter.writeInt64("dot3adAggPortAttachedAggID", this.dot3adAggPortAttachedAggID);
        bsonWriter.writeBinaryData("dot3adAggPortState", new BsonBinary(this.dot3adAggPortState.value));
        bsonWriter.writeInt64("dot3adAggPortStatsLACPDUsRx", this.dot3adAggPortStatsLACPDUsRx);
        bsonWriter.writeInt64("dot3adAggPortStatsMarkerPDUsRx", this.dot3adAggPortStatsMarkerPDUsRx);
        bsonWriter.writeInt64("dot3adAggPortStatsMarkerResponsePDUsRx", this.dot3adAggPortStatsMarkerResponsePDUsRx);
        bsonWriter.writeInt64("dot3adAggPortStatsUnknownRx", this.dot3adAggPortStatsUnknownRx);
        bsonWriter.writeInt64("dot3adAggPortStatsIllegalRx", this.dot3adAggPortStatsIllegalRx);
        bsonWriter.writeInt64("dot3adAggPortStatsLACPDUsTx", this.dot3adAggPortStatsLACPDUsTx);
        bsonWriter.writeInt64("dot3adAggPortStatsMarkerPDUsTx", this.dot3adAggPortStatsMarkerPDUsTx);
        bsonWriter.writeInt64("dot3adAggPortStatsMarkerResponsePDUsTx", this.dot3adAggPortStatsMarkerResponsePDUsTx);
        bsonWriter.writeEndDocument();
    }


}
