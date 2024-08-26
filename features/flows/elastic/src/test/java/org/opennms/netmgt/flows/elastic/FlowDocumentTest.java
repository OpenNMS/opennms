/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;


import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.opennms.integration.api.v1.flows.Flow.NetflowVersion;
import static org.opennms.integration.api.v1.flows.Flow.Direction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.opennms.core.test.xml.JsonTest;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.opennms.netmgt.telemetry.protocols.common.cache.NodeInfo;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.searchbox.client.AbstractJestClient;

public class FlowDocumentTest {

    private final Gson gson = new GsonBuilder()
            .setDateFormat(AbstractJestClient.ELASTIC_SEARCH_DATE_FORMAT)
            .create();

    @Test
    public void verifyEffectiveDocument() throws IOException {
        final var flow = EnrichedFlow.from(getMockFlow());
        flow.setLocation("SomeLocation");
        flow.setHost("192.168.1.1");
        flow.setFlowLocality(EnrichedFlow.Locality.PRIVATE);
        flow.setSrcLocality(EnrichedFlow.Locality.PRIVATE);
        flow.setDstLocality(EnrichedFlow.Locality.PRIVATE);

        flow.setSrcNodeInfo(new NodeInfo() {{
            this.setNodeId(1);
            this.setInterfaceId(0);
            this.setForeignSource("SomeRequisition");
            this.setForeignId("1");
            this.setCategories(List.of("SomeCategory"));
        }});
        flow.setDstNodeInfo(new NodeInfo() {{
            this.setNodeId(2);
            this.setInterfaceId(0);
            this.setForeignSource("SomeRequisition");
            this.setForeignId("2");
            this.setCategories(List.of("SomeCategory"));
        }});
        flow.setExporterNodeInfo(new NodeInfo() {{
            this.setNodeId(3);
            this.setInterfaceId(0);
            this.setForeignSource("SomeRequisition");
            this.setForeignId("3");
            this.setCategories(List.of("SomeCategory"));
        }});

        final FlowDocument document = FlowDocument.from(flow);

        // Serialize
        final String actualJson = gson.toJson(document);

        // Verify
        final String expectedJson = Resources.toString(Resources.getResource("flow-document-netflow5.json"), StandardCharsets.UTF_8);
        JsonTest.assertJsonEquals(expectedJson, actualJson);
    }

    public static Flow getMockFlow() {
        final Flow flow = mock(Flow.class);
        when(flow.getNetflowVersion()).thenReturn(NetflowVersion.V5);
        when(flow.getDirection()).thenReturn(Direction.INGRESS);
        when(flow.getIpProtocolVersion()).thenReturn(4);
        when(flow.getSrcAddr()).thenReturn("192.168.1.2");
        when(flow.getSrcAddrHostname()).thenReturn(Optional.empty());
        when(flow.getDstAddr()).thenReturn("192.168.2.2");
        when(flow.getDstAddrHostname()).thenReturn(Optional.of("four.three.two.one"));
        when(flow.getNextHopHostname()).thenReturn(Optional.empty());
        when(flow.getVlan()).thenReturn(null);
        return flow;
    }
}
