/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
