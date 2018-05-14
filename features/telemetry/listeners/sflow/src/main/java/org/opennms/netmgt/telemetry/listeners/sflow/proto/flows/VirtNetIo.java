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

// struct virt_net_io {
//    unsigned hyper rx_bytes;  /* total bytes received */
//    unsigned int rx_packets;  /* total packets received */
//    unsigned int rx_errs;     /* total receive errors */
//    unsigned int rx_drop;     /* total receive drops */
//    unsigned hyper tx_bytes;  /* total bytes transmitted */
//    unsigned int tx_packets;  /* total packets transmitted */
//    unsigned int tx_errs;     /* total transmit errors */
//    unsigned int tx_drop;     /* total transmit drops */
// };

public class VirtNetIo implements CounterData {
    public final UnsignedLong rx_bytes;
    public final long rx_packets;
    public final long rx_errs;
    public final long rx_drop;
    public final UnsignedLong tx_bytes;
    public final long tx_packets;
    public final long tx_errs;
    public final long tx_drop;

    public VirtNetIo(final ByteBuffer buffer) throws InvalidPacketException {
        this.rx_bytes = BufferUtils.uint64(buffer);
        this.rx_packets = BufferUtils.uint32(buffer);
        this.rx_errs = BufferUtils.uint32(buffer);
        this.rx_drop = BufferUtils.uint32(buffer);
        this.tx_bytes = BufferUtils.uint64(buffer);
        this.tx_packets = BufferUtils.uint32(buffer);
        this.tx_errs = BufferUtils.uint32(buffer);
        this.tx_drop = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("rx_bytes", this.rx_bytes)
                .add("rx_packets", this.rx_packets)
                .add("rx_errs", this.rx_errs)
                .add("rx_drop", this.rx_drop)
                .add("tx_bytes", this.tx_bytes)
                .add("tx_packets", this.tx_packets)
                .add("tx_errs", this.tx_errs)
                .add("tx_drop", this.tx_drop)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("rx_bytes", this.rx_bytes.longValue());
        bsonWriter.writeInt64("rx_packets", this.rx_packets);
        bsonWriter.writeInt64("rx_errs", this.rx_errs);
        bsonWriter.writeInt64("rx_drop", this.rx_drop);
        bsonWriter.writeInt64("tx_bytes", this.tx_bytes.longValue());
        bsonWriter.writeInt64("tx_packets", this.tx_packets);
        bsonWriter.writeInt64("tx_errs", this.tx_errs);
        bsonWriter.writeInt64("tx_drop", this.tx_drop);
        bsonWriter.writeEndDocument();
    }

}
