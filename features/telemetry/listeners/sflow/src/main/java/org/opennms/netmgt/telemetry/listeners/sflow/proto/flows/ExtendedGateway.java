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
import java.util.Optional;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Array;

import com.google.common.primitives.UnsignedInteger;

// struct extended_gateway {
//    next_hop nexthop;           /* Address of the border router that should
//                                   be used for the destination network */
//    unsigned int as;            /* Autonomous system number of router */
//    unsigned int src_as;        /* Autonomous system number of source */
//    unsigned int src_peer_as;   /* Autonomous system number of source peer */
//    as_path_type dst_as_path<>; /* Autonomous system path to the destination */
//    unsigned int communities<>; /* Communities associated with this route */
//    unsigned int localpref;     /* LocalPref associated with this route */
// };

public class ExtendedGateway {
    public final NextHop nexthop;
    public final long as;
    public final long src_as;
    public final long src_peer_as;
    public final Array<AsPathType> dst_as_path;
    public final Array<UnsignedInteger> communities;
    public final long localpref;

    public ExtendedGateway(final ByteBuffer buffer) throws InvalidPacketException {
        this.nexthop = new NextHop(buffer);
        this.as = BufferUtils.uint32(buffer);
        this.src_as = BufferUtils.uint32(buffer);
        this.src_peer_as = BufferUtils.uint32(buffer);
        this.dst_as_path = new Array(buffer, Optional.empty(), AsPathType::new);
        this.communities = new Array(buffer, Optional.empty(), BufferUtils::uint32);
        this.localpref = BufferUtils.uint32(buffer);
    }
}
