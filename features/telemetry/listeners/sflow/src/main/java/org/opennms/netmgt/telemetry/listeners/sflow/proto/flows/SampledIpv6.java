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

// struct sampled_ipv6 {
//    unsigned int length;     /* The length of the IP packet excluding
//                                lower layer encapsulations */
//    unsigned int protocol;   /* IP next header
//                                (for example, TCP = 6, UDP = 17) */
//    ip_v6 src_ip;            /* Source IP Address */
//    ip_v6 dst_ip;            /* Destination IP Address */
//    unsigned int src_port;   /* TCP/UDP source port number or equivalent */
//    unsigned int dst_port;   /* TCP/UDP destination port number or equivalent */
//    unsigned int tcp_flags;  /* TCP flags */
//    unsigned int priority;   /* IP priority */
// };

public class SampledIpv6 implements FlowData {
    public final long length;
    public final long protocol;
    public final IpV6 src_ip;
    public final IpV6 dst_ip;
    public final long src_port;
    public final long dst_port;
    public final long tcp_flags;
    public final long priority;

    public SampledIpv6(final ByteBuffer buffer) throws InvalidPacketException {
        this.length = BufferUtils.uint32(buffer);
        this.protocol = BufferUtils.uint32(buffer);
        this.src_ip = new IpV6(buffer);
        this.dst_ip = new IpV6(buffer);
        this.src_port = BufferUtils.uint32(buffer);
        this.dst_port = BufferUtils.uint32(buffer);
        this.tcp_flags = BufferUtils.uint32(buffer);
        this.priority = BufferUtils.uint32(buffer);
    }
}
