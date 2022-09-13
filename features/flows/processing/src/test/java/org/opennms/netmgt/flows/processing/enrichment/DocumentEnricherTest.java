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
