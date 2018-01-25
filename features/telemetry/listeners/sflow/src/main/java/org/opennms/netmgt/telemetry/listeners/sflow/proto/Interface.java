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

package org.opennms.netmgt.telemetry.listeners.sflow.proto;

/* Input/output port information
     Encoding of interface(s) involved in the packet's path through
     the device.

     0 if interface is not known.
     The most significant 2 bits are used to indicate the format of
     the 30 bit value.

        - format = 0 single interface
            value is ifIndex of the interface. The maximum value,
            0x3FFFFFFF, indicates that there is no input or output
            interface (according to which field it appears in).
            This is used in describing traffic which is not
            bridged, routed, or otherwise sent through the device
            being monitored by the agent, but which rather
            originates or terminates in the device itself.  In
            the input field, this value is used to indicate
            packets for which the origin was the device itself
            (e.g. a RIP request packet sent by the device, if it
            is acting as an IP router).  In the output field,
            this value is used to indicate packets for which the
            destination was the device itself (e.g. a RIP
            response packet (whether unicast or not) received by
            the device, if it is acting as an IP router).

        - format = 1 packet discarded
            value is a reason code. Currently the following codes
            are defined:
                0 - 255 use ICMP Destination Unreachable codes
                        See www.iana.org for authoritative list.
                        RFC 1812, section 5.2.7.1 describes the
                        current codes.  Note that the use of
                        these codes does not imply that the
                        packet to which they refer is an IP
                        packet, or if it is, that an ICMP message
                        of any kind was generated for it.
                        Current value are:
                          0  Net Unreachable
                          1  Host Unreachable
                          2  Protocol Unreachable
                          3  Port Unreachable
                          4  Fragmentation Needed and
                             Don't Fragment was Set
                          5  Source Route Failed
                          6  Destination Network Unknown
                          7  Destination Host Unknown
                          8  Source Host Isolated
                          9  Communication with Destination
                             Network is Administratively
                             Prohibited
                         10  Communication with Destination Host
                             is Administratively Prohibited
                         11  Destination Network Unreachable for
                             Type of Service
                         12  Destination Host Unreachable for
                             Type of Service
                         13  Communication Administratively
                             Prohibited
                         14  Host Precedence Violation
                         15  Precedence cutoff in effect
                256 = unknown
                257 = ttl exceeded
                258 = ACL
                259 = no buffer space
                260 = RED
                261 = traffic shaping/rate limiting
                262 = packet too big (for protocols that don't
                      support fragmentation)

             Note: Additional reason codes may be published over
                   time. An application receiving sFlow must be
                   prepared to accept additional reason codes.
                   The authoritative list of reason codes will
                   be maintained at www.sflow.org

        - format = 2 multiple destination interfaces
            value is the number of interfaces. A value of 0
            indicates an unknown number greater than 1.

      Note: Formats 1 & 2 apply only to an output interface and
            never to an input interface. A packet is always
            received on a single (possibly unknown) interface.

      Examples:
         0x00000002  indicates ifIndex = 2
         0x00000000  ifIndex unknown.
         0x40000001  packet discarded because of ACL.
         0x80000007  indicates a packet sent to 7 interfaces.
         0x80000000  indicates a packet sent to an unknown number
                     of interfaces greater than 1. */

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

import com.google.common.base.MoreObjects;

public class Interface {
    public final long format;
    public final long value;

    public Interface(final ByteBuffer buffer) throws InvalidPacketException {
        long l = BufferUtils.uint32(buffer);
        this.format = l >> 30 & (2<<2)-1;
        this.value = l & (2<<30)-1;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("format", format)
                .add("value", value)
                .toString();
    }
}
