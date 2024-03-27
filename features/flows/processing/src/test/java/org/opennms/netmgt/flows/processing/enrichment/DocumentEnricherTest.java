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
package org.opennms.netmgt.flows.processing.enrichment;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.opennms.integration.api.v1.flows.Flow.Direction;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.IpAddr;
import org.opennms.netmgt.flows.processing.impl.DocumentEnricherImpl;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.collect.Lists;
import com.spotify.hamcrest.pojo.IsPojo;

public class DocumentEnricherTest {

    @Test
    public void verifyCacheUsage() throws InterruptedException {
        final MockDocumentEnricherFactory factory = new MockDocumentEnricherFactory();
        final DocumentEnricherImpl enricher = factory.getEnricher();
        final NodeDao nodeDao = factory.getNodeDao();
        final InterfaceToNodeCache interfaceToNodeCache = factory.getInterfaceToNodeCache();
        final AtomicInteger nodeDaoGetCounter = factory.getNodeDaoGetCounter();

        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.1"), 1);
        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.2"), 2);
        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.3"), 3);

        nodeDao.save(createOnmsNode(1, "my-requisition"));
        nodeDao.save(createOnmsNode(2, "my-requisition"));
        nodeDao.save(createOnmsNode(3, "my-requisition"));

        final List<Flow> documents = Lists.newArrayList();
        documents.add(createFlowDocument("10.0.0.1", "10.0.0.2"));
        documents.add(createFlowDocument("10.0.0.1", "10.0.0.3"));
        enricher.enrich(documents, new FlowSource("Default", "127.0.0.1", null));

        // get is also called for each save, so we account for those as well
        Assert.assertEquals(6, nodeDaoGetCounter.get());

        // Try to enrich flow documents to existing IpAddresses.
        documents.clear();
        documents.add(createFlowDocument("10.0.0.2", "10.0.0.3"));
        enricher.enrich(documents, new FlowSource("Default", "127.0.0.1", null));
        // Since above two addresses are cached, no extra calls to nodeDao.
        Assert.assertEquals(6, nodeDaoGetCounter.get());

        // Add two more interfaces to the system.
        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.4"), 2);
        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.5"), 3);
        documents.add(createFlowDocument("10.0.0.4", "10.0.0.5"));
        enricher.enrich(documents, new FlowSource("Default", "127.0.0.1", null));
        // Since above two addresses are added to same nodes, no extra calls to nodeDao
        Assert.assertEquals(6, nodeDaoGetCounter.get());
    }

    private static Flow createFlowDocument(String sourceIp, String destIp) {
        return createFlowDocument(sourceIp, destIp, 0);
    }

    private static Flow createFlowDocument(String sourceIp, String destIp, final long timeOffset) {
        final var now = Instant.now();

        final Flow flow = mock(Flow.class);
        when(flow.getReceivedAt()).thenReturn(now.plus(timeOffset, ChronoUnit.MILLIS));
        when(flow.getTimestamp()).thenReturn(now);
        when(flow.getFirstSwitched()).thenReturn(now.minus(20_000L, ChronoUnit.MILLIS));
        when(flow.getDeltaSwitched()).thenReturn(now.minus(10_000L, ChronoUnit.MILLIS));
        when(flow.getLastSwitched()).thenReturn(now.minus(5_000L, ChronoUnit.MILLIS));
        when(flow.getSrcAddr()).thenReturn(sourceIp);
        when(flow.getSrcPort()).thenReturn(510);
        when(flow.getDstAddr()).thenReturn(destIp);
        when(flow.getDstPort()).thenReturn(80);
        when(flow.getProtocol()).thenReturn(6); // TCP

        return flow;
    }

    private static OnmsNode createOnmsNode(int nodeId, String foreignSource) {
        final OnmsNode node = new OnmsNode();
        node.setId(nodeId);
        node.setForeignSource(foreignSource);
        node.setForeignId(nodeId + "");
        return node;
    }

    @Test
    public void testCreateClassificationRequest() throws InterruptedException {
        final MockDocumentEnricherFactory factory = new MockDocumentEnricherFactory();
        final DocumentEnricherImpl enricher = factory.getEnricher();

        final EnrichedFlow flowDocument = new EnrichedFlow();

        // verify that null values are handled correctly, see issue HZN-1329
        ClassificationRequest classificationRequest;

        classificationRequest = enricher.createClassificationRequest(flowDocument);
        Assert.assertEquals(false, classificationRequest.isClassifiable());

        flowDocument.setDstPort(123);

        classificationRequest = enricher.createClassificationRequest(flowDocument);
        Assert.assertEquals(false, classificationRequest.isClassifiable());

        flowDocument.setSrcPort(456);

        classificationRequest = enricher.createClassificationRequest(flowDocument);
        Assert.assertEquals(false, classificationRequest.isClassifiable());

        flowDocument.setProtocol(6);

        classificationRequest = enricher.createClassificationRequest(flowDocument);
        Assert.assertEquals(true, classificationRequest.isClassifiable());
    }

    @Test
    public void testDirection() throws InterruptedException {
        final MockDocumentEnricherFactory factory = new MockDocumentEnricherFactory();
        final DocumentEnricherImpl enricher = factory.getEnricher();

        final EnrichedFlow d1 = new EnrichedFlow();
        d1.setSrcAddr("1.1.1.1");
        d1.setSrcPort(1);
        d1.setDstAddr("2.2.2.2");
        d1.setDstPort(2);
        d1.setProtocol(6);
        d1.setDirection(Direction.INGRESS);

        final ClassificationRequest c1 = enricher.createClassificationRequest(d1);
        Assert.assertEquals(IpAddr.of("1.1.1.1"), c1.getSrcAddress());
        Assert.assertEquals(IpAddr.of("2.2.2.2"), c1.getDstAddress());
        Assert.assertEquals(Integer.valueOf(1), c1.getSrcPort());
        Assert.assertEquals(Integer.valueOf(2), c1.getDstPort());

        final EnrichedFlow d2 = new EnrichedFlow();
        d2.setSrcAddr("1.1.1.1");
        d2.setSrcPort(1);
        d2.setDstAddr("2.2.2.2");
        d2.setDstPort(2);
        d2.setProtocol(6);
        d2.setDirection(Direction.EGRESS);

        // check that fields stay as theay are even when EGRESS is used
        final ClassificationRequest c2 = enricher.createClassificationRequest(d2);
        Assert.assertEquals(IpAddr.of("1.1.1.1"), c2.getSrcAddress());
        Assert.assertEquals(IpAddr.of("2.2.2.2"), c2.getDstAddress());
        Assert.assertEquals(Integer.valueOf(1), c2.getSrcPort());
        Assert.assertEquals(Integer.valueOf(2), c2.getDstPort());

        final EnrichedFlow d3 = new EnrichedFlow();
        d3.setSrcAddr("1.1.1.1");
        d3.setSrcPort(1);
        d3.setDstAddr("2.2.2.2");
        d3.setDstPort(2);
        d3.setProtocol(6);
        d3.setDirection(null);

        final ClassificationRequest c3 = enricher.createClassificationRequest(d3);
        Assert.assertEquals(IpAddr.of("1.1.1.1"), c3.getSrcAddress());
        Assert.assertEquals(IpAddr.of("2.2.2.2"), c3.getDstAddress());
        Assert.assertEquals(Integer.valueOf(1), c3.getSrcPort());
        Assert.assertEquals(Integer.valueOf(2), c3.getDstPort());
    }

    @Test
    public void testClockCorrection() throws InterruptedException {
        final MockDocumentEnricherFactory factory = new MockDocumentEnricherFactory(2400_000L);
        final DocumentEnricherImpl enricher = factory.getEnricher();

        final Flow flow1 = createFlowDocument("10.0.0.1", "10.0.0.3");
        final Flow flow2 = createFlowDocument("10.0.0.1", "10.0.0.3", -3600_000L);
        final Flow flow3 = createFlowDocument("10.0.0.1", "10.0.0.3", +3600_000L);

        final List<Flow> flows = Lists.newArrayList(flow1, flow2, flow3);

        final List<EnrichedFlow> docs = enricher.enrich(flows, new FlowSource("Default", "127.0.0.1", null));

        Assert.assertThat(docs, Matchers.contains(
                IsPojo.pojo(EnrichedFlow.class)
                      .where(EnrichedFlow::getTimestamp, Matchers.is(flow1.getTimestamp()))
                      .where(EnrichedFlow::getFirstSwitched, Matchers.is(flow1.getFirstSwitched()))
                      .where(EnrichedFlow::getDeltaSwitched, Matchers.is(flow1.getDeltaSwitched()))
                      .where(EnrichedFlow::getLastSwitched, Matchers.is(flow1.getLastSwitched())),
                IsPojo.pojo(EnrichedFlow.class)
                      .where(EnrichedFlow::getTimestamp, Matchers.is(flow2.getTimestamp().minus( 3600_000L, ChronoUnit.MILLIS)))
                      .where(EnrichedFlow::getFirstSwitched, Matchers.is(flow2.getFirstSwitched().minus(3600_000L, ChronoUnit.MILLIS)))
                      .where(EnrichedFlow::getDeltaSwitched, Matchers.is(flow2.getDeltaSwitched().minus(3600_000L, ChronoUnit.MILLIS)))
                      .where(EnrichedFlow::getLastSwitched, Matchers.is(flow2.getLastSwitched().minus(3600_000L, ChronoUnit.MILLIS))),
                IsPojo.pojo(EnrichedFlow.class)
                      .where(EnrichedFlow::getTimestamp, Matchers.is(flow3.getTimestamp().plus(3600_000L, ChronoUnit.MILLIS)))
                      .where(EnrichedFlow::getFirstSwitched, Matchers.is(flow3.getFirstSwitched().plus(3600_000L, ChronoUnit.MILLIS)))
                      .where(EnrichedFlow::getDeltaSwitched, Matchers.is(flow3.getDeltaSwitched().plus(3600_000L, ChronoUnit.MILLIS)))
                      .where(EnrichedFlow::getLastSwitched, Matchers.is(flow3.getLastSwitched().plus(3600_000L, ChronoUnit.MILLIS)))));
    }
}
