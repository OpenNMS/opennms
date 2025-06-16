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

// struct jvm_statistics {
//   unsigned hyper heap_initial;    /* initial heap memory requested */
//   unsigned hyper heap_used;       /* current heap memory usage  */
//   unsigned hyper heap_committed;  /* heap memory currently committed */
//   unsigned hyper heap_max;        /* max heap space */
//   unsigned hyper non_heap_initial; /* initial non heap memory
//                                       requested */
//   unsigned hyper non_heap_used;   /* current non heap memory usage  */
//   unsigned hyper non_heap_committed; /* non heap memory currently
//                                         committed */
//   unsigned hyper non_heap_max;    /* max non-heap space */
//   unsigned int gc_count;          /* total number of collections that
//                                      have occurred */
//   unsigned int gc_time;           /* approximate accumulated collection
//                                      elapsed time in milliseconds */
//   unsigned int classes_loaded;    /* number of classes currently loaded
//                                      in vm */
//   unsigned int classes_total;     /* total number of classes loaded
//                                      since vm started */
//   unsigned int classes_unloaded;  /* total number of classe unloaded
//                                      since vm started */
//   unsigned int compilation_time;  /* total accumulated time spent in
//                                      compilation (in milliseconds) */
//   unsigned int thread_num_live;   /* current number of live threads */
//   unsigned int thread_num_daemon; /* current number of live daemon
//                                      threads */
//   unsigned int thread_num_started; /* total threads started since
//                                       vm started */
//   unsigned int fd_open_count;     /* number of open file descriptors */
//   unsigned int fd_max_count;      /* max number of file descriptors */
// };

public class JvmStatistics implements CounterData {
    public final UnsignedLong heap_initial;
    public final UnsignedLong heap_used;
    public final UnsignedLong heap_committed;
    public final UnsignedLong heap_max;
    public final UnsignedLong non_heap_initial;
    public final UnsignedLong non_heap_used;
    public final UnsignedLong non_heap_committed;
    public final UnsignedLong non_heap_max;
    public final long gc_count;
    public final long gc_time;
    public final long classes_loaded;
    public final long classes_total;
    public final long classes_unloaded;
    public final long compilation_time;
    public final long thread_num_live;
    public final long thread_num_daemon;
    public final long thread_num_started;
    public final long fd_open_count;
    public final long fd_max_count;

    public JvmStatistics(final ByteBuf buffer) throws InvalidPacketException {
        this.heap_initial = BufferUtils.uint64(buffer);
        this.heap_used = BufferUtils.uint64(buffer);
        this.heap_committed = BufferUtils.uint64(buffer);
        this.heap_max = BufferUtils.uint64(buffer);
        this.non_heap_initial = BufferUtils.uint64(buffer);
        this.non_heap_used = BufferUtils.uint64(buffer);
        this.non_heap_committed = BufferUtils.uint64(buffer);
        this.non_heap_max = BufferUtils.uint64(buffer);
        this.gc_count = BufferUtils.uint32(buffer);
        this.gc_time = BufferUtils.uint32(buffer);
        this.classes_loaded = BufferUtils.uint32(buffer);
        this.classes_total = BufferUtils.uint32(buffer);
        this.classes_unloaded = BufferUtils.uint32(buffer);
        this.compilation_time = BufferUtils.uint32(buffer);
        this.thread_num_live = BufferUtils.uint32(buffer);
        this.thread_num_daemon = BufferUtils.uint32(buffer);
        this.thread_num_started = BufferUtils.uint32(buffer);
        this.fd_open_count = BufferUtils.uint32(buffer);
        this.fd_max_count = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("heap_initial", this.heap_initial)
                .add("heap_used", this.heap_used)
                .add("heap_committed", this.heap_committed)
                .add("heap_max", this.heap_max)
                .add("non_heap_initial", this.non_heap_initial)
                .add("non_heap_used", this.non_heap_used)
                .add("non_heap_committed", this.non_heap_committed)
                .add("non_heap_max", this.non_heap_max)
                .add("gc_count", this.gc_count)
                .add("gc_time", this.gc_time)
                .add("classes_loaded", this.classes_loaded)
                .add("classes_total", this.classes_total)
                .add("classes_unloaded", this.classes_unloaded)
                .add("compilation_time", this.compilation_time)
                .add("thread_num_live", this.thread_num_live)
                .add("thread_num_daemon", this.thread_num_daemon)
                .add("thread_num_started", this.thread_num_started)
                .add("fd_open_count", this.fd_open_count)
                .add("fd_max_count", this.fd_max_count)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter, final SampleDatagramEnrichment enr) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeInt64("heap_initial", this.heap_initial.longValue());
        bsonWriter.writeInt64("heap_used", this.heap_used.longValue());
        bsonWriter.writeInt64("heap_committed", this.heap_committed.longValue());
        bsonWriter.writeInt64("heap_max", this.heap_max.longValue());
        bsonWriter.writeInt64("non_heap_initial", this.non_heap_initial.longValue());
        bsonWriter.writeInt64("non_heap_used", this.non_heap_used.longValue());
        bsonWriter.writeInt64("non_heap_committed", this.non_heap_committed.longValue());
        bsonWriter.writeInt64("non_heap_max", this.non_heap_max.longValue());
        bsonWriter.writeInt64("gc_count", this.gc_count);
        bsonWriter.writeInt64("gc_time", this.gc_time);
        bsonWriter.writeInt64("classes_loaded", this.classes_loaded);
        bsonWriter.writeInt64("classes_total", this.classes_total);
        bsonWriter.writeInt64("classes_unloaded", this.classes_unloaded);
        bsonWriter.writeInt64("compilation_time", this.compilation_time);
        bsonWriter.writeInt64("thread_num_live", this.thread_num_live);
        bsonWriter.writeInt64("thread_num_daemon", this.thread_num_daemon);
        bsonWriter.writeInt64("thread_num_started", this.thread_num_started);
        bsonWriter.writeInt64("fd_open_count", this.fd_open_count);
        bsonWriter.writeInt64("fd_max_count", this.fd_max_count);
        bsonWriter.writeEndDocument();
    }
}
