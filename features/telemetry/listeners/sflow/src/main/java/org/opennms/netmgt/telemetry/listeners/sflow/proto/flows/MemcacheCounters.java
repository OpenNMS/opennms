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

// struct memcache_counters {
//   unsigned int cmd_set;
//   unsigned int cmd_touch;
//   unsigned int cmd_flush;
//   unsigned int get_hits;
//   unsigned int get_misses;
//   unsigned int delete_hits;
//   unsigned int delete_misses;
//   unsigned int incr_hits;
//   unsigned int incr_misses;
//   unsigned int decr_hits;
//   unsigned int decr_misses;
//   unsigned int cas_hits;
//   unsigned int cas_misses;
//   unsigned int cas_badval;
//   unsigned int auth_cmds;
//   unsigned int auth_errors;
//   unsigned int threads;
//   unsigned int conn_yields;
//   unsigned int listen_disabled_num;
//   unsigned int curr_connections;
//   unsigned int rejected_connections;
//   unsigned int total_connections;
//   unsigned int connection_structures;
//   unsigned int evictions;
//   unsigned int reclaimed;
//   unsigned int curr_items;
//   unsigned int total_items;
//   unsigned hyper bytes_read;
//   unsigned hyper bytes_written;
//   unsigned hyper bytes;
//   unsigned hyper limit_maxbytes;
// };

public class MemcacheCounters implements CounterData {
    public final long cmd_set;
    public final long cmd_touch;
    public final long cmd_flush;
    public final long get_hits;
    public final long get_misses;
    public final long delete_hits;
    public final long delete_misses;
    public final long incr_hits;
    public final long incr_misses;
    public final long decr_hits;
    public final long decr_misses;
    public final long cas_hits;
    public final long cas_misses;
    public final long cas_badval;
    public final long auth_cmds;
    public final long auth_errors;
    public final long threads;
    public final long conn_yields;
    public final long listen_disabled_num;
    public final long curr_connections;
    public final long rejected_connections;
    public final long total_connections;
    public final long connection_structures;
    public final long evictions;
    public final long reclaimed;
    public final long curr_items;
    public final long total_items;
    public final UnsignedLong bytes_read;
    public final UnsignedLong bytes_written;
    public final UnsignedLong bytes;
    public final UnsignedLong limit_maxbytes;

    public MemcacheCounters(final ByteBuffer buffer) throws InvalidPacketException {
        this.cmd_set = BufferUtils.uint32(buffer);
        this.cmd_touch = BufferUtils.uint32(buffer);
        this.cmd_flush = BufferUtils.uint32(buffer);
        this.get_hits = BufferUtils.uint32(buffer);
        this.get_misses = BufferUtils.uint32(buffer);
        this.delete_hits = BufferUtils.uint32(buffer);
        this.delete_misses = BufferUtils.uint32(buffer);
        this.incr_hits = BufferUtils.uint32(buffer);
        this.incr_misses = BufferUtils.uint32(buffer);
        this.decr_hits = BufferUtils.uint32(buffer);
        this.decr_misses = BufferUtils.uint32(buffer);
        this.cas_hits = BufferUtils.uint32(buffer);
        this.cas_misses = BufferUtils.uint32(buffer);
        this.cas_badval = BufferUtils.uint32(buffer);
        this.auth_cmds = BufferUtils.uint32(buffer);
        this.auth_errors = BufferUtils.uint32(buffer);
        this.threads = BufferUtils.uint32(buffer);
        this.conn_yields = BufferUtils.uint32(buffer);
        this.listen_disabled_num = BufferUtils.uint32(buffer);
        this.curr_connections = BufferUtils.uint32(buffer);
        this.rejected_connections = BufferUtils.uint32(buffer);
        this.total_connections = BufferUtils.uint32(buffer);
        this.connection_structures = BufferUtils.uint32(buffer);
        this.evictions = BufferUtils.uint32(buffer);
        this.reclaimed = BufferUtils.uint32(buffer);
        this.curr_items = BufferUtils.uint32(buffer);
        this.total_items = BufferUtils.uint32(buffer);
        this.bytes_read = BufferUtils.uint64(buffer);
        this.bytes_written = BufferUtils.uint64(buffer);
        this.bytes = BufferUtils.uint64(buffer);
        this.limit_maxbytes = BufferUtils.uint64(buffer);
    }
}
