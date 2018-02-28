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

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

import com.google.common.primitives.UnsignedLong;

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

    public JvmStatistics(final ByteBuffer buffer) throws InvalidPacketException {
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
}
