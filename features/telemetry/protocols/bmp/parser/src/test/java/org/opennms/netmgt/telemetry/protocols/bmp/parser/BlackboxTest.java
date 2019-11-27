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

package org.opennms.netmgt.telemetry.protocols.bmp.parser;

import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.slice;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.NotificationPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.UpdatePacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Aggregator;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AsPath;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.AtomicAggregate;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Attribute;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.LocalPref;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.MultiExistDisc;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.NextHop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.packets.pathattr.Origin;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.InformationElement;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Packet;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerFlags;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.PeerHeader;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.InitiationPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.PeerDownPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.PeerUpPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.RouteMirroringPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.RouteMonitoringPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.StatisticsReportPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.TerminationPacket;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.LocalBgpNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.LocalNoNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.Reason;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.RemoteBgpNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.RemoteNoNotification;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.down.Unknown;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.AdjRibIn;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.AdjRibOut;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.DuplicatePrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.DuplicateUpdate;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.DuplicateWithdraws;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.ExportRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToAsConfedLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToAsPathLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToClusterListLoop;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.InvalidUpdateDueToOriginatorId;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.LocRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PerAfiAdjRibIn;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PerAfiLocRib;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.PrefixTreatAsWithdraw;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.Rejected;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.UpdateTreatAsWithdraw;

import com.google.common.primitives.UnsignedLong;

@RunWith(Parameterized.class)
public class BlackboxTest implements Packet.Visitor {

    private static class AttributeVisitorAdapter implements Attribute.Visitor {
        @Override
        public void visit(Aggregator aggregator) {
            fail("Wrong Attribute Aggregator");
        }

        @Override
        public void visit(AsPath asPath) {
            fail("Wrong Attribute AsPath");
        }

        @Override
        public void visit(AtomicAggregate atomicAggregate) {
            fail("Wrong Attribute AtomicAggregate");
        }

        @Override
        public void visit(LocalPref localPref) {
            fail("Wrong Attribute LocalPref");
        }

        @Override
        public void visit(MultiExistDisc multiExistDisc) {
            fail("Wrong Attribute MultiExistDisc");
        }

        @Override
        public void visit(NextHop nextHop) {
            fail("Wrong Attribute NextHop");
        }

        @Override
        public void visit(Origin origin) {
            fail("Wrong Attribute Origin");
        }
    }

    private static class MetricVisitorAdapter implements org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.packets.stats.Metric.Visitor {
        @Override
        public void visit(DuplicatePrefix duplicatePrefix) {
            fail("Wrong Metric DuplicatePrefix");
        }

        @Override
        public void visit(DuplicateWithdraws duplicateWithdraws) {
            fail("Wrong Metric DuplicateWithdraws");
        }

        @Override
        public void visit(AdjRibIn adjRibIn) {
            fail("Wrong Metric AdjRibIn");
        }

        @Override
        public void visit(AdjRibOut adjRibOut) {
            fail("Wrong Metric AdjRibOut");
        }

        @Override
        public void visit(ExportRib exportRib) {
            fail("Wrong Metric ExportRib");
        }

        @Override
        public void visit(InvalidUpdateDueToAsConfedLoop invalidUpdateDueToAsConfedLoop) {
            fail("Wrong Metric InvalidUpdateDueToAsConfedLoop");
        }

        @Override
        public void visit(InvalidUpdateDueToAsPathLoop invalidUpdateDueToAsPathLoop) {
            fail("Wrong Metric InvalidUpdateDueToAsPathLoop");
        }

        @Override
        public void visit(InvalidUpdateDueToClusterListLoop invalidUpdateDueToClusterListLoop) {
            fail("Wrong Metric InvalidUpdateDueToClusterListLoop");
        }

        @Override
        public void visit(InvalidUpdateDueToOriginatorId invalidUpdateDueToOriginatorId) {
            fail("Wrong Metric InvalidUpdateDueToOriginatorId");
        }

        @Override
        public void visit(PerAfiAdjRibIn perAfiAdjRibIn) {
            fail("Wrong Metric PerAfiAdjRibIn");
        }

        @Override
        public void visit(PerAfiLocRib perAfiLocRib) {
            fail("Wrong Metric PerAfiLocRib");
        }

        @Override
        public void visit(PrefixTreatAsWithdraw prefixTreatAsWithdraw) {
            fail("Wrong Metric PrefixTreatAsWithdraw");
        }

        @Override
        public void visit(UpdateTreatAsWithdraw updateTreatAsWithdraw) {
            fail("Wrong Metric UpdateTreatAsWithdraw");
        }

        @Override
        public void visit(LocRib locRib) {
            fail("Wrong Metric LocRib");
        }

        @Override
        public void visit(DuplicateUpdate duplicateUpdate) {
            fail("Wrong Metric DuplicateUpdate");
        }

        @Override
        public void visit(Rejected rejected) {
            fail("Wrong Metric Rejected");
        }
    }

    private final static Path FOLDER = Paths.get("src/test/resources");

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"init.raw", InitiationPacket.class},
                {"peer_down.raw", PeerDownPacket.class},
                {"peer_up.raw", PeerUpPacket.class},
                {"route_monitoring_reachable.raw", RouteMonitoringPacket.class},
                {"route_monitoring_withdraw.raw", RouteMonitoringPacket.class},
                {"statistics.raw", StatisticsReportPacket.class}
        });
    }

    private final String file;
    private final Class<Packet> clazz;

    public BlackboxTest(final String file, final Class<Packet> clazz) {
        this.file = file;
        this.clazz = clazz;
    }

    @Test
    public void testFiles() throws Exception {
        try (final FileChannel channel = FileChannel.open(FOLDER.resolve(file))) {
            final ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();

            final Header header = new Header(slice(buffer, Header.SIZE));
            final Packet packet = header.parsePayload(buffer);
            assertThat(packet, isA(clazz));
            assertThat((long) header.length, is(channel.size()));
            packet.accept(this);
        }
    }

    @Override
    public void visit(final InitiationPacket packet) {
        assertThat(packet.information.get(0).type, is(InformationElement.Type.SYS_NAME));
        assertThat(packet.information.get(0).length, is(8));
        assertThat(packet.information.get(0).value, is("Gobgp-R0"));

        assertThat(packet.information.get(1).type, is(InformationElement.Type.SYS_DESCR));
        assertThat(packet.information.get(1).length, is(21));
        assertThat(packet.information.get(1).value, is("Gobgp Version: master"));

        assertThat(packet.information.size(), is(2));
    }

    @Override
    public void visit(final PeerDownPacket packet) {
        assertThat(packet.peerHeader.type, is(PeerHeader.Type.GLOBAL_INSTANCE));
        assertThat(packet.peerHeader.flags.addressVersion, is(PeerFlags.AddressVersion.IP_V4));
        assertThat(packet.peerHeader.flags.postPolicy, is(false));
        assertThat(packet.peerHeader.flags.legacyASPath, is(false));
        assertThat(packet.peerHeader.distinguisher, is(UnsignedLong.ZERO));
        assertThat(packet.peerHeader.address, is(InetAddressUtils.addr("10.0.255.5")));
        assertThat(packet.peerHeader.as, is(64512L));
        assertThat(packet.peerHeader.id, is(3232238085L));
        assertThat(packet.peerHeader.timestamp, is(Instant.ofEpochSecond(1574257076L)));
        assertThat(packet.type, is(PeerDownPacket.Type.REMOTE_BGP_NOTIFICATION));
        packet.reason.accept(new Reason.Visitor(){
            @Override
            public void visit(LocalBgpNotification localNotification) {
                fail("Wrong Reason LocalBgpNotification");
            }

            @Override
            public void visit(LocalNoNotification localNoNotification) {
                fail("Wrong Reason LocalNoNotification");
            }

            @Override
            public void visit(RemoteBgpNotification remoteNotification) {
                assertThat(remoteNotification.notification.header.length, is(21));
                assertThat(remoteNotification.notification.header.type, is(org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Header.Type.NOTIFICATION));
                assertThat(remoteNotification.notification.error, is(NotificationPacket.Error.PEER_DECONFIGURED));
            }

            @Override
            public void visit(RemoteNoNotification remoteNoNotification) {
                fail("Wrong Reason RemoteNoNotification");
            }

            @Override
            public void visit(Unknown unknown) {
                fail("Wrong Reason Unknown");
            }
        });
    }

    @Override
    public void visit(PeerUpPacket packet) {
        assertThat(packet.peerHeader.type, is(PeerHeader.Type.GLOBAL_INSTANCE));
        assertThat(packet.peerHeader.flags.addressVersion, is(PeerFlags.AddressVersion.IP_V4));
        assertThat(packet.peerHeader.flags.postPolicy, is(false));
        assertThat(packet.peerHeader.flags.legacyASPath, is(false));
        assertThat(packet.peerHeader.distinguisher, is(UnsignedLong.ZERO));
        assertThat(packet.peerHeader.address, is(InetAddressUtils.addr("10.0.255.5")));
        assertThat(packet.peerHeader.as, is(64512L));
        assertThat(packet.peerHeader.id, is(3232238085L));
        assertThat(packet.peerHeader.timestamp, is(Instant.ofEpochSecond(1574257049L)));
        assertThat(packet.localAddress, is(InetAddressUtils.addr("10.0.255.7")));
        assertThat(packet.localPort, is(179));
        assertThat(packet.remotePort, is(49103));
        assertThat(packet.sendOpenMessage.header.length, is(45));
        assertThat(packet.sendOpenMessage.header.type, is(org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Header.Type.OPEN));
        assertThat(packet.sendOpenMessage.version, is(4));
        assertThat(packet.sendOpenMessage.as, is(65002));
        assertThat(packet.sendOpenMessage.id, is(3232238087L));
        assertThat(packet.sendOpenMessage.holdTime, is(90));
        assertThat(packet.recvOpenMessage.header.length, is(45));
        assertThat(packet.recvOpenMessage.header.type, is(org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Header.Type.OPEN));
        assertThat(packet.recvOpenMessage.version, is(4));
        assertThat(packet.recvOpenMessage.as, is(64512));
        assertThat(packet.recvOpenMessage.id, is(3232238085L));
        assertThat(packet.recvOpenMessage.holdTime, is(90));
        assertThat(packet.information.size(), is(0));
    }

    @Override
    public void visit(RouteMonitoringPacket packet) {
        assertThat(packet.peerHeader.type, is(PeerHeader.Type.GLOBAL_INSTANCE));
        assertThat(packet.peerHeader.flags.addressVersion, is(PeerFlags.AddressVersion.IP_V4));
        assertThat(packet.peerHeader.flags.postPolicy, is(false));
        assertThat(packet.peerHeader.flags.legacyASPath, is(false));
        assertThat(packet.peerHeader.distinguisher, is(UnsignedLong.ZERO));
        assertThat(packet.peerHeader.address, is(InetAddressUtils.addr("10.0.255.5")));
        assertThat(packet.peerHeader.as, is(64512L));
        assertThat(packet.peerHeader.id, is(3232238085L));
        assertThat(packet.peerHeader.timestamp, either(is(Instant.ofEpochSecond(1574257996L))).or(is(Instant.ofEpochSecond(1574257061L))));

        assertThat(packet.updateMessage.header.length, either(is(27)).or(is(47)));
        assertThat(packet.updateMessage.header.type, is(org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bgp.Header.Type.UPDATE));

        if (!packet.updateMessage.withdrawRoutes.isEmpty()) {
            assertThat(packet.updateMessage.reachableRoutes, is(empty()));
            assertThat(packet.updateMessage.withdrawRoutes.get(0).length, is(24));
            assertThat(packet.updateMessage.withdrawRoutes.get(0).prefix, is(InetAddressUtils.addr("192.168.254.0")));
        } else {
            assertThat(packet.updateMessage.withdrawRoutes, is(empty()));
            assertThat(packet.updateMessage.reachableRoutes.get(0).length, is(24));
            assertThat(packet.updateMessage.reachableRoutes.get(0).prefix, is(InetAddressUtils.addr("192.168.255.0")));
        }

        assertThat(packet.updateMessage.pathAttributes.size(), either(is(0)).or(is(3)));

        if (packet.updateMessage.pathAttributes.size() > 0) {
            assertThat(packet.updateMessage.pathAttributes.get(0).optional, is(false));
            assertThat(packet.updateMessage.pathAttributes.get(0).transitive, is(true));
            assertThat(packet.updateMessage.pathAttributes.get(0).partial, is(false));
            assertThat(packet.updateMessage.pathAttributes.get(0).extended, is(false));
            assertThat(packet.updateMessage.pathAttributes.get(0).type, is(UpdatePacket.PathAttribute.Type.ORIGIN));
            assertThat(packet.updateMessage.pathAttributes.get(0).length, is(1));
            packet.updateMessage.pathAttributes.get(0).attribute.accept(new AttributeVisitorAdapter() {
                @Override
                public void visit(Origin origin) {
                    assertThat(origin.value, is(Origin.Value.INCOMPLETE));
                }
            });

            assertThat(packet.updateMessage.pathAttributes.get(1).optional, is(false));
            assertThat(packet.updateMessage.pathAttributes.get(1).transitive, is(true));
            assertThat(packet.updateMessage.pathAttributes.get(1).partial, is(false));
            assertThat(packet.updateMessage.pathAttributes.get(1).extended, is(false));
            assertThat(packet.updateMessage.pathAttributes.get(1).type, is(UpdatePacket.PathAttribute.Type.AS_PATH));
            assertThat(packet.updateMessage.pathAttributes.get(1).length, is(6));
            packet.updateMessage.pathAttributes.get(1).attribute.accept(new AttributeVisitorAdapter() {
                @Override
                public void visit(AsPath asPath) {
                    assertThat(asPath.segments.size(), is(1));
                    assertThat(asPath.segments.get(0).type, is(AsPath.Segment.Type.AS_SEQUENCE));
                    assertThat(asPath.segments.get(0).path, hasItem(64512L));
                }
            });

            assertThat(packet.updateMessage.pathAttributes.get(2).optional, is(false));
            assertThat(packet.updateMessage.pathAttributes.get(2).transitive, is(true));
            assertThat(packet.updateMessage.pathAttributes.get(2).partial, is(false));
            assertThat(packet.updateMessage.pathAttributes.get(2).extended, is(false));
            assertThat(packet.updateMessage.pathAttributes.get(2).type, is(UpdatePacket.PathAttribute.Type.NEXT_HOP));
            assertThat(packet.updateMessage.pathAttributes.get(2).length, is(4));
            packet.updateMessage.pathAttributes.get(2).attribute.accept(new AttributeVisitorAdapter() {
                @Override
                public void visit(NextHop nextHop) {
                    assertThat(nextHop.address, is(InetAddressUtils.addr("10.0.255.5")));
                }
            });
        }
    }

    @Override
    public void visit(StatisticsReportPacket packet) {
        assertThat(packet.peerHeader.type, is(PeerHeader.Type.GLOBAL_INSTANCE));
        assertThat(packet.peerHeader.flags.addressVersion, is(PeerFlags.AddressVersion.IP_V4));
        assertThat(packet.peerHeader.flags.postPolicy, is(false));
        assertThat(packet.peerHeader.flags.legacyASPath, is(false));
        assertThat(packet.peerHeader.distinguisher, is(UnsignedLong.ZERO));
        assertThat(packet.peerHeader.address, is(InetAddressUtils.addr("10.0.255.5")));
        assertThat(packet.peerHeader.as, is(64512L));
        assertThat(packet.peerHeader.id, is(3232238085L));
        assertThat(packet.peerHeader.timestamp, is(Instant.ofEpochSecond(1574257732L)));

        assertThat(packet.statistics.size(), is(4));
        assertThat(packet.statistics.get(0).length, is(8));
        assertThat(packet.statistics.get(0).type, is(StatisticsReportPacket.Element.Type.ADJ_RIB_IN));
        packet.statistics.get(0).value.accept(new MetricVisitorAdapter() {
            @Override
            public void visit(AdjRibIn adjRibIn) {
                assertThat(adjRibIn.gauge, is(UnsignedLong.ONE));
            }
        });
        assertThat(packet.statistics.get(1).length, is(8));
        assertThat(packet.statistics.get(1).type, is(StatisticsReportPacket.Element.Type.LOC_RIB));
        packet.statistics.get(1).value.accept(new MetricVisitorAdapter() {
            @Override
            public void visit(LocRib locRib) {
                assertThat(locRib.gauge, is(UnsignedLong.ONE));
            }
        });
        assertThat(packet.statistics.get(2).length, is(4));
        assertThat(packet.statistics.get(2).type, is(StatisticsReportPacket.Element.Type.UPDATE_TREAT_AS_WITHDRAW));
        packet.statistics.get(2).value.accept(new MetricVisitorAdapter() {
            @Override
            public void visit(UpdateTreatAsWithdraw updateTreatAsWithdraw) {
                assertThat(updateTreatAsWithdraw.counter, is(0L));
            }
        });
        assertThat(packet.statistics.get(3).length, is(4));
        assertThat(packet.statistics.get(3).type, is(StatisticsReportPacket.Element.Type.PREFIX_TREAT_AS_WITHDRAW));
        packet.statistics.get(3).value.accept(new MetricVisitorAdapter() {
            @Override
            public void visit(PrefixTreatAsWithdraw prefixTreatAsWithdraw) {
                assertThat(prefixTreatAsWithdraw.counter, is(0L));
            }
        });
    }

    @Override
    public void visit(TerminationPacket packet) {
        fail("Wrong Packet TerminationPacket");
    }

    @Override
    public void visit(RouteMirroringPacket packet) {
        fail("Wrong Packet RouteMirroringPacket");
    }
}
