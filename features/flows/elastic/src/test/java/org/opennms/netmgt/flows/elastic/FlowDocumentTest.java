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
import org.opennms.netmgt.telemetry.protocols.cache.NodeInfo;

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
