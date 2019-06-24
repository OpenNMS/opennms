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

// struct host_cpu {
//    float load_one;              /* 1 minute load avg., -1.0 = unknown */
//    float load_five;             /* 5 minute load avg., -1.0 = unknown */
//    float load_fifteen;          /* 15 minute load avg., -1.0 = unknown */
//    unsigned int proc_run;       /* total number of running processes */
//    unsigned int proc_total;     /* total number of processes */
//    unsigned int cpu_num;        /* number of CPUs */
//    unsigned int cpu_speed;      /* speed in MHz of CPU */
//    unsigned int uptime;         /* seconds since last reboot */
//    unsigned int cpu_user;       /* user time (ms) */
//    unsigned int cpu_nice;       /* nice time (ms) */
//    unsigned int cpu_system;     /* system time (ms) */
//    unsigned int cpu_idle;       /* idle time (ms) */
//    unsigned int cpu_wio;        /* time waiting for I/O to complete (ms) */
//    unsigned int cpu_intr;       /* time servicing interrupts (ms) */
//    unsigned int cpu_sintr;      /* time servicing soft interrupts (ms) */
//    unsigned int interrupts;     /* interrupt count */
//    unsigned int contexts;       /* context switch count */
// };

public class HostCpu implements CounterData {
    public final float load_one;
    public final float load_five;
    public final float load_fifteen;
    public final long proc_run;
    public final long proc_total;
    public final long cpu_num;
    public final long cpu_speed;
    public final long uptime;
    public final long cpu_user;
    public final long cpu_nice;
    public final long cpu_system;
    public final long cpu_idle;
    public final long cpu_wio;
    public final long cpu_intr;
    public final long cpu_sintr;
    public final long interrupts;
    public final long contexts;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("load_one", this.load_one)
                .add("load_five", this.load_five)
                .add("load_fifteen", this.load_fifteen)
                .add("proc_run", this.proc_run)
                .add("proc_total", this.proc_total)
                .add("cpu_num", this.cpu_num)
                .add("cpu_speed", this.cpu_speed)
                .add("uptime", this.uptime)
                .add("cpu_user", this.cpu_user)
                .add("cpu_nice", this.cpu_nice)
                .add("cpu_system", this.cpu_system)
                .add("cpu_idle", this.cpu_idle)
                .add("cpu_wio", this.cpu_wio)
                .add("cpu_intr", this.cpu_intr)
                .add("cpu_sintr", this.cpu_sintr)
                .add("interrupts", this.interrupts)
                .add("contexts", this.contexts)
                .toString();
    }

    public HostCpu(final ByteBuffer buffer) throws InvalidPacketException {
        this.load_one = BufferUtils.sfloat(buffer);
        this.load_five = BufferUtils.sfloat(buffer);
        this.load_fifteen = BufferUtils.sfloat(buffer);
        this.proc_run = BufferUtils.uint32(buffer);
        this.proc_total = BufferUtils.uint32(buffer);
        this.cpu_num = BufferUtils.uint32(buffer);
        this.cpu_speed = BufferUtils.uint32(buffer);
        this.uptime = BufferUtils.uint32(buffer);
        this.cpu_user = BufferUtils.uint32(buffer);
        this.cpu_nice = BufferUtils.uint32(buffer);
        this.cpu_system = BufferUtils.uint32(buffer);
        this.cpu_idle = BufferUtils.uint32(buffer);
        this.cpu_wio = BufferUtils.uint32(buffer);
        this.cpu_intr = BufferUtils.uint32(buffer);
        this.cpu_sintr = BufferUtils.uint32(buffer);
        this.interrupts = BufferUtils.uint32(buffer);
        this.contexts = BufferUtils.uint32(buffer);
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeDouble("load_one", this.load_one);
        bsonWriter.writeDouble("load_five", this.load_five);
        bsonWriter.writeDouble("load_fifteen", this.load_fifteen);
        bsonWriter.writeInt64("proc_run", this.proc_run);
        bsonWriter.writeInt64("proc_total", this.proc_total);
        bsonWriter.writeInt64("cpu_num", this.cpu_num);
        bsonWriter.writeInt64("cpu_speed", this.cpu_speed);
        bsonWriter.writeInt64("uptime", this.uptime);
        bsonWriter.writeInt64("cpu_user", this.cpu_user);
        bsonWriter.writeInt64("cpu_nice", this.cpu_nice);
        bsonWriter.writeInt64("cpu_system", this.cpu_system);
        bsonWriter.writeInt64("cpu_idle", this.cpu_idle);
        bsonWriter.writeInt64("cpu_wio", this.cpu_wio);
        bsonWriter.writeInt64("cpu_intr", this.cpu_intr);
        bsonWriter.writeInt64("cpu_sintr", this.cpu_sintr);
        bsonWriter.writeInt64("interrupts", this.interrupts);
        bsonWriter.writeInt64("contexts", this.contexts);
        bsonWriter.writeEndDocument();
    }
}
