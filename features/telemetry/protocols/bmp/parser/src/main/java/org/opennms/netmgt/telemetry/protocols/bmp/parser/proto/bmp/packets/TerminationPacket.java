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
