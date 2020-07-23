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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;

import com.google.common.primitives.UnsignedInts;
import com.google.protobuf.Timestamp;

public interface BmpAdapterTools {

    static InetAddress address(final Transport.IpAddress address) {
        switch (address.getAddressCase()) {
            case V4:
                return InetAddressUtils.getInetAddress(address.getV4().toByteArray());
            case V6:
                return InetAddressUtils.getInetAddress(address.getV6().toByteArray());
            default:
                throw new IllegalStateException();
        }
    }

    static String addressAsStr(final Transport.IpAddress address) {
        return InetAddressUtils.str(address(address));
    }

    static Instant timestamp(final Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    static Optional<Transport.RouteMonitoringPacket.PathAttribute> getPathAttributeOfType(Transport.RouteMonitoringPacket routeMonitoring,
                                                                                                  Transport.RouteMonitoringPacket.PathAttribute.ValueCase pathAttrValueType) {
        return routeMonitoring.getAttributesList().stream()
                .filter(p -> Objects.equals(pathAttrValueType, p.getValueCase()))
                .findFirst();
    }

    static Stream<Transport.RouteMonitoringPacket.PathAttribute> getPathAttributesOfType(Transport.RouteMonitoringPacket routeMonitoring,
                                                                                        Transport.RouteMonitoringPacket.PathAttribute.ValueCase pathAttrValueType) {
        return routeMonitoring.getAttributesList().stream()
                .filter(p -> Objects.equals(pathAttrValueType, p.getValueCase()));
    }

    static boolean isV4(Transport.IpAddress ipAddress) {
        return Transport.IpAddress.AddressCase.V4.equals(ipAddress.getAddressCase());
    }

    static long uint32(int i) {
        return UnsignedInts.toLong(i);
    }

    static String asAttr(final int val) {
        int as = (val >> 16) & 0xFFFF;
        int attr = (val >> 0) & 0xFFFF;
        return String.format("%d:%d", as, attr);
    }
}
