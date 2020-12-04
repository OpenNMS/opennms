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

import java.util.List;

import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

/**
 *     0                   1                   2                   3
 *     0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                      Global Administrator                     |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                       Local Data Part 1                       |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                       Local Data Part 2                       |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class LargeCommunities implements Attribute {
    public final List<LargeCommunity> largeCommunities;

    public LargeCommunities(final ByteBuf buffer, final PeerFlags flags) {
        this.largeCommunities = BufferUtils.repeatRemaining(buffer, LargeCommunity::new);
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("largeCommunities", this.largeCommunities)
                .toString();
    }

    public static class LargeCommunity {
        public final long globalAdministrator; // uint32
        public final long localDataPart1; // uint32
        public final long localDataPart2; // uint32

        public LargeCommunity(final ByteBuf buffer) {
            this.globalAdministrator = uint32(buffer);
            this.localDataPart1 = uint32(buffer);
            this.localDataPart2 = uint32(buffer);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("globalAdministrator", this.globalAdministrator)
                    .add("localDataPart1", this.localDataPart1)
                    .add("localDataPart2", this.localDataPart2)
                    .toString();
        }
    }
}
