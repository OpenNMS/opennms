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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint8;

import java.util.Objects;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class NotificationPacket implements Packet {
    public final Header header;

    public final Error error; // uint8 + uint8

    public NotificationPacket(final Header header, final ByteBuf buffer, final PeerFlags flags) {
        this.header = Objects.requireNonNull(header);

        this.error = Error.from(uint8(buffer), uint8(buffer));

        // Skip the error data (see https://tools.ietf.org/html/rfc4271#section-6)
        skip(buffer, buffer.readableBytes());
    }

    public enum Error {
        // Message Header Error            1
        CONNECTION_NOT_SYNCHRONIZED,    // 1
        BAD_MESSAGE_LENGTH,             // 2
        BAD_MESSAGE_TYPE,               // 3

        // Open Message Error              2
        UNSUPPORTED_VERSION_NUMBER,     // 1
        BAD_PEER_AS,                    // 2
        BAD_BGP_IDENTIFIER,             // 3
        UNSUPPORTED_OPTIONAL_PARAMETER, // 4
        AUTHENTICATION_FAILURE,         // 5
        UNACCEPTABLE_HOLD_TIME,         // 6

        // Update Message Error                3
        MALFORMED_ATTRIBUTE_LIST,           // 1
        UNRECOGNIZED_WELL_KNOWN_ATTRIBUTE,  // 2
        MISSING_WELL_KNOWN_ATTRIBUTE,       // 3
        ATTRIBUTE_FLAGS_ERROR,              // 4
        ATTRIBUTE_LENGTH_ERROR,             // 5
        INVALID_ORIGIN_ATTRIBUTE,           // 6
        ROUTING_LOOP,                       // 7
        INVALID_NEXT_HOP_ATTRIBUTE,         // 8
        OPTIONAL_ATTRIBUTE_ERROR,           // 9
        INVALID_NETWORK_FIELD,              // 10
        MALFORMED_AS_PATH,                  // 11

        // Hold Timer Expired      4
        HOLD_TIMER_EXPIRED,     // 1

        // FSM Error   5
        FSM_ERROR,  // 1

        // Cease                               6 - see RFC 4486
        MAXIMUM_NUMBER_OF_PREFIXES_REACHED, // 1
        ADMINISTRATIVE_SHUTDOWN,            // 2
        PEER_DECONFIGURED,                  // 3
        ADMINISTRATIVE_RESET,               // 4
        CONNECTION_RESET,                   // 5
        OTHER_CONFIGURATION_CHANGE,         // 6
        CONNECTION_COLLISION_RESOLUTION,    // 7
        OUT_OF_RESOURCES,                   // 8
        ;

        public static Error from(final int code, final int subcode) {
            switch ((code << 8) + subcode) {
                case (1 << 8) + 1: return CONNECTION_NOT_SYNCHRONIZED;
                case (1 << 8) + 2: return BAD_MESSAGE_LENGTH;
                case (1 << 8) + 3: return BAD_MESSAGE_TYPE;

                case (2 << 8) + 1: return UNSUPPORTED_VERSION_NUMBER;
                case (2 << 8) + 2: return BAD_PEER_AS;
                case (2 << 8) + 3: return BAD_BGP_IDENTIFIER;
                case (2 << 8) + 4: return UNSUPPORTED_OPTIONAL_PARAMETER;
                case (2 << 8) + 5: return AUTHENTICATION_FAILURE;
                case (2 << 8) + 6: return UNACCEPTABLE_HOLD_TIME;

                case (3 << 8) + 1: return MALFORMED_ATTRIBUTE_LIST;
                case (3 << 8) + 2: return UNRECOGNIZED_WELL_KNOWN_ATTRIBUTE;
                case (3 << 8) + 3: return MISSING_WELL_KNOWN_ATTRIBUTE;
                case (3 << 8) + 4: return ATTRIBUTE_FLAGS_ERROR;
                case (3 << 8) + 5: return ATTRIBUTE_LENGTH_ERROR;
                case (3 << 8) + 6: return INVALID_ORIGIN_ATTRIBUTE;
                case (3 << 8) + 7: return ROUTING_LOOP;
                case (3 << 8) + 8: return INVALID_NEXT_HOP_ATTRIBUTE;
                case (3 << 8) + 9: return OPTIONAL_ATTRIBUTE_ERROR;
                case (3 << 8) + 10: return INVALID_NETWORK_FIELD;
                case (3 << 8) + 11: return MALFORMED_AS_PATH;

                case (4 << 8) + 1: return HOLD_TIMER_EXPIRED;

                case (5 << 8) + 1: return FSM_ERROR;

                case (6 << 8) + 1: return MAXIMUM_NUMBER_OF_PREFIXES_REACHED;
                case (6 << 8) + 2: return ADMINISTRATIVE_SHUTDOWN;
                case (6 << 8) + 3: return PEER_DECONFIGURED;
                case (6 << 8) + 4: return ADMINISTRATIVE_RESET;
                case (6 << 8) + 5: return CONNECTION_RESET;
                case (6 << 8) + 6: return OTHER_CONFIGURATION_CHANGE;
                case (6 << 8) + 7: return CONNECTION_COLLISION_RESOLUTION;
                case (6 << 8) + 8: return OUT_OF_RESOURCES;

                default:
                    throw new IllegalArgumentException("Unknown error code: " + code + "/" + subcode);
            }
        }
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    public static NotificationPacket parse(final ByteBuf buffer, final PeerFlags flags) throws InvalidPacketException {
        final Header header = new Header(buffer);
        if (header.type != Header.Type.NOTIFICATION) {
            throw new InvalidPacketException(buffer, "Expected Notification Message, got: {}", header.type);
        }

        return new NotificationPacket(header, slice(buffer, header.length - Header.SIZE), flags);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("error", this.error)
                .toString();
    }
}
