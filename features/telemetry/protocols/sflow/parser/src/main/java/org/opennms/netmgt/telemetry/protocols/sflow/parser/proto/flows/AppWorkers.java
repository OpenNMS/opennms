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

    public AppWorkers(final ByteBuf buffer) throws InvalidPacketException {
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
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("workers_active", this.workers_active);
        bsonWriter.writeInt64("workers_idle", this.workers_idle);
        bsonWriter.writeInt64("workers_max", this.workers_max);
        bsonWriter.writeInt64("req_delayed", this.req_delayed);
        bsonWriter.writeInt64("req_dropped", this.req_dropped);
        bsonWriter.writeEndDocument();
    }
}
