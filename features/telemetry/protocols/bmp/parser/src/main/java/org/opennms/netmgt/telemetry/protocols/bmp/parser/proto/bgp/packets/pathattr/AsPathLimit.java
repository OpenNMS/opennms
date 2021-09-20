/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

/**
 *    The AS_PATHLIMIT attribute is a transitive optional BGP path
 *    attribute, with Type Code 21.  The AS_PATHLIMIT attribute has a fixed
 *    length of 5 octets.  The first octet is an unsigned number that is
 *    the upper bound on the number of ASes in the AS_PATH attribute of the
 *    associated paths.  One octet suffices because the TTL field of the IP
 *    header ensures that only one octet's worth of ASes can ever be
 *    traversed.  The second thru fifth octets are the AS number of the AS
 *    that attached the AS_PATHLIMIT attribute to the NLRI.
 */
public class AsPathLimit implements Attribute {
    public final int upperBound; // uint8
    public final long as; // uint32

    public AsPathLimit(final ByteBuf buffer, final PeerFlags flags) {
        this.upperBound = uint8(buffer);
        this.as = uint32(buffer);
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("upperBound", this.upperBound)
                .add("as", this.as)
                .toString();
    }
}
