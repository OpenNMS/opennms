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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets;

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.bytes;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.repeatRemaining;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint16;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.TLV;

import com.google.common.base.MoreObjects;

public class TerminationPacket implements Packet {
    public final Header header;
    public final TLV.List<Element, Element.Type, String> information;

    public TerminationPacket(final Header header, final ByteBuffer buffer) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        this.information = TLV.List.wrap(repeatRemaining(buffer, Element::new));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    public static class Element extends TLV<Element.Type, String, Void> {

        public Element(final ByteBuffer buffer) throws InvalidPacketException {
            super(buffer, Element.Type::from, null);
        }

        public enum Type implements TLV.Type<String, Void> {
            STRING {
                @Override
                public String parse(final ByteBuffer buffer, final Void parameter) {
                    return new String(bytes(buffer, buffer.remaining()), StandardCharsets.UTF_8);
                }
            },

            REASON {
                @Override
                public String parse(final ByteBuffer buffer, final Void parameter) {
                    final int reason = uint16(buffer);
                    switch (reason) {
                        case 0:
                            return "Session administratively closed.  The session might be re-initiated";
                        case 1:
                            return "Unspecified reason";
                        case 2:
                            return "Out of resources.  The router has exhausted resources available for the BMP session";
                        case 3:
                            return "Redundant connection.  The router has determined that this connection is redundant with another one";
                        case 4:
                            return "Session permanently administratively closed, will not be re-initiated";
                        default:
                            return "Unknown reason";
                    }
                }
            };

            private static Element.Type from(final int type) {
                switch (type) {
                    case 0:
                        return STRING;
                    case 1:
                        return REASON;
                    default:
                        throw new IllegalArgumentException("Unknown termination type");
                }
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("header", this.header)
                .add("terminations", this.information)
                .toString();
    }
}
