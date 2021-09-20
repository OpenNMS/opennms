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

package org.opennms.netmgt.telemetry.itests;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser.address;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAgentFactory;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpPeerStatusAdapter;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpTelemetryAdapter;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;
import org.opennms.netmgt.telemetry.protocols.collection.CollectionSetWithAgent;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

import com.codahale.metrics.MetricRegistry;
import com.google.common.primitives.UnsignedInteger;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-collectionAgentFactory.xml",
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class BmpAdapterIT {
    @Autowired
    InterfaceToNodeCache interfaceToNodeCache;

    @Autowired
    DatabasePopulator databasePopulator;

    @Autowired
    NodeDao nodeDao;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    CollectionAgentFactory collectionAgentFactory;

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
        this.databasePopulator.populateDatabase();
        this.interfaceToNodeCache.dataSourceSync();

        // add metadata for nodeId 2
        final OnmsNode onmsNode = databasePopulator.getNode2();
        onmsNode.addMetaData("myContext", "myKey", "10.123.123.123");
        databasePopulator.getNodeDao().update(onmsNode);
    }

    /**
     * Exporter's nodeId is 2
     * Exporter's metaData myContext:myKey is set to 10.123.123.123
     *
     * Message's nat'd endpoint address is 10.10.10.10
     * Message's bgpIp is 10.123.123.123
     *
     * ...this should result in the node's primary interface address 192.168.2.1 since 10.123.123.123 does not exists in database
     */
    @JUnitTemporaryDatabase
    @Test
    public void testTelemetryAdapter() {
        final AdapterDefinition adapterDef = mock(AdapterDefinition.class);
        final MetricRegistry metricRegistry = mock(MetricRegistry.class);
        final BmpTelemetryAdapter adapter = new BmpTelemetryAdapter(adapterDef, metricRegistry, nodeDao, transactionTemplate);
        adapter.setMetaDataNodeLookup("myContext:myKey");
        adapter.setInterfaceToNodeCache(interfaceToNodeCache);
        adapter.setCollectionAgentFactory(collectionAgentFactory);

        final Transport.StatisticsReportPacket.Builder statisticsReport = Transport.StatisticsReportPacket.newBuilder()
                .setAdjRibIn(Transport.StatisticsReportPacket.Gauge.newBuilder()
                        .setValue(1))
                .setAdjRibOut(Transport.StatisticsReportPacket.Gauge
                        .newBuilder()
                        .setValue(1))
                .setPeer(Transport.Peer.newBuilder()
                        .setAddress(address(InetAddressUtils.addr("20.20.20.20")))
                        .setId(address(InetAddressUtils.addr("20.20.20.20"))));

        final Transport.Message statisticsMessage = Transport.Message.newBuilder()
                .setVersion(3)
                .setStatisticsReport(statisticsReport)
                .setBgpId(address(InetAddressUtils.addr("10.123.123.123")))
                .build();

        final TelemetryMessageLog messageLog = mock(TelemetryMessageLog.class);

        when(messageLog.getSystemId()).thenReturn("0xDEADBEEF");
        when(messageLog.getSourceAddress()).thenReturn("10.10.10.10");
        when(messageLog.getSourcePort()).thenReturn(666);

        final TelemetryMessageLogEntry messageLogEntry = mock(TelemetryMessageLogEntry.class);
        when(messageLogEntry.getByteArray()).thenReturn(statisticsMessage.toByteArray());
        when(messageLogEntry.getTimestamp()).thenReturn(1L);
        final List<CollectionSetWithAgent> collectionSetWithAgentStream = adapter.handleCollectionMessage(messageLogEntry, messageLog).collect(Collectors.toList());
        assertThat(collectionSetWithAgentStream.size(), is(1));
        assertThat(collectionSetWithAgentStream.get(0).getAgent().getNodeId(), is(2));
        assertThat(collectionSetWithAgentStream.get(0).getAgent().getAddress(), is(InetAddressUtils.addr("192.168.2.1")));
    }

    /**
     * Exporter's nodeId is 2
     * Exporter's metaData myContext:myKey is set to 10.123.123.123
     *
     * Message's nat'd endpoint address is 10.10.10.10
     * Message's bgpIp is 10.123.123.123
     * Peer addresses in peerUp packet doesn't matter
     *
     * ...this should result in an event containing nodeId 2
     */
    @JUnitTemporaryDatabase
    @Test
    public void testStatusAdapter() {
        final AdapterDefinition adapterDef = mock(AdapterDefinition.class);
        final MetricRegistry metricRegistry = mock(MetricRegistry.class);
        final EventForwarder eventForwarder = mock(EventForwarder.class);

        final BmpPeerStatusAdapter adapter = new BmpPeerStatusAdapter(adapterDef, interfaceToNodeCache, eventForwarder, metricRegistry, nodeDao);
        adapter.setMetaDataNodeLookup("myContext:myKey");

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
                .setSysDesc("Black Ops IV")
                .setMessage("Gun Game!");

        Transport.Message peerUpMessage = Transport.Message.newBuilder()
                .setVersion(3)
                .setPeerUp(peerUpPacket)
                .setBgpId(address(InetAddressUtils.addr("10.123.123.123")))
                .build();

        final TelemetryMessageLog messageLog = mock(TelemetryMessageLog.class);
        when(messageLog.getSystemId()).thenReturn("0xDEADBEEF");
        when(messageLog.getSourceAddress()).thenReturn("10.10.10.10");
        when(messageLog.getSourcePort()).thenReturn(666);

        final TelemetryMessageLogEntry messageLogEntry = mock(TelemetryMessageLogEntry.class);
        when(messageLogEntry.getByteArray()).thenReturn(peerUpMessage.toByteArray());
        when(messageLogEntry.getTimestamp()).thenReturn(1L);
        adapter.handleMessage(messageLogEntry, messageLog);

        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventForwarder).sendNow(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getNodeid(), is(2L));
    }
}
