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

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.bytes;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint32;

import java.net.InetAddress;
import java.nio.ByteBuffer;

import org.opennms.core.utils.InetAddressUtils;

import com.google.common.base.MoreObjects;

public class PeerFlags {
    public enum AddressVersion {
        IP_V4,
        IP_V6,
    }

    public final AddressVersion addressVersion;  // uint8 x0000000
    public final boolean postPolicy;             // ..... 0x000000
    public final boolean legacyASPath;           // ..... 00x00000

    public PeerFlags(final int flags) {
        this.addressVersion = ((flags >> 7 & 0x01) == 1) ? AddressVersion.IP_V6 : AddressVersion.IP_V4;
        this.postPolicy = (flags >> 6 & 0x01) == 1;
        this.legacyASPath = (flags >> 5 & 0x01) == 1;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("addressVersion", this.addressVersion)
                .add("postPolicy", this.postPolicy)
                .add("legacyASPath", this.legacyASPath)
                .toString();
    }

    public InetAddress parsePaddedAddress(final ByteBuffer buffer) {
        final byte[] bytes;
        switch (this.addressVersion) {
            case IP_V4:
                skip(buffer, 12);
                bytes = bytes(buffer, 4);
                break;

            case IP_V6:
                bytes = bytes(buffer, 16);
                break;

            default:
                throw new IllegalStateException();
        }

        return InetAddressUtils.getInetAddress(bytes);
    }

    public InetAddress parseAddress(final ByteBuffer buffer) {
        final byte[] bytes;
        switch (this.addressVersion) {
            case IP_V4:
                bytes = bytes(buffer, 4);
                break;

            case IP_V6:
                bytes = bytes(buffer, 16);
                break;

            default:
                throw new IllegalStateException();
        }

        return InetAddressUtils.getInetAddress(bytes);
    }

    public long parseAS(final ByteBuffer buffer) {
        return this.legacyASPath
                ? uint16(buffer)
                : uint32(buffer);
    }
}
