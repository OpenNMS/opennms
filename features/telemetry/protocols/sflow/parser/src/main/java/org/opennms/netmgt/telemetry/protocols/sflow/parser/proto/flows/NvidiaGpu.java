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
import com.google.common.primitives.UnsignedLong;

// struct nvidia_gpu {
//   unsigned int device_count; /* see nvmlDeviceGetCount */
//   unsigned int processes;    /* see nvmlDeviceGetComputeRunningProcesses */
//   unsigned int gpu_time;     /* total milliseconds in which one or more
//                                 kernels was executing on GPU
//                                 sum across all devices */
//   unsigned int mem_time;     /* total milliseconds during which global device
//                                 memory was being read/written
//                                 sum across all devices */
//   unsigned hyper mem_total;  /* sum of framebuffer memory across devices
//                                 see nvmlDeviceGetMemoryInfo */
//   unsigned hyper mem_free;   /* sum of free framebuffer memory across devices
//                                 see nvmlDeviceGetMemoryInfo */
//   unsigned int ecc_errors;   /* sum of volatile ECC errors across devices
//                                 see nvmlDeviceGetTotalEccErrors */
//   unsigned int energy;       /* sum of millijoules across devices
//                                 see nvmlDeviceGetPowerUsage */
//   unsigned int temperature;  /* maximum temperature in degrees Celsius
//                                 across devices
//                                 see nvmlDeviceGetTemperature */
//   unsigned int fan_speed;    /* maximum fan speed in percent across devices
//                                 see nvmlDeviceGetFanSpeed */
// };

public class NvidiaGpu implements CounterData {
    public final long device_count;
    public final long processes;
    public final long gpu_time;
    public final long mem_time;
    public final UnsignedLong mem_total;
    public final UnsignedLong mem_free;
    public final long ecc_errors;
    public final long energy;
    public final long temperature;
    public final long fan_speed;

    public NvidiaGpu(final ByteBuffer buffer) throws InvalidPacketException {
        this.device_count = BufferUtils.uint32(buffer);
        this.processes = BufferUtils.uint32(buffer);
        this.gpu_time = BufferUtils.uint32(buffer);
        this.mem_time = BufferUtils.uint32(buffer);
        this.mem_total = BufferUtils.uint64(buffer);
        this.mem_free = BufferUtils.uint64(buffer);
        this.ecc_errors = BufferUtils.uint32(buffer);
        this.energy = BufferUtils.uint32(buffer);
        this.temperature = BufferUtils.uint32(buffer);
        this.fan_speed = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("device_count", this.device_count)
                .add("processes", this.processes)
                .add("gpu_time", this.gpu_time)
                .add("mem_time", this.mem_time)
                .add("mem_total", this.mem_total)
                .add("mem_free", this.mem_free)
                .add("ecc_errors", this.ecc_errors)
                .add("energy", this.energy)
                .add("temperature", this.temperature)
                .add("fan_speed", this.fan_speed)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("device_count", this.device_count);
        bsonWriter.writeInt64("processes", this.processes);
        bsonWriter.writeInt64("gpu_time", this.gpu_time);
        bsonWriter.writeInt64("mem_time", this.mem_time);
        bsonWriter.writeInt64("mem_total", this.mem_total.longValue());
        bsonWriter.writeInt64("mem_free", this.mem_free.longValue());
        bsonWriter.writeInt64("ecc_errors", this.ecc_errors);
        bsonWriter.writeInt64("energy", this.energy);
        bsonWriter.writeInt64("temperature", this.temperature);
        bsonWriter.writeInt64("fan_speed", this.fan_speed);
        bsonWriter.writeEndDocument();
    }

}
