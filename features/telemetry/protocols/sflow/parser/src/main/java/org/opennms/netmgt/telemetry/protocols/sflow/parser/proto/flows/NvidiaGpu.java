/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.SampleDatagramEnrichment;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;

import io.netty.buffer.ByteBuf;

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

    public NvidiaGpu(final ByteBuf buffer) throws InvalidPacketException {
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
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
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
