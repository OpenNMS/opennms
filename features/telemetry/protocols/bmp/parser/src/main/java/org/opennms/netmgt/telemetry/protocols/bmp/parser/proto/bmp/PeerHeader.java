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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.bytes;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint32;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.net.InetAddress;
import java.time.Instant;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class PeerHeader {
    public final Type type;   // uint8

    public final PeerFlags flags; // uint8

    public final String distinguisher; // uint32

    public final InetAddress address; // 16 bytes

    public final long as; // uint32
    public final InetAddress id; // uint32

    public final Instant timestamp; // uint32 (seconds) + uint32(microseconds)

    public PeerHeader(final ByteBuf buffer) throws InvalidPacketException {
        this.type = Type.from(buffer);

        this.flags = new PeerFlags(uint8(buffer));

        this.distinguisher = parseDistinguisher(slice(buffer, 8));

        this.address = this.flags.parsePaddedAddress(buffer);

        this.as = uint32(buffer);
        this.id = InetAddressUtils.getInetAddress(bytes(buffer, 4));

        this.timestamp = Instant.ofEpochSecond(uint32(buffer), uint32(buffer) * 1000);
    }

    public enum Type {
        GLOBAL_INSTANCE,
        RD_INSTANCE,
        LOCAL_INSTANCE;

        private static Type from(final ByteBuf buffer) throws InvalidPacketException {
            final int type = uint8(buffer);
            switch (type) {
                case 0:
                    return GLOBAL_INSTANCE;
                case 1:
                    return RD_INSTANCE;
                case 2:
                    return LOCAL_INSTANCE;
                default:
                    throw new InvalidPacketException(buffer, "Unknown peer type: %d", type);
            }
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

    private static String parseDistinguisher(final ByteBuf buffer) throws InvalidPacketException {
        final int type = uint16(buffer);
        switch (type) {
            case 0: {
                final int as = uint16(buffer);
                final long assigned = uint32(buffer);
                return String.format("%d:%d", as, assigned);
            }

            case 1: {
                final InetAddress admin = InetAddressUtils.getInetAddress(bytes(buffer, 4));
                final int assigned = uint16(buffer);
                return String.format("%s:%d", admin.getHostAddress(), assigned);
            }

            case 2: {
                final long as = uint32(buffer);
                final int assigned = uint16(buffer);
                return String.format("%d:%d", as, assigned);
            }

            default: {
                throw new InvalidPacketException(buffer, "Unknown route distinguisher type: {}", type);
            }
        }
    }
}
