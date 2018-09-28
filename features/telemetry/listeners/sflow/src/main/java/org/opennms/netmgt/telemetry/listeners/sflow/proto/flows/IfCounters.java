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

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;

// struct if_counters {
//    unsigned int ifIndex;
//    unsigned int ifType;
//    unsigned hyper ifSpeed;
//    unsigned int ifDirection;    /* derived from MAU MIB (RFC 2668)
//                                    0 = unkown, 1=full-duplex, 2=half-duplex,
//                                    3 = in, 4=out */
//    unsigned int ifStatus;       /* bit field with the following bits assigned
//                                    bit 0 = ifAdminStatus (0 = down, 1 = up)
//                                    bit 1 = ifOperStatus (0 = down, 1 = up) */
//    unsigned hyper ifInOctets;
//    unsigned int ifInUcastPkts;
//    unsigned int ifInMulticastPkts;
//    unsigned int ifInBroadcastPkts;
//    unsigned int ifInDiscards;
//    unsigned int ifInErrors;
//    unsigned int ifInUnknownProtos;
//    unsigned hyper ifOutOctets;
//    unsigned int ifOutUcastPkts;
//    unsigned int ifOutMulticastPkts;
//    unsigned int ifOutBroadcastPkts;
//    unsigned int ifOutDiscards;
//    unsigned int ifOutErrors;
//    unsigned int ifPromiscuousMode;
// };

public class IfCounters implements CounterData {
    public final long ifIndex;
    public final long ifType;
    public final UnsignedLong ifSpeed;
    public final long ifDirection;
    public final long ifStatus;
    public final UnsignedLong ifInOctets;
    public final long ifInUcastPkts;
    public final long ifInMulticastPkts;
    public final long ifInBroadcastPkts;
    public final long ifInDiscards;
    public final long ifInErrors;
    public final long ifInUnknownProtos;
    public final UnsignedLong ifOutOctets;
    public final long ifOutUcastPkts;
    public final long ifOutMulticastPkts;
    public final long ifOutBroadcastPkts;
    public final long ifOutDiscards;
    public final long ifOutErrors;
    public final long ifPromiscuousMode;

    public IfCounters(final ByteBuffer buffer) throws InvalidPacketException {
        this.ifIndex = BufferUtils.uint32(buffer);
        this.ifType = BufferUtils.uint32(buffer);
        this.ifSpeed = BufferUtils.uint64(buffer);
        this.ifDirection = BufferUtils.uint32(buffer);
        this.ifStatus = BufferUtils.uint32(buffer);
        this.ifInOctets = BufferUtils.uint64(buffer);
        this.ifInUcastPkts = BufferUtils.uint32(buffer);
        this.ifInMulticastPkts = BufferUtils.uint32(buffer);
        this.ifInBroadcastPkts = BufferUtils.uint32(buffer);
        this.ifInDiscards = BufferUtils.uint32(buffer);
        this.ifInErrors = BufferUtils.uint32(buffer);
        this.ifInUnknownProtos = BufferUtils.uint32(buffer);
        this.ifOutOctets = BufferUtils.uint64(buffer);
        this.ifOutUcastPkts = BufferUtils.uint32(buffer);
        this.ifOutMulticastPkts = BufferUtils.uint32(buffer);
        this.ifOutBroadcastPkts = BufferUtils.uint32(buffer);
        this.ifOutDiscards = BufferUtils.uint32(buffer);
        this.ifOutErrors = BufferUtils.uint32(buffer);
        this.ifPromiscuousMode = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ifIndex", this.ifIndex)
                .add("ifType", this.ifType)
                .add("ifSpeed", this.ifSpeed)
                .add("ifDirection", this.ifDirection)
                .add("ifStatus", this.ifStatus)
                .add("ifInOctets", this.ifInOctets)
                .add("ifInUcastPkts", this.ifInUcastPkts)
                .add("ifInMulticastPkts", this.ifInMulticastPkts)
                .add("ifInBroadcastPkts", this.ifInBroadcastPkts)
                .add("ifInDiscards", this.ifInDiscards)
                .add("ifInErrors", this.ifInErrors)
                .add("ifInUnknownProtos", this.ifInUnknownProtos)
                .add("ifOutOctets", this.ifOutOctets)
                .add("ifOutUcastPkts", this.ifOutUcastPkts)
                .add("ifOutMulticastPkts", this.ifOutMulticastPkts)
                .add("ifOutBroadcastPkts", this.ifOutBroadcastPkts)
                .add("ifOutDiscards", this.ifOutDiscards)
                .add("ifOutErrors", this.ifOutErrors)
                .add("ifPromiscuousMode", this.ifPromiscuousMode)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("ifIndex", this.ifIndex);
        bsonWriter.writeInt64("ifType", this.ifType);
        bsonWriter.writeInt64("ifSpeed", this.ifSpeed.longValue());
        bsonWriter.writeInt64("ifDirection", this.ifDirection);
        bsonWriter.writeInt64("ifStatus", this.ifStatus);
        bsonWriter.writeInt64("ifInOctets", this.ifInOctets.longValue());
        bsonWriter.writeInt64("ifInUcastPkts", this.ifInUcastPkts);
        bsonWriter.writeInt64("ifInMulticastPkts", this.ifInMulticastPkts);
        bsonWriter.writeInt64("ifInBroadcastPkts", this.ifInBroadcastPkts);
        bsonWriter.writeInt64("ifInDiscards", this.ifInDiscards);
        bsonWriter.writeInt64("ifInErrors", this.ifInErrors);
        bsonWriter.writeInt64("ifInUnknownProtos", this.ifInUnknownProtos);
        bsonWriter.writeInt64("ifOutOctets", this.ifOutOctets.longValue());
        bsonWriter.writeInt64("ifOutUcastPkts", this.ifOutUcastPkts);
        bsonWriter.writeInt64("ifOutMulticastPkts", this.ifOutMulticastPkts);
        bsonWriter.writeInt64("ifOutBroadcastPkts", this.ifOutBroadcastPkts);
        bsonWriter.writeInt64("ifOutDiscards", this.ifOutDiscards);
        bsonWriter.writeInt64("ifOutErrors", this.ifOutErrors);
        bsonWriter.writeInt64("ifPromiscuousMode", this.ifPromiscuousMode);
        bsonWriter.writeEndDocument();
    }
}
