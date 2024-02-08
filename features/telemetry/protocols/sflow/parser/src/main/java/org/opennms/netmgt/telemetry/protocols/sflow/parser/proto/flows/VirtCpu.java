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

import io.netty.buffer.ByteBuf;

// struct virt_cpu {
//    unsigned int state;         /* virtDomainState */
//    unsigned int cpuTime;       /* the CPU time used (ms) */
//    unsigned int nrVirtCpu;     /* number of virtual CPUs for the domain */
// };

public class VirtCpu implements CounterData {
    public final long state;
    public final long cpuTime;
    public final long nrVirtCpu;

    public VirtCpu(final ByteBuf buffer) throws InvalidPacketException {
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

    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("state", this.state);
        bsonWriter.writeInt64("cpuTime", this.cpuTime);
        bsonWriter.writeInt64("nrVirtCpu", this.nrVirtCpu);
        bsonWriter.writeEndDocument();
    }
}
