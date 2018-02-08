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

// struct mib2_icmp_group {
//   unsigned int icmpInMsgs;
//   unsigned int icmpInErrors;
//   unsigned int icmpInDestUnreachs;
//   unsigned int icmpInTimeExcds;
//   unsigned int icmpInParamProbs;
//   unsigned int icmpInSrcQuenchs;
//   unsigned int icmpInRedirects;
//   unsigned int icmpInEchos;
//   unsigned int icmpInEchoReps;
//   unsigned int icmpInTimestamps;
//   unsigned int icmpInAddrMasks;
//   unsigned int icmpInAddrMaskReps;
//   unsigned int icmpOutMsgs;
//   unsigned int icmpOutErrors;
//   unsigned int icmpOutDestUnreachs;
//   unsigned int icmpOutTimeExcds;
//   unsigned int icmpOutParamProbs;
//   unsigned int icmpOutSrcQuenchs;
//   unsigned int icmpOutRedirects;
//   unsigned int icmpOutEchos;
//   unsigned int icmpOutEchoReps;
//   unsigned int icmpOutTimestamps;
//   unsigned int icmpOutTimestampReps;
//   unsigned int icmpOutAddrMasks;
//   unsigned int icmpOutAddrMaskReps;
// };

public class Mib2IcmpGroup {
    public final long icmpInMsgs;
    public final long icmpInErrors;
    public final long icmpInDestUnreachs;
    public final long icmpInTimeExcds;
    public final long icmpInParamProbs;
    public final long icmpInSrcQuenchs;
    public final long icmpInRedirects;
    public final long icmpInEchos;
    public final long icmpInEchoReps;
    public final long icmpInTimestamps;
    public final long icmpInAddrMasks;
    public final long icmpInAddrMaskReps;
    public final long icmpOutMsgs;
    public final long icmpOutErrors;
    public final long icmpOutDestUnreachs;
    public final long icmpOutTimeExcds;
    public final long icmpOutParamProbs;
    public final long icmpOutSrcQuenchs;
    public final long icmpOutRedirects;
    public final long icmpOutEchos;
    public final long icmpOutEchoReps;
    public final long icmpOutTimestamps;
    public final long icmpOutTimestampReps;
    public final long icmpOutAddrMasks;
    public final long icmpOutAddrMaskReps;

    public Mib2IcmpGroup(final ByteBuffer buffer) throws InvalidPacketException {
        this.icmpInMsgs = BufferUtils.uint32(buffer);
        this.icmpInErrors = BufferUtils.uint32(buffer);
        this.icmpInDestUnreachs = BufferUtils.uint32(buffer);
        this.icmpInTimeExcds = BufferUtils.uint32(buffer);
        this.icmpInParamProbs = BufferUtils.uint32(buffer);
        this.icmpInSrcQuenchs = BufferUtils.uint32(buffer);
        this.icmpInRedirects = BufferUtils.uint32(buffer);
        this.icmpInEchos = BufferUtils.uint32(buffer);
        this.icmpInEchoReps = BufferUtils.uint32(buffer);
        this.icmpInTimestamps = BufferUtils.uint32(buffer);
        this.icmpInAddrMasks = BufferUtils.uint32(buffer);
        this.icmpInAddrMaskReps = BufferUtils.uint32(buffer);
        this.icmpOutMsgs = BufferUtils.uint32(buffer);
        this.icmpOutErrors = BufferUtils.uint32(buffer);
        this.icmpOutDestUnreachs = BufferUtils.uint32(buffer);
        this.icmpOutTimeExcds = BufferUtils.uint32(buffer);
        this.icmpOutParamProbs = BufferUtils.uint32(buffer);
        this.icmpOutSrcQuenchs = BufferUtils.uint32(buffer);
        this.icmpOutRedirects = BufferUtils.uint32(buffer);
        this.icmpOutEchos = BufferUtils.uint32(buffer);
        this.icmpOutEchoReps = BufferUtils.uint32(buffer);
        this.icmpOutTimestamps = BufferUtils.uint32(buffer);
        this.icmpOutTimestampReps = BufferUtils.uint32(buffer);
        this.icmpOutAddrMasks = BufferUtils.uint32(buffer);
        this.icmpOutAddrMaskReps = BufferUtils.uint32(buffer);
    }
}
