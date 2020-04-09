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

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.bytes;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.repeatRemaining;
import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.uint16;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerAccessor;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.TLV;

import com.google.common.base.MoreObjects;

import io.netty.buffer.ByteBuf;

public class TerminationPacket implements Packet {
    public final Header header;
    public final TLV.List<Element, Element.Type, Information> information;

    public TerminationPacket(final Header header, final ByteBuf buffer, final PeerAccessor peerAccessor) throws InvalidPacketException {
        this.header = Objects.requireNonNull(header);

        this.information = TLV.List.wrap(repeatRemaining(buffer, Element::new));
    }

    @Override
    public void accept(final Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <R> R map(final Mapper<R> mapper) {
        return mapper.map(this);
    }

    public static class Element extends TLV<Element.Type, Information, Void> {

        public Element(final ByteBuf buffer) throws InvalidPacketException {
            super(buffer, Element.Type::from, null, Optional.empty());
        }

        public enum Type implements TLV.Type<Information, Void> {
            STRING {
                @Override
                public Information parse(final ByteBuf buffer, final Void parameter, final Optional<PeerInfo> peerInfo) {
                    return new StringInformation(new String(bytes(buffer, buffer.readableBytes()), StandardCharsets.UTF_8));
                }
            },

            REASON {
                @Override
                public Information parse(final ByteBuf buffer, final Void parameter, final Optional<PeerInfo> peerInfo) {
                    final int reason = uint16(buffer);
                    return new ReasonInformation(reason);
                }
            },

            UNKNOWN {
                @Override
                public Information parse(final ByteBuf buffer, final Void parameter, final Optional<PeerInfo> peerInfo) throws InvalidPacketException {
                    return new UnknownInformation();
                }
            };

            private static Element.Type from(final int type) {
                switch (type) {
                    case 0:
                        return STRING;
                    case 1:
                        return REASON;
                    default:
                        BmpParser.RATE_LIMITED_LOG.debug("Unknown Termination Packet Type: {}", type);
                        return UNKNOWN;
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

    public interface Information {
        void accept(final Visitor visitor);

        interface Visitor {
            void visit(final StringInformation string);
            void visit(final ReasonInformation reason);
            void visit(final UnknownInformation unknown);
        }
    }

    public static class StringInformation implements Information {
        public final String string;

        public StringInformation(final String string) {
            this.string = string;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("string", string)
                    .toString();
        }
    }

    public static class ReasonInformation implements Information {
        public final int reason;

        public ReasonInformation(final int reason) {
            this.reason = reason;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("reason", reason)
                    .toString();
        }
    }

    public static class UnknownInformation implements Information {
        public UnknownInformation() {
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .toString();
        }
    }
}
