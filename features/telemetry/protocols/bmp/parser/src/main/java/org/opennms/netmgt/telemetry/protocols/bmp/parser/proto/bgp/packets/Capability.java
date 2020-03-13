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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.repeatRemaining;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.util.List;

import io.netty.buffer.ByteBuf;

public class Capability {

    private final int code;
    private final int length;
    private final ByteBuf value;
    private final String message;

    public Capability(final ByteBuf buffer) {
        this.code = uint8(buffer);
        this.length = uint8(buffer);
        this.value = slice(buffer, this.length);
        this.message = generateMessage(code, value);
    }

    public String getMessage() {
        return message;
    }

    private String generateMessage(final int code, final ByteBuf value) {
        if (code == 72 || (code >= 10 && code <= 63) || (code >= 74 && code <= 127)) {
            return String.format("Unassigned (%d)", code);
        }

        if (code >= 128 && code <= 255) {
            return String.format("Reserved for Private Use", code);
        }
        switch (this.code) {
            case 0:
                return "Reserved";
            case 1: {
                int afi = uint16(value);
                skip(value, 1);
                int safi = uint8(value);

                return String.format("Multiprotocol Extensions for BGP-4 (1): afi=%d safi=%d: %s %s",
                        code,
                        afi,
                        safi,
                        AddressFamilyIdentifier.from(afi).getDescription(),
                        SubsequentAddressFamilyIdentifier.from(safi).getDescription());
            }
            case 2:
                return "Route Refresh Capability for BGP-4 (2)";
            case 3:
                return "Outbound Route Filtering Capability (3)";
            case 4:
                return "Multiple routes to a destination capability (deprecated) (4)";
            case 5:
                return "Extended Next Hop Encoding (5)";
            case 6:
                return "BGP Extended Message (6)";
            case 7:
                return "BGPsec Capability (7)";
            case 8:
                return "BGP Role (TEMPORARY - registered 2018-03-29, extension registered 2019-03-18, expires 2020-03-29) (8)";
            case 9:
                return "Multiple Labels Capability (9)";
            case 64:
                return "Graceful Restart Capability (64)";
            case 65:
                return "Support for 4-octet AS number capability (65)";
            case 66:
                return "Deprecated (2003-03-06) (66)";
            case 67:
                return "Support for Dynamic Capability (capability specific) (67)";
            case 68:
                return "Multisession BGP Capability (68)";
            case 69: {
                final List<String> strings = repeatRemaining(value, b -> {
                    int afi = uint16(b);
                    int safi = uint8(b);
                    int sendReceive = uint8(b);

                    return String.format("afi=%d safi=%d send/receive=%d: %s %s %s",
                            afi,
                            safi,
                            sendReceive,
                            AddressFamilyIdentifier.from(afi),
                            SubsequentAddressFamilyIdentifier.from(safi),
                            parseSendReceive(sendReceive));
                });

                return String.format("ADD-PATH Capability (69): %s", String.join(", ", strings));
            }
            case 70:
                return "Enhanced Route Refresh Capability (70)";
            case 71:
                return "Long-Lived Graceful Restart (71)";
            case 73:
                return "FQDN Capability (73)";
        }
        return String.format("Unknown capability (%d)", code);
    }

    private String parseSendReceive(final int sendReceive) {
        switch (sendReceive) {
            case 1:
                return "Receive";
            case 2:
                return "Send";
            case 3:
                return "Send/Receive";
            default:
                return "unknown";
        }
    }
}
