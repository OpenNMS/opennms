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

public class Mib2IcmpGroup implements CounterData {
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("icmpInMsgs", this.icmpInMsgs)
                .add("icmpInErrors", this.icmpInErrors)
                .add("icmpInDestUnreachs", this.icmpInDestUnreachs)
                .add("icmpInTimeExcds", this.icmpInTimeExcds)
                .add("icmpInParamProbs", this.icmpInParamProbs)
                .add("icmpInSrcQuenchs", this.icmpInSrcQuenchs)
                .add("icmpInRedirects", this.icmpInRedirects)
                .add("icmpInEchos", this.icmpInEchos)
                .add("icmpInEchoReps", this.icmpInEchoReps)
                .add("icmpInTimestamps", this.icmpInTimestamps)
                .add("icmpInAddrMasks", this.icmpInAddrMasks)
                .add("icmpInAddrMaskReps", this.icmpInAddrMaskReps)
                .add("icmpOutMsgs", this.icmpOutMsgs)
                .add("icmpOutErrors", this.icmpOutErrors)
                .add("icmpOutDestUnreachs", this.icmpOutDestUnreachs)
                .add("icmpOutTimeExcds", this.icmpOutTimeExcds)
                .add("icmpOutParamProbs", this.icmpOutParamProbs)
                .add("icmpOutSrcQuenchs", this.icmpOutSrcQuenchs)
                .add("icmpOutRedirects", this.icmpOutRedirects)
                .add("icmpOutEchos", this.icmpOutEchos)
                .add("icmpOutEchoReps", this.icmpOutEchoReps)
                .add("icmpOutTimestamps", this.icmpOutTimestamps)
                .add("icmpOutTimestampReps", this.icmpOutTimestampReps)
                .add("icmpOutAddrMasks", this.icmpOutAddrMasks)
                .add("icmpOutAddrMaskReps", this.icmpOutAddrMaskReps)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("icmpInMsgs", this.icmpInMsgs);
        bsonWriter.writeInt64("icmpInErrors", this.icmpInErrors);
        bsonWriter.writeInt64("icmpInDestUnreachs", this.icmpInDestUnreachs);
        bsonWriter.writeInt64("icmpInTimeExcds", this.icmpInTimeExcds);
        bsonWriter.writeInt64("icmpInParamProbs", this.icmpInParamProbs);
        bsonWriter.writeInt64("icmpInSrcQuenchs", this.icmpInSrcQuenchs);
        bsonWriter.writeInt64("icmpInRedirects", this.icmpInRedirects);
        bsonWriter.writeInt64("icmpInEchos", this.icmpInEchos);
        bsonWriter.writeInt64("icmpInEchoReps", this.icmpInEchoReps);
        bsonWriter.writeInt64("icmpInTimestamps", this.icmpInTimestamps);
        bsonWriter.writeInt64("icmpInAddrMasks", this.icmpInAddrMasks);
        bsonWriter.writeInt64("icmpInAddrMaskReps", this.icmpInAddrMaskReps);
        bsonWriter.writeInt64("icmpOutMsgs", this.icmpOutMsgs);
        bsonWriter.writeInt64("icmpOutErrors", this.icmpOutErrors);
        bsonWriter.writeInt64("icmpOutDestUnreachs", this.icmpOutDestUnreachs);
        bsonWriter.writeInt64("icmpOutTimeExcds", this.icmpOutTimeExcds);
        bsonWriter.writeInt64("icmpOutParamProbs", this.icmpOutParamProbs);
        bsonWriter.writeInt64("icmpOutSrcQuenchs", this.icmpOutSrcQuenchs);
        bsonWriter.writeInt64("icmpOutRedirects", this.icmpOutRedirects);
        bsonWriter.writeInt64("icmpOutEchos", this.icmpOutEchos);
        bsonWriter.writeInt64("icmpOutEchoReps", this.icmpOutEchoReps);
        bsonWriter.writeInt64("icmpOutTimestamps", this.icmpOutTimestamps);
        bsonWriter.writeInt64("icmpOutTimestampReps", this.icmpOutTimestampReps);
        bsonWriter.writeInt64("icmpOutAddrMasks", this.icmpOutAddrMasks);
        bsonWriter.writeInt64("icmpOutAddrMaskReps", this.icmpOutAddrMaskReps);
        bsonWriter.writeEndDocument();
    }
}
