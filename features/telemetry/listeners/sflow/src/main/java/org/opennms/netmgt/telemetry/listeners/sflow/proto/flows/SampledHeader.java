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
import org.opennms.netmgt.telemetry.listeners.sflow.proto.Opaque;

// struct sampled_header {
//    header_protocol protocol;       /* Format of sampled header */
//    unsigned int frame_length;      /* Original length of packet before
//                                       sampling.
//                                       Note: For a layer 2 header_protocol,
//                                             length is total number of octets
//                                             of data received on the network 
//                                             (excluding framing bits but
//                                             including FCS octets).
//                                             Hardware limitations may
//                                             prevent an exact reporting
//                                             of the underlying frame length,
//                                             but an agent should attempt to
//                                             be as accurate as possible. Any
//                                             octets added to the frame_length
//                                             to compensate for encapsulations
//                                             removed by the underlying hardware
//                                             must also be added to the stripped
//                                             count. */
//    unsigned int stripped;          /* The number of octets removed from
//                                       the packet before extracting the
//                                       header<> octets. Trailing encapsulation
//                                       data corresponding to any leading
//                                       encapsulations that were stripped must
//                                       also be stripped. Trailing encapsulation
//                                       data for the outermost protocol layer
//                                       included in the sampled header must be
//                                       stripped.
// 
//                                       In the case of a non-encapsulated 802.3
//                                       packet stripped >= 4 since VLAN tag
//                                       information might have been stripped off
//                                       in addition to the FCS.
// 
//                                       Outer encapsulations that are ambiguous,
//                                       or not one of the standard header_protocol
//                                       must be stripped. */
//    opaque header<>;                /* Header bytes */
// };

public class SampledHeader {
    public final HeaderProtocol protocol;
    public final long frame_length;
    public final long stripped;
    public final Opaque<byte[]> header;

    public SampledHeader(final ByteBuffer buffer) throws InvalidPacketException {
        this.protocol = HeaderProtocol.from(buffer);
        this.frame_length = BufferUtils.uint32(buffer);
        this.stripped = BufferUtils.uint32(buffer);
        this.header = new Opaque(buffer, Optional.empty(), Opaque::parseBytes);
    }
}
