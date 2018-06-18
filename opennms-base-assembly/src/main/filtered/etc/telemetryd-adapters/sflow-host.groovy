/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.collection.support.builder.NodeLevelResource

import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.get
import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.getDouble
import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.getInt64


NodeLevelResource nodeLevelResource = new NodeLevelResource(agent.getNodeId())

get(msg, "counters", "0:2003").ifPresent { doc ->
    builder.withGauge(nodeLevelResource, "host-cpu", "load_avg_1min", getDouble(doc, "load_one").get())
    builder.withGauge(nodeLevelResource, "host-cpu", "load_avg_5min", getDouble(doc, "load_five").get())
    builder.withGauge(nodeLevelResource, "host-cpu", "load_avg_15min", getDouble(doc, "load_fifteen").get())
}

get(msg, "counters", "0:2004").ifPresent { doc ->
    builder.withGauge(nodeLevelResource, "host-memory", "mem_total", getInt64(doc, "mem_total").get())
    builder.withGauge(nodeLevelResource, "host-memory", "mem_free", getInt64(doc, "mem_free").get())
    builder.withGauge(nodeLevelResource, "host-memory", "mem_shared", getInt64(doc, "mem_shared").get())
    builder.withGauge(nodeLevelResource, "host-memory", "mem_buffers", getInt64(doc, "mem_buffers").get())
    builder.withGauge(nodeLevelResource, "host-memory", "mem_cached", getInt64(doc, "mem_cached").get())
}
