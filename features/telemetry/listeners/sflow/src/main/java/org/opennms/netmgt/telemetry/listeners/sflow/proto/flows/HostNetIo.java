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

// struct host_net_io {
//    unsigned hyper bytes_in;  /* total bytes in */
//    unsigned int pkts_in;     /* total packets in */
//    unsigned int errs_in;     /* total errors in */
//    unsigned int drops_in;    /* total drops in */
//    unsigned hyper bytes_out; /* total bytes out */
//    unsigned int packets_out; /* total packets out */
//    unsigned int errs_out;    /* total errors out */
//    unsigned int drops_out;   /* total drops out */
// };

public class HostNetIo implements CounterData {
    public final UnsignedLong bytes_in;
    public final long pkts_in;
    public final long errs_in;
    public final long drops_in;
    public final UnsignedLong bytes_out;
    public final long packets_out;
    public final long errs_out;
    public final long drops_out;

    public HostNetIo(final ByteBuffer buffer) throws InvalidPacketException {
        this.bytes_in = BufferUtils.uint64(buffer);
        this.pkts_in = BufferUtils.uint32(buffer);
        this.errs_in = BufferUtils.uint32(buffer);
        this.drops_in = BufferUtils.uint32(buffer);
        this.bytes_out = BufferUtils.uint64(buffer);
        this.packets_out = BufferUtils.uint32(buffer);
        this.errs_out = BufferUtils.uint32(buffer);
        this.drops_out = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("bytes_in", this.bytes_in)
                .add("pkts_in", this.pkts_in)
                .add("errs_in", this.errs_in)
                .add("drops_in", this.drops_in)
                .add("bytes_out", this.bytes_out)
                .add("packets_out", this.packets_out)
                .add("errs_out", this.errs_out)
                .add("drops_out", this.drops_out)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("bytes_in", this.bytes_in.longValue());
        bsonWriter.writeInt64("pkts_in", this.pkts_in);
        bsonWriter.writeInt64("errs_in", this.errs_in);
        bsonWriter.writeInt64("drops_in", this.drops_in);
        bsonWriter.writeInt64("bytes_out", this.bytes_out.longValue());
        bsonWriter.writeInt64("packets_out", this.packets_out);
        bsonWriter.writeInt64("errs_out", this.errs_out);
        bsonWriter.writeInt64("drops_out", this.drops_out);
        bsonWriter.writeEndDocument();
    }
}
