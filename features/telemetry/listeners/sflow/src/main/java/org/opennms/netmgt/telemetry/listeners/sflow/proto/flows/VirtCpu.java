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

// struct virt_cpu {
//    unsigned int state;         /* virtDomainState */
//    unsigned int cpuTime;       /* the CPU time used (ms) */
//    unsigned int nrVirtCpu;     /* number of virtual CPUs for the domain */
// };

public class VirtCpu implements CounterData {
    public final long state;
    public final long cpuTime;
    public final long nrVirtCpu;

    public VirtCpu(final ByteBuffer buffer) throws InvalidPacketException {
        this.state = BufferUtils.uint32(buffer);
        this.cpuTime = BufferUtils.uint32(buffer);
        this.nrVirtCpu = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("state", this.state)
                .add("cpuTime", this.cpuTime)
                .add("nrVirtCpu", this.nrVirtCpu)
                .toString();
    }

    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("state", this.state);
        bsonWriter.writeInt64("cpuTime", this.cpuTime);
        bsonWriter.writeInt64("nrVirtCpu", this.nrVirtCpu);
        bsonWriter.writeEndDocument();
    }
}
