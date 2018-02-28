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

// struct hw_tables {
//   unsigned int host_entries;
//   unsigned int host_entries_max;
//   unsigned int ipv4_entries;
//   unsigned int ipv4_entries_max;
//   unsigned int ipv6_entries;
//   unsigned int ipv6_entries_max;
//   unsigned int ipv4_ipv6_entries;
//   unsigned int ipv6_ipv6_entries_max;
//   unsigned int long_ipv6_entries;
//   unsigned int long_ipv6_entries_max;
//   unsigned int total_routes;
//   unsigned int total_routes_max;
//   unsigned int ecmp_nexthops;
//   unsigned int ecmp_nexthops_max;
//   unsigned int mac_entries;
//   unsigned int mac_entries_max;
//   unsigned int ipv4_neighbors;
//   unsigned int ipv6_neighbors;
//   unsigned int ipv4_routes;
//   unsigned int ipv6_routes;
//   unsigned int acl_ingress_entries;
//   unsigned int acl_ingress_entries_max;
//   unsigned int acl_ingress_counters;
//   unsigned int acl_ingress_counters_max;
//   unsigned int acl_ingress_meters;
//   unsigned int acl_ingress_meters_max;
//   unsigned int acl_ingress_slices;
//   unsigned int acl_ingress_slices_max;
//   unsigned int acl_egress_entries;
//   unsigned int acl_egress_entries_max;
//   unsigned int acl_egress_counters;
//   unsigned int acl_egress_counters_max;
//   unsigned int acl_egress_meters;
//   unsigned int acl_egress_meters_max;
//   unsigned int acl_egress_slices;
//   unsigned int acl_egress_slices_max;
// };

public class HwTables implements CounterData {
    public final long host_entries;
    public final long host_entries_max;
    public final long ipv4_entries;
    public final long ipv4_entries_max;
    public final long ipv6_entries;
    public final long ipv6_entries_max;
    public final long ipv4_ipv6_entries;
    public final long ipv6_ipv6_entries_max;
    public final long long_ipv6_entries;
    public final long long_ipv6_entries_max;
    public final long total_routes;
    public final long total_routes_max;
    public final long ecmp_nexthops;
    public final long ecmp_nexthops_max;
    public final long mac_entries;
    public final long mac_entries_max;
    public final long ipv4_neighbors;
    public final long ipv6_neighbors;
    public final long ipv4_routes;
    public final long ipv6_routes;
    public final long acl_ingress_entries;
    public final long acl_ingress_entries_max;
    public final long acl_ingress_counters;
    public final long acl_ingress_counters_max;
    public final long acl_ingress_meters;
    public final long acl_ingress_meters_max;
    public final long acl_ingress_slices;
    public final long acl_ingress_slices_max;
    public final long acl_egress_entries;
    public final long acl_egress_entries_max;
    public final long acl_egress_counters;
    public final long acl_egress_counters_max;
    public final long acl_egress_meters;
    public final long acl_egress_meters_max;
    public final long acl_egress_slices;
    public final long acl_egress_slices_max;

    public HwTables(final ByteBuffer buffer) throws InvalidPacketException {
        this.host_entries = BufferUtils.uint32(buffer);
        this.host_entries_max = BufferUtils.uint32(buffer);
        this.ipv4_entries = BufferUtils.uint32(buffer);
        this.ipv4_entries_max = BufferUtils.uint32(buffer);
        this.ipv6_entries = BufferUtils.uint32(buffer);
        this.ipv6_entries_max = BufferUtils.uint32(buffer);
        this.ipv4_ipv6_entries = BufferUtils.uint32(buffer);
        this.ipv6_ipv6_entries_max = BufferUtils.uint32(buffer);
        this.long_ipv6_entries = BufferUtils.uint32(buffer);
        this.long_ipv6_entries_max = BufferUtils.uint32(buffer);
        this.total_routes = BufferUtils.uint32(buffer);
        this.total_routes_max = BufferUtils.uint32(buffer);
        this.ecmp_nexthops = BufferUtils.uint32(buffer);
        this.ecmp_nexthops_max = BufferUtils.uint32(buffer);
        this.mac_entries = BufferUtils.uint32(buffer);
        this.mac_entries_max = BufferUtils.uint32(buffer);
        this.ipv4_neighbors = BufferUtils.uint32(buffer);
        this.ipv6_neighbors = BufferUtils.uint32(buffer);
        this.ipv4_routes = BufferUtils.uint32(buffer);
        this.ipv6_routes = BufferUtils.uint32(buffer);
        this.acl_ingress_entries = BufferUtils.uint32(buffer);
        this.acl_ingress_entries_max = BufferUtils.uint32(buffer);
        this.acl_ingress_counters = BufferUtils.uint32(buffer);
        this.acl_ingress_counters_max = BufferUtils.uint32(buffer);
        this.acl_ingress_meters = BufferUtils.uint32(buffer);
        this.acl_ingress_meters_max = BufferUtils.uint32(buffer);
        this.acl_ingress_slices = BufferUtils.uint32(buffer);
        this.acl_ingress_slices_max = BufferUtils.uint32(buffer);
        this.acl_egress_entries = BufferUtils.uint32(buffer);
        this.acl_egress_entries_max = BufferUtils.uint32(buffer);
        this.acl_egress_counters = BufferUtils.uint32(buffer);
        this.acl_egress_counters_max = BufferUtils.uint32(buffer);
        this.acl_egress_meters = BufferUtils.uint32(buffer);
        this.acl_egress_meters_max = BufferUtils.uint32(buffer);
        this.acl_egress_slices = BufferUtils.uint32(buffer);
        this.acl_egress_slices_max = BufferUtils.uint32(buffer);
    }
}
