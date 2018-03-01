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

// struct app_workers {
//   unsigned int workers_active; /* number of active workers */
//   unsigned int workers_idle;   /* number of idle workers */
//   unsigned int workers_max;    /* max. number of workers */
//   unsigned int req_delayed;    /* number of times processing of a client request
//                                   was delayed because of lack of resources */
//   unsigned int req_dropped;    /* number of times a client request was dropped
//                                   because of a lack of resources */
// };

public class AppWorkers implements CounterData {
    public final long workers_active;
    public final long workers_idle;
    public final long workers_max;
    public final long req_delayed;
    public final long req_dropped;

    public AppWorkers(final ByteBuffer buffer) throws InvalidPacketException {
        this.workers_active = BufferUtils.uint32(buffer);
        this.workers_idle = BufferUtils.uint32(buffer);
        this.workers_max = BufferUtils.uint32(buffer);
        this.req_delayed = BufferUtils.uint32(buffer);
        this.req_dropped = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("workers_active", this.workers_active)
                .add("workers_idle", this.workers_idle)
                .add("workers_max", this.workers_max)
                .add("req_delayed", this.req_delayed)
                .add("req_dropped", this.req_dropped)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("workers_active", this.workers_active);
        bsonWriter.writeInt64("workers_idle", this.workers_idle);
        bsonWriter.writeInt64("workers_max", this.workers_max);
        bsonWriter.writeInt64("req_delayed", this.req_delayed);
        bsonWriter.writeInt64("req_dropped", this.req_dropped);
        bsonWriter.writeEndDocument();
    }
}
