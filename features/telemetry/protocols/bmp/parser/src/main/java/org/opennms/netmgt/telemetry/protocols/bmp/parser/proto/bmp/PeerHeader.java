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
package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.bytes;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint64;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.net.InetAddress;
import java.time.Instant;
import java.util.function.Function;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;

import io.netty.buffer.ByteBuf;

public class PeerHeader {
    public final Type type;   // uint8

    public final PeerFlags flags; // uint8
    public final LocRibFlags locRibFlags; // unint8

    public final UnsignedLong distinguisher; // uint64

    public final InetAddress address; // 16 bytes

    public final long as; // uint32
    public final InetAddress id; // uint32

    public final Instant timestamp; // uint32 (seconds) + uint32(microseconds)

    public PeerHeader(final ByteBuf buffer) throws InvalidPacketException {
        this.type = Type.from(buffer);

        if (this.type == Type.LOC_RIB_INSTANCE) {
            this.flags = null;
            this.locRibFlags = new LocRibFlags(uint8(buffer));
        } else {
            this.flags = new PeerFlags(uint8(buffer));
            this.locRibFlags = null;
        }

        this.distinguisher = uint64(buffer);

        this.address = this.flags == null? null : this.flags.parsePaddedAddress(buffer);

        this.as = uint32(buffer);
        this.id = InetAddressUtils.getInetAddress(bytes(buffer, 4));

        this.timestamp = Instant.ofEpochSecond(uint32(buffer), uint32(buffer) * 1000);
    }

    public enum Type {
        GLOBAL_INSTANCE,
        RD_INSTANCE,
        LOCAL_INSTANCE,
        LOC_RIB_INSTANCE;

        private static Type from(final ByteBuf buffer) throws InvalidPacketException {
            final int type = uint8(buffer);
            switch (type) {
                case 0:
                    return GLOBAL_INSTANCE;
                case 1:
                    return RD_INSTANCE;
                case 2:
                    return LOCAL_INSTANCE;
                case 3:
                    return LOC_RIB_INSTANCE;
                default:
                    throw new InvalidPacketException(buffer, "Unknown peer type: %d", type);
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
                .add("config", this.flags)
                .add("distinguisher", this.distinguisher)
                .add("address", this.address)
                .add("as", this.as)
                .add("id", this.id)
                .add("timestamp", this.timestamp)
                .toString();
    }
}
