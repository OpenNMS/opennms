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

// struct mib2_ip_group {
//   unsigned int ipForwarding;
//   unsigned int ipDefaultTTL;
//   unsigned int ipInReceives;
//   unsigned int ipInHdrErrors;
//   unsigned int ipInAddrErrors;
//   unsigned int ipForwDatagrams;
//   unsigned int ipInUnknownProtos;
//   unsigned int ipInDiscards;
//   unsigned int ipInDelivers;
//   unsigned int ipOutRequests;
//   unsigned int ipOutDiscards;
//   unsigned int ipOutNoRoutes;
//   unsigned int ipReasmTimeout;
//   unsigned int ipReasmReqds;
//   unsigned int ipReasmOKs;
//   unsigned int ipReasmFails;
//   unsigned int ipFragOKs;
//   unsigned int ipFragFails;
//   unsigned int ipFragCreates;
// };

public class Mib2IpGroup implements CounterData {
    public final long ipForwarding;
    public final long ipDefaultTTL;
    public final long ipInReceives;
    public final long ipInHdrErrors;
    public final long ipInAddrErrors;
    public final long ipForwDatagrams;
    public final long ipInUnknownProtos;
    public final long ipInDiscards;
    public final long ipInDelivers;
    public final long ipOutRequests;
    public final long ipOutDiscards;
    public final long ipOutNoRoutes;
    public final long ipReasmTimeout;
    public final long ipReasmReqds;
    public final long ipReasmOKs;
    public final long ipReasmFails;
    public final long ipFragOKs;
    public final long ipFragFails;
    public final long ipFragCreates;

    public Mib2IpGroup(final ByteBuffer buffer) throws InvalidPacketException {
        this.ipForwarding = BufferUtils.uint32(buffer);
        this.ipDefaultTTL = BufferUtils.uint32(buffer);
        this.ipInReceives = BufferUtils.uint32(buffer);
        this.ipInHdrErrors = BufferUtils.uint32(buffer);
        this.ipInAddrErrors = BufferUtils.uint32(buffer);
        this.ipForwDatagrams = BufferUtils.uint32(buffer);
        this.ipInUnknownProtos = BufferUtils.uint32(buffer);
        this.ipInDiscards = BufferUtils.uint32(buffer);
        this.ipInDelivers = BufferUtils.uint32(buffer);
        this.ipOutRequests = BufferUtils.uint32(buffer);
        this.ipOutDiscards = BufferUtils.uint32(buffer);
        this.ipOutNoRoutes = BufferUtils.uint32(buffer);
        this.ipReasmTimeout = BufferUtils.uint32(buffer);
        this.ipReasmReqds = BufferUtils.uint32(buffer);
        this.ipReasmOKs = BufferUtils.uint32(buffer);
        this.ipReasmFails = BufferUtils.uint32(buffer);
        this.ipFragOKs = BufferUtils.uint32(buffer);
        this.ipFragFails = BufferUtils.uint32(buffer);
        this.ipFragCreates = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ipForwarding", this.ipForwarding)
                .add("ipDefaultTTL", this.ipDefaultTTL)
                .add("ipInReceives", this.ipInReceives)
                .add("ipInHdrErrors", this.ipInHdrErrors)
                .add("ipInAddrErrors", this.ipInAddrErrors)
                .add("ipForwDatagrams", this.ipForwDatagrams)
                .add("ipInUnknownProtos", this.ipInUnknownProtos)
                .add("ipInDiscards", this.ipInDiscards)
                .add("ipInDelivers", this.ipInDelivers)
                .add("ipOutRequests", this.ipOutRequests)
                .add("ipOutDiscards", this.ipOutDiscards)
                .add("ipOutNoRoutes", this.ipOutNoRoutes)
                .add("ipReasmTimeout", this.ipReasmTimeout)
                .add("ipReasmReqds", this.ipReasmReqds)
                .add("ipReasmOKs", this.ipReasmOKs)
                .add("ipReasmFails", this.ipReasmFails)
                .add("ipFragOKs", this.ipFragOKs)
                .add("ipFragFails", this.ipFragFails)
                .add("ipFragCreates", this.ipFragCreates)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("ipForwarding", this.ipForwarding);
        bsonWriter.writeInt64("ipDefaultTTL", this.ipDefaultTTL);
        bsonWriter.writeInt64("ipInReceives", this.ipInReceives);
        bsonWriter.writeInt64("ipInHdrErrors", this.ipInHdrErrors);
        bsonWriter.writeInt64("ipInAddrErrors", this.ipInAddrErrors);
        bsonWriter.writeInt64("ipForwDatagrams", this.ipForwDatagrams);
        bsonWriter.writeInt64("ipInUnknownProtos", this.ipInUnknownProtos);
        bsonWriter.writeInt64("ipInDiscards", this.ipInDiscards);
        bsonWriter.writeInt64("ipInDelivers", this.ipInDelivers);
        bsonWriter.writeInt64("ipOutRequests", this.ipOutRequests);
        bsonWriter.writeInt64("ipOutDiscards", this.ipOutDiscards);
        bsonWriter.writeInt64("ipOutNoRoutes", this.ipOutNoRoutes);
        bsonWriter.writeInt64("ipReasmTimeout", this.ipReasmTimeout);
        bsonWriter.writeInt64("ipReasmReqds", this.ipReasmReqds);
        bsonWriter.writeInt64("ipReasmOKs", this.ipReasmOKs);
        bsonWriter.writeInt64("ipReasmFails", this.ipReasmFails);
        bsonWriter.writeInt64("ipFragOKs", this.ipFragOKs);
        bsonWriter.writeInt64("ipFragFails", this.ipFragFails);
        bsonWriter.writeInt64("ipFragCreates", this.ipFragCreates);
        bsonWriter.writeEndDocument();
    }

}
