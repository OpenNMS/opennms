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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ifIndex", ifIndex)
                .add("ifType", ifType)
                .add("ifSpeed", ifSpeed)
                .add("ifDirection", ifDirection)
                .add("ifStatus", ifStatus)
                .add("ifInOctets", ifInOctets)
                .add("ifInUcastPkts", ifInUcastPkts)
                .add("ifInMulticastPkts", ifInMulticastPkts)
                .add("ifInBroadcastPkts", ifInBroadcastPkts)
                .add("ifInDiscards", ifInDiscards)
                .add("ifInErrors", ifInErrors)
                .add("ifInUnknownProtos", ifInUnknownProtos)
                .add("ifOutOctets", ifOutOctets)
                .add("ifOutUcastPkts", ifOutUcastPkts)
                .add("ifOutMulticastPkts", ifOutMulticastPkts)
                .add("ifOutBroadcastPkts", ifOutBroadcastPkts)
                .add("ifOutDiscards", ifOutDiscards)
                .add("ifOutErrors", ifOutErrors)
                .add("ifPromiscuousMode", ifPromiscuousMode)
                .toString();
    }

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
}
