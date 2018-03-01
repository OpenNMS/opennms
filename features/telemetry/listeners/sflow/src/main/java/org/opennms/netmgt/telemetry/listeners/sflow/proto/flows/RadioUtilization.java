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

// struct radio_utilization {
//    unsigned int elapsed_time;         /* elapsed time in ms */
//    unsigned int on_channel_time;      /* time in ms spent on channel */
//    unsigned int on_channel_busy_time; /* time in ms spent on channel
//                                          and busy */
// };

public class RadioUtilization implements CounterData {
    public final long elapsed_time;
    public final long on_channel_time;
    public final long on_channel_busy_time;

    public RadioUtilization(final ByteBuffer buffer) throws InvalidPacketException {
        this.elapsed_time = BufferUtils.uint32(buffer);
        this.on_channel_time = BufferUtils.uint32(buffer);
        this.on_channel_busy_time = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("elapsed_time", this.elapsed_time)
                .add("on_channel_time", this.on_channel_time)
                .add("on_channel_busy_time", this.on_channel_busy_time)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("elapsed_time", this.elapsed_time);
        bsonWriter.writeInt64("on_channel_time", this.on_channel_time);
        bsonWriter.writeInt64("on_channel_busy_time", this.on_channel_busy_time);
        bsonWriter.writeEndDocument();
    }
}
