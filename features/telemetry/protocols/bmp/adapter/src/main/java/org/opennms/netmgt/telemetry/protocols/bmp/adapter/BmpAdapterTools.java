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
