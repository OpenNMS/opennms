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

// struct mib2_tcp_group {
//   unsigned int tcpRtoAlgorithm;
//   unsigned int tcpRtoMin;
//   unsigned int tcpRtoMax;
//   unsigned int tcpMaxConn;
//   unsigned int tcpActiveOpens;
//   unsigned int tcpPassiveOpens;
//   unsigned int tcpAttemptFails;
//   unsigned int tcpEstabResets;
//   unsigned int tcpCurrEstab;
//   unsigned int tcpInSegs;
//   unsigned int tcpOutSegs;
//   unsigned int tcpRetransSegs;
//   unsigned int tcpInErrs;
//   unsigned int tcpOutRsts;
//   unsigned int tcpInCsumErrs;
// };

public class Mib2TcpGroup implements CounterData {
    public final long tcpRtoAlgorithm;
    public final long tcpRtoMin;
    public final long tcpRtoMax;
    public final long tcpMaxConn;
    public final long tcpActiveOpens;
    public final long tcpPassiveOpens;
    public final long tcpAttemptFails;
    public final long tcpEstabResets;
    public final long tcpCurrEstab;
    public final long tcpInSegs;
    public final long tcpOutSegs;
    public final long tcpRetransSegs;
    public final long tcpInErrs;
    public final long tcpOutRsts;
    public final long tcpInCsumErrs;

    public Mib2TcpGroup(final ByteBuffer buffer) throws InvalidPacketException {
        this.tcpRtoAlgorithm = BufferUtils.uint32(buffer);
        this.tcpRtoMin = BufferUtils.uint32(buffer);
        this.tcpRtoMax = BufferUtils.uint32(buffer);
        this.tcpMaxConn = BufferUtils.uint32(buffer);
        this.tcpActiveOpens = BufferUtils.uint32(buffer);
        this.tcpPassiveOpens = BufferUtils.uint32(buffer);
        this.tcpAttemptFails = BufferUtils.uint32(buffer);
        this.tcpEstabResets = BufferUtils.uint32(buffer);
        this.tcpCurrEstab = BufferUtils.uint32(buffer);
        this.tcpInSegs = BufferUtils.uint32(buffer);
        this.tcpOutSegs = BufferUtils.uint32(buffer);
        this.tcpRetransSegs = BufferUtils.uint32(buffer);
        this.tcpInErrs = BufferUtils.uint32(buffer);
        this.tcpOutRsts = BufferUtils.uint32(buffer);
        this.tcpInCsumErrs = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("tcpRtoAlgorithm", this.tcpRtoAlgorithm)
                .add("tcpRtoMin", this.tcpRtoMin)
                .add("tcpRtoMax", this.tcpRtoMax)
                .add("tcpMaxConn", this.tcpMaxConn)
                .add("tcpActiveOpens", this.tcpActiveOpens)
                .add("tcpPassiveOpens", this.tcpPassiveOpens)
                .add("tcpAttemptFails", this.tcpAttemptFails)
                .add("tcpEstabResets", this.tcpEstabResets)
                .add("tcpCurrEstab", this.tcpCurrEstab)
                .add("tcpInSegs", this.tcpInSegs)
                .add("tcpOutSegs", this.tcpOutSegs)
                .add("tcpRetransSegs", this.tcpRetransSegs)
                .add("tcpInErrs", this.tcpInErrs)
                .add("tcpOutRsts", this.tcpOutRsts)
                .add("tcpInCsumErrs", this.tcpInCsumErrs)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("tcpRtoAlgorithm", this.tcpRtoAlgorithm);
        bsonWriter.writeInt64("tcpRtoMin", this.tcpRtoMin);
        bsonWriter.writeInt64("tcpRtoMax", this.tcpRtoMax);
        bsonWriter.writeInt64("tcpMaxConn", this.tcpMaxConn);
        bsonWriter.writeInt64("tcpActiveOpens", this.tcpActiveOpens);
        bsonWriter.writeInt64("tcpPassiveOpens", this.tcpPassiveOpens);
        bsonWriter.writeInt64("tcpAttemptFails", this.tcpAttemptFails);
        bsonWriter.writeInt64("tcpEstabResets", this.tcpEstabResets);
        bsonWriter.writeInt64("tcpCurrEstab", this.tcpCurrEstab);
        bsonWriter.writeInt64("tcpInSegs", this.tcpInSegs);
        bsonWriter.writeInt64("tcpOutSegs", this.tcpOutSegs);
        bsonWriter.writeInt64("tcpRetransSegs", this.tcpRetransSegs);
        bsonWriter.writeInt64("tcpInErrs", this.tcpInErrs);
        bsonWriter.writeInt64("tcpOutRsts", this.tcpOutRsts);
        bsonWriter.writeInt64("tcpInCsumErrs", this.tcpInCsumErrs);
        bsonWriter.writeEndDocument();
    }

}
