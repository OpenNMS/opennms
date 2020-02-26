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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
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
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Record;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Type;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.BaseAttribute;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.records.Router;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;

import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.ByteString;

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
    public void canGenerateRouterMessages() {
        // Send an initiation packet
        Transport.InitiationPacket.Builder initiationPacket = Transport.InitiationPacket.newBuilder()
                .setSysName("router1");
        Transport.Message message = Transport.Message.newBuilder()
                .setVersion(1)
                .setInitiation(initiationPacket)
                .build();
        send(message.toByteString());

        // Grab the generated "router" messages
        List<Router> routerMsgs = getHandledRecordsOfType(Type.ROUTER);
        assertThat(routerMsgs, hasSize(1));

        // Verify
        Router router = routerMsgs.get(0);
        assertThat(router.sequence, equalTo(0L));
        assertThat(router.name, equalTo("router1"));
    }

    @Test
    public void canGenerateBaseAttributeMessages() {
        // Send a route monitoring packet
        Transport.RouteMonitoringPacket.Builder updatePacket = Transport.RouteMonitoringPacket.newBuilder()
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
                                        .addPaths(64514))));
        Transport.Message message = Transport.Message.newBuilder()
                .setVersion(1)
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
    public void handle(Message message) {
        messagesHandled.add(message);
    }

    @Override
    public void close() {
        // pass
    }

    // FIXME: Dedup
    private static Transport.IpAddress address(final InetAddress address) {
        final Transport.IpAddress.Builder builder = Transport.IpAddress.newBuilder();
        if (address instanceof Inet4Address) {
            builder.setV4(ByteString.copyFrom(address.getAddress()));
        } else if (address instanceof Inet6Address) {
            builder.setV6(ByteString.copyFrom(address.getAddress()));
        } else {
            throw new IllegalStateException();
        }

        return builder.build();
    }
}
