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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser.address;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Message;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.BaseAttribute;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Collector;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Peer;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Router;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;

import com.codahale.metrics.MetricRegistry;
import com.google.common.primitives.UnsignedInteger;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

/**
 * These tests drive the BmpIntegrationAdapter with mocked messages
 * (as though they originated from the BmpParser) and captures the
 * generated OpenBMP messages for validation.
 */
public class BmpIntegrationAdapterTest implements BmpMessageHandler {

    private BmpIntegrationAdapter adapter;
    private List<Message> messagesHandled = new LinkedList<>();

    @Before
    public void setUp() {
        AdapterDefinition adapterDef = mock(AdapterDefinition.class);
        MetricRegistry metricRegistry = mock(MetricRegistry.class);
        adapter = new BmpIntegrationAdapter(adapterDef, metricRegistry, this);
    }

    @Test
    public void canGenerateCollectorMessages() {
        final Transport.Heartbeat.Builder heartbeat = Transport.Heartbeat.newBuilder()
                                                                         .setMode(Transport.Heartbeat.Mode.CHANGE);
        heartbeat.addRoutersBuilder().setV4(ByteString.copyFrom(InetAddressUtils.toIpAddrBytes("1.2.3.4")));
        heartbeat.addRoutersBuilder().setV4(ByteString.copyFrom(InetAddressUtils.toIpAddrBytes("5.6.7.8")));
        heartbeat.addRoutersBuilder().setV6(ByteString.copyFrom(InetAddressUtils.toIpAddrBytes("2001:0db8:85a3::8a2e:0370:7334")));

        Transport.Message message = Transport.Message.newBuilder()
                                                     .setVersion(3)
                                                     .setHeartbeat(heartbeat)
                                                     .build();

        send(message.toByteString());

        List<Collector> collectorMessages = getHandledRecordsOfType(Type.COLLECTOR);
        assertThat(collectorMessages, hasSize(1));
        assertThat(collectorMessages.get(0).adminId, is("0xDEADBEEF"));
        assertThat(collectorMessages.get(0).action, is(Collector.Action.CHANGE));
        assertThat(collectorMessages.get(0).routers, contains(InetAddressUtils.addr("1.2.3.4"),
                                                              InetAddressUtils.addr("5.6.7.8"),
                                                              InetAddressUtils.addr("2001:0db8:85a3::8a2e:0370:7334")));
    }

    @Test
    public void canGenerateRouterMessages() {
        // Send an initiation packet
        final Transport.InitiationPacket.Builder initiationPacket = Transport.InitiationPacket.newBuilder()
                .setSysName("router1")
                .addSysDesc("description1")
                .setBgpId(address(InetAddressUtils.addr("10.1.1.1")));

        final Transport.Message initiationMessage = Transport.Message.newBuilder()
                .setVersion(3)
                .setInitiation(initiationPacket)
                .build();
        send(initiationMessage.toByteString());

        // Send an termination packet
        final Transport.TerminationPacket.Builder terminationPacket = Transport.TerminationPacket.newBuilder()
                .setReason(2)
                .addMessage("message");

        final Transport.Message terminationMessage = Transport.Message.newBuilder()
                .setVersion(3)
                .setTermination(terminationPacket)
                .build();
        send(terminationMessage.toByteString());

        // Grab the generated "router" messages
        final List<Router> routerMsgs = getHandledRecordsOfType(Type.ROUTER);
        assertThat(routerMsgs, hasSize(2));

        // Verify initiation message
        final Router router1 = routerMsgs.get(0);
        assertThat(router1.sequence, equalTo(0L));
        assertThat(router1.name, equalTo("router1"));
        assertThat(router1.description, equalTo("description1"));
        assertThat(router1.bgpId, equalTo(InetAddressUtils.addr("10.1.1.1")));

        // Verify termination message
        final Router router2 = routerMsgs.get(1);
        assertThat(router2.sequence, equalTo(1L));
        assertThat(router2.termCode, equalTo(2));
        assertThat(router2.termReason, equalTo("Out of resources. The router has exhausted resources available for the BMP session"));
    }

    @Test
    public void canGeneratePeerUpMessages() {
        final Transport.PeerUpPacket.Builder peerUpPacket = Transport.PeerUpPacket.newBuilder();
        peerUpPacket.getPeerBuilder()
                    .setType(Transport.Peer.Type.GLOBAL_INSTANCE)
                    .setPeerFlags(Transport.Peer.PeerFlags.newBuilder()
                                                  .setIpVersion(Transport.Peer.PeerFlags.IpVersion.IP_V4)
                                                  .setLegacyAsPath(false)
                                                  .setPolicy(Transport.Peer.PeerFlags.Policy.PRE_POLICY)
                                                  .build())
                    .setDistinguisher(0)
                    .setAddress(Transport.IpAddress.newBuilder()
                                                   .setV4(ByteString.copyFrom(InetAddressUtils.toIpAddrBytes("192.168.0.5")))
                                                   .build())
                    .setAs(UnsignedInteger.valueOf(4200000000L).intValue())
                    .setId(Transport.IpAddress.newBuilder()
                                              .setV4(ByteString.copyFrom(InetAddressUtils.toIpAddrBytes("9.9.9.9")))
                                              .build())
                    .setTimestamp(Timestamp.newBuilder()
                                           .setSeconds(1234567890L)
                                           .setNanos(987654321)
                                           .build());
        peerUpPacket.setLocalAddress(Transport.IpAddress.newBuilder()
                                                        .setV4(ByteString.copyFrom(InetAddressUtils.toIpAddrBytes("192.168.0.4")))
                                                        .build())
                    .setLocalPort(179)
                    .setRemotePort(117799);
        peerUpPacket.getSendMsgBuilder()
                    .setVersion(4)
                    .setAs(UnsignedInteger.valueOf(4200000023L).intValue())
                    .setHoldTime(200)
                    .setId(Transport.IpAddress.newBuilder()
                                              .setV4(ByteString.copyFrom(InetAddressUtils.toIpAddrBytes("1.1.1.1")))
                                              .build());
        peerUpPacket.getRecvMsgBuilder()
                    .setVersion(4)
                    .setAs(UnsignedInteger.valueOf(4200000000L).intValue())
                    .setHoldTime(100)
                    .setId(Transport.IpAddress.newBuilder()
                                              .setV4(ByteString.copyFrom(InetAddressUtils.toIpAddrBytes("9.9.9.9")))
                                              .build());
        peerUpPacket.setSysName("router1")
                    .setSysDesc("My little router")
                    .setMessage("No router - no cry!");

        Transport.Message peerUpMessage = Transport.Message.newBuilder()
                                                     .setVersion(3)
                                                     .setPeerUp(peerUpPacket)
                                                     .build();
        send(peerUpMessage.toByteString());

        List<Peer> peerMessages = getHandledRecordsOfType(Type.PEER);
        assertThat(peerMessages, hasSize(1));

        // Verify
        Peer peer = peerMessages.get(0);
        assertThat(peer.action, equalTo(Peer.Action.UP));
        assertThat(peer.sequence, equalTo(0L));
        assertThat(peer.name, equalTo("192.168.0.5"));

        assertThat(peer.remoteBgpId, is(InetAddressUtils.addr("9.9.9.9")));
        assertThat(peer.routerIp, is(InetAddressUtils.addr("10.10.10.10")));
        assertThat(peer.timestamp, is(Instant.ofEpochSecond(1234567890L, 987654321)));
        assertThat(peer.remoteAsn, is(4200000000L));
        assertThat(peer.remoteIp, is(InetAddressUtils.addr("192.168.0.5")));
        assertThat(peer.peerRd, is("0:0"));
        assertThat(peer.remotePort, is(117799));
        assertThat(peer.localAsn, is(4200000023L));
        assertThat(peer.localIp, is(InetAddressUtils.addr("192.168.0.4")));
        assertThat(peer.localPort, is(179));
        assertThat(peer.localBgpId, is(InetAddressUtils.addr("1.1.1.1")));
        assertThat(peer.infoData, is("No router - no cry!"));
        assertThat(peer.advertisedCapabilities, is(""));
        assertThat(peer.receivedCapabilities, is(""));
        assertThat(peer.remoteHolddown, is(100L));
        assertThat(peer.advertisedHolddown, is(200L));
        assertThat(peer.bmpReason, is(nullValue()));
        assertThat(peer.bgpErrorCode, is(nullValue()));
        assertThat(peer.bgpErrorSubcode, is(nullValue()));
        assertThat(peer.errorText, is(nullValue()));
        assertThat(peer.l3vpn, is(false));
        assertThat(peer.prePolicy, is(true));
        assertThat(peer.ipv4, is(true));
        assertThat(peer.locRib, is(false));
        assertThat(peer.locRibFiltered, is(false));
        assertThat(peer.tableName, is(""));
    }

    @Test
    public void canGeneratePeerDownMessages() {
        final Transport.PeerDownPacket.Builder peerDownPacket = Transport.PeerDownPacket.newBuilder();
        peerDownPacket.getPeerBuilder()
                    .setType(Transport.Peer.Type.GLOBAL_INSTANCE)
                    .setPeerFlags(Transport.Peer.PeerFlags.newBuilder()
                                                  .setIpVersion(Transport.Peer.PeerFlags.IpVersion.IP_V4)
                                                  .setLegacyAsPath(false)
                                                  .setPolicy(Transport.Peer.PeerFlags.Policy.PRE_POLICY)
                                                  .build())
                    .setDistinguisher(0)
                    .setAddress(Transport.IpAddress.newBuilder()
                                                   .setV4(ByteString.copyFrom(InetAddressUtils.toIpAddrBytes("192.168.0.5")))
                                                   .build())
                    .setAs(UnsignedInteger.valueOf(4200000000L).intValue())
                    .setId(Transport.IpAddress.newBuilder()
                                              .setV4(ByteString.copyFrom(InetAddressUtils.toIpAddrBytes("9.9.9.9")))
                                              .build())
                    .setTimestamp(Timestamp.newBuilder()
                                           .setSeconds(1234567890L)
                                           .setNanos(987654321)
                                           .build());
        peerDownPacket.getRemoteBgpNotificationBuilder()
                      .setCode(1)
                      .setSubcode(2);

        Transport.Message peerDownMessage = Transport.Message.newBuilder()
                                                           .setVersion(3)
                                                           .setPeerDown(peerDownPacket)
                                                           .build();
        send(peerDownMessage.toByteString());

        List<Peer> peerMessages = getHandledRecordsOfType(Type.PEER);
        assertThat(peerMessages, hasSize(1));

        // Verify
        Peer peer = peerMessages.get(0);
        assertThat(peer.action, equalTo(Peer.Action.DOWN));
        assertThat(peer.sequence, equalTo(0L));
        assertThat(peer.name, equalTo("192.168.0.5"));

        assertThat(peer.remoteBgpId, is(InetAddressUtils.addr("9.9.9.9")));
        assertThat(peer.routerIp, is(InetAddressUtils.addr("10.10.10.10")));
        assertThat(peer.timestamp, is(Instant.ofEpochSecond(1234567890L, 987654321)));
        assertThat(peer.remoteAsn, is(4200000000L));
        assertThat(peer.remoteIp, is(InetAddressUtils.addr("192.168.0.5")));
        assertThat(peer.peerRd, is("0:0"));
        assertThat(peer.remotePort, is(nullValue()));
        assertThat(peer.localAsn, is(nullValue()));
        assertThat(peer.localIp, is(nullValue()));
        assertThat(peer.localPort, is(nullValue()));
        assertThat(peer.localBgpId, is(nullValue()));
        assertThat(peer.infoData, is(nullValue()));
        assertThat(peer.advertisedCapabilities, is(nullValue()));
        assertThat(peer.receivedCapabilities, is(nullValue()));
        assertThat(peer.remoteHolddown, is(nullValue()));
        assertThat(peer.advertisedHolddown, is(nullValue()));
        assertThat(peer.bmpReason, is(3));
        assertThat(peer.bgpErrorCode, is(1));
        assertThat(peer.bgpErrorSubcode, is(2));
        assertThat(peer.errorText, is("Bad message header length"));
        assertThat(peer.l3vpn, is(false));
        assertThat(peer.prePolicy, is(true));
        assertThat(peer.ipv4, is(true));
        assertThat(peer.locRib, is(false));
        assertThat(peer.locRibFiltered, is(false));
        assertThat(peer.tableName, is(""));
    }

    @Test
    public void canGenerateBaseAttributeMessages() {
        // Send a route monitoring packet
        Transport.RouteMonitoringPacket.Builder updatePacket = Transport.RouteMonitoringPacket.newBuilder()
                .setPeer(Transport.Peer.newBuilder()
                    .setAddress(address(InetAddressUtils.addr("172.23.1.1"))))
                .addReachables(Transport.RouteMonitoringPacket.Route.newBuilder()
                        .setPrefix(address(InetAddressUtils.addr("10.1.1.0")))
                        .setLength(28))
                .addAttributes(Transport.RouteMonitoringPacket.PathAttribute.newBuilder()
                        .setAsPath(Transport.RouteMonitoringPacket.PathAttribute.AsPath.newBuilder()
                                .addSegments(Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.newBuilder()
                                        .setType(Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.Type.AS_SEQUENCE)
                                        .addPaths(64512)
                                        .addPaths(64513))
                                .addSegments(Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.newBuilder()
                                        .setType(Transport.RouteMonitoringPacket.PathAttribute.AsPath.Segment.Type.AS_SET)
                                        .addPaths(64514))))
                .addAttributes(Transport.RouteMonitoringPacket.PathAttribute.newBuilder()
                        .setCommunity(0x02B202B3))
                .addAttributes(Transport.RouteMonitoringPacket.PathAttribute.newBuilder()
                        .setCommunity(0x02B2FFFF));
        Transport.Message message = Transport.Message.newBuilder()
                .setVersion(3)
                .setRouteMonitoring(updatePacket)
                .build();
        send(message.toByteString());

        // Grab the generated "base_attribute" messages
        List<BaseAttribute> baseAttributeMsgs = getHandledRecordsOfType(Type.BASE_ATTRIBUTE);
        assertThat(baseAttributeMsgs, hasSize(1));

        // Verify
        BaseAttribute baseAttribute = baseAttributeMsgs.get(0);
        assertThat(baseAttribute.sequence, equalTo(0L));
        assertThat(baseAttribute.asPath, equalTo("64512 64513 {64514 }"));
        assertThat(baseAttribute.asPathCount, equalTo(3));
        assertThat(baseAttribute.communityList, equalTo("690:691 690:65535"));
    }

    private <T> List<T> getHandledRecordsOfType(Type type) {
        return messagesHandled.stream()
                .filter(m -> type.equals(m.getType()))
                .flatMap(m -> m.getRecords().stream())
                .map(m -> (T)m)
                .collect(Collectors.toList());
    }

    private void send(ByteString... messages) {
        final TelemetryMessageLog messageLog = mock(TelemetryMessageLog.class);
        when(messageLog.getSystemId()).thenReturn("0xDEADBEEF");
        when(messageLog.getSourceAddress()).thenReturn("10.10.10.10");
        when(messageLog.getSourcePort()).thenReturn(666);

        for (ByteString message : messages) {
            final TelemetryMessageLogEntry messageLogEntry = mock(TelemetryMessageLogEntry.class);
            when(messageLogEntry.getByteArray()).thenReturn(message.toByteArray());
            when(messageLogEntry.getTimestamp()).thenReturn(1L);
            adapter.handleMessage(messageLogEntry, messageLog);
        }
    }

    @Override
    public void handle(Message message, Context context) {
        messagesHandled.add(message);
    }

    @Override
    public void close() {
        // pass
    }
}
