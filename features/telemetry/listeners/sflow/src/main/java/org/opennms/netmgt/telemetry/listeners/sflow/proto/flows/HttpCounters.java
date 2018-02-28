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

import com.google.common.base.MoreObjects;

// struct http_counters {
//   unsigned int method_option_count;
//   unsigned int method_get_count;
//   unsigned int method_head_count;
//   unsigned int method_post_count;
//   unsigned int method_put_count;
//   unsigned int method_delete_count;
//   unsigned int method_trace_count;
//   unsigned int method_connect_count;
//   unsigned int method_other_count;
//   unsigned int status_1XX_count;
//   unsigned int status_2XX_count;
//   unsigned int status_3XX_count;
//   unsigned int status_4XX_count;
//   unsigned int status_5XX_count;
//   unsigned int status_other_count;
// };

public class HttpCounters implements CounterData {
    public final long method_option_count;
    public final long method_get_count;
    public final long method_head_count;
    public final long method_post_count;
    public final long method_put_count;
    public final long method_delete_count;
    public final long method_trace_count;
    public final long method_connect_count;
    public final long method_other_count;
    public final long status_1XX_count;
    public final long status_2XX_count;
    public final long status_3XX_count;
    public final long status_4XX_count;
    public final long status_5XX_count;
    public final long status_other_count;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("method_option_count", method_option_count)
                .add("method_get_count", method_get_count)
                .add("method_head_count", method_head_count)
                .add("method_post_count", method_post_count)
                .add("method_put_count", method_put_count)
                .add("method_delete_count", method_delete_count)
                .add("method_trace_count", method_trace_count)
                .add("method_connect_count", method_connect_count)
                .add("method_other_count", method_other_count)
                .add("status_1XX_count", status_1XX_count)
                .add("status_2XX_count", status_2XX_count)
                .add("status_3XX_count", status_3XX_count)
                .add("status_4XX_count", status_4XX_count)
                .add("status_5XX_count", status_5XX_count)
                .add("status_other_count", status_other_count)
                .toString();
    }

    public HttpCounters(final ByteBuffer buffer) throws InvalidPacketException {
        this.method_option_count = BufferUtils.uint32(buffer);
        this.method_get_count = BufferUtils.uint32(buffer);
        this.method_head_count = BufferUtils.uint32(buffer);
        this.method_post_count = BufferUtils.uint32(buffer);
        this.method_put_count = BufferUtils.uint32(buffer);
        this.method_delete_count = BufferUtils.uint32(buffer);
        this.method_trace_count = BufferUtils.uint32(buffer);
        this.method_connect_count = BufferUtils.uint32(buffer);
        this.method_other_count = BufferUtils.uint32(buffer);
        this.status_1XX_count = BufferUtils.uint32(buffer);
        this.status_2XX_count = BufferUtils.uint32(buffer);
        this.status_3XX_count = BufferUtils.uint32(buffer);
        this.status_4XX_count = BufferUtils.uint32(buffer);
        this.status_5XX_count = BufferUtils.uint32(buffer);
        this.status_other_count = BufferUtils.uint32(buffer);
    }
}
