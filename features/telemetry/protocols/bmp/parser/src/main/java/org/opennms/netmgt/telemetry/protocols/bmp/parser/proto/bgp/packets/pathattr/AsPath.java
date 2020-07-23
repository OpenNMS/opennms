/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.repeatCount;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.util.List;
import java.util.function.Function;

import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class AsPath implements Attribute {
    public final List<Segment> segments;

    public AsPath(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
        segments = BufferUtils.repeatRemaining(buffer, segmentBuffer -> new Segment(segmentBuffer, flags));
    }

    public static class Segment {
        public final Type type;
        public final List<Long> path;

        public Segment(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
            this.type = Type.from(uint8(buffer));
            this.path = repeatCount(buffer, uint8(buffer), flags::parseAS);
        }

        public enum Type {
            AS_SET,
            AS_SEQUENCE,
            UNKNOWN;

            public static Type from(final int type) {
                switch (type) {
                    case 1: return AS_SET;
                    case 2: return AS_SEQUENCE;
                    default:
                        BmpParser.RATE_LIMITED_LOG.debug("Unknown AS Path Type: {}", type);
                        return UNKNOWN;
                }
            }

            public <R> R map(final Function<Type, R> mapper) {
                return mapper.apply(this);
            }
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("type", this.type)
                    .add("path", this.path)
                    .toString();
        }
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("segments", this.segments)
                .toString();
    }
}
