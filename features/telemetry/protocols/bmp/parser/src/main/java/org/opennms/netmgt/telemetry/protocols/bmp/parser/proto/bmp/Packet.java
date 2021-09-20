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

import java.util.Objects;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.InitiationPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.PeerDownPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.PeerUpPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.RouteMirroringPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.RouteMonitoringPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.StatisticsReportPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.TerminationPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.UnknownPacket;

import io.netty.buffer.ByteBuf;

public interface Packet {
    void accept(final Visitor visitor);
    <R> R map(final Mapper<R> mapper);

    interface Parser {
        Packet parse(final Header header, final ByteBuf buffer, final PeerAccessor peerAccessor) throws InvalidPacketException;
    }

    interface Visitor {
        void visit(final InitiationPacket packet);
        void visit(final TerminationPacket packet);
        void visit(final PeerUpPacket packet);
        void visit(final PeerDownPacket packet);
        void visit(final StatisticsReportPacket packet);
        void visit(final RouteMonitoringPacket packet);
        void visit(final RouteMirroringPacket packet);
        void visit(final UnknownPacket packet);

        class Adapter implements Visitor {
            public void visit(final RouteMonitoringPacket packet) {}
            public void visit(final StatisticsReportPacket packet) {}
            public void visit(final PeerDownPacket packet) {}
            public void visit(final PeerUpPacket packet) {}
            public void visit(final InitiationPacket packet) {}
            public void visit(final TerminationPacket packet) {}
            public void visit(final RouteMirroringPacket packet) {}
            public void visit(final UnknownPacket packet) {}
        }
    }

    interface Mapper<R> {
        R map(final InitiationPacket packet);
        R map(final TerminationPacket packet);
        R map(final PeerUpPacket packet);
        R map(final PeerDownPacket packet);
        R map(final StatisticsReportPacket packet);
        R map(final RouteMonitoringPacket packet);
        R map(final RouteMirroringPacket packet);
        R map(final UnknownPacket packet);

        class Adapter<R> implements Mapper<R> {
            private final R defaultValue;

            public Adapter(final R defaultValue) {
                this.defaultValue = Objects.requireNonNull(defaultValue);
            }

            public R map(final InitiationPacket packet) { return this.defaultValue; }
            public R map(final TerminationPacket packet) { return this.defaultValue; }
            public R map(final PeerUpPacket packet) { return this.defaultValue; }
            public R map(final PeerDownPacket packet) { return this.defaultValue; }
            public R map(final StatisticsReportPacket packet) { return this.defaultValue; }
            public R map(final RouteMonitoringPacket packet) { return this.defaultValue; }
            public R map(final RouteMirroringPacket packet) { return this.defaultValue; }
            public R map(final UnknownPacket packet) { return this.defaultValue; }
        }
    }

    default Optional<PeerHeader> getPeerHeader() {
        return this.map(new Mapper<Optional<PeerHeader>>() {

            @Override
            public Optional<PeerHeader> map(final InitiationPacket packet) {
                return Optional.empty();
            }

            @Override
            public Optional<PeerHeader> map(final TerminationPacket packet) {
                return Optional.empty();
            }

            @Override
            public Optional<PeerHeader> map(final PeerUpPacket packet) {
                return Optional.of(packet.peerHeader);
            }

            @Override
            public Optional<PeerHeader> map(final PeerDownPacket packet) {
                return Optional.of(packet.peerHeader);
            }

            @Override
            public Optional<PeerHeader> map(final StatisticsReportPacket packet) {
                return Optional.of(packet.peerHeader);
            }

            @Override
            public Optional<PeerHeader> map(final RouteMonitoringPacket packet) {
                return Optional.of(packet.peerHeader);
            }

            @Override
            public Optional<PeerHeader> map(final RouteMirroringPacket packet) {
                return Optional.of(packet.peerHeader);
            }

            @Override
            public Optional<PeerHeader> map(final UnknownPacket packet) {
                return Optional.empty();
            }
        });
    }
}
