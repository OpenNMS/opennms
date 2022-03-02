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

import static com.spotify.hamcrest.pojo.IsPojo.pojo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.IpAddr;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.collect.Lists;

public class DocumentEnricherTest {

    @Test
    public void verifyCacheUsage() throws InterruptedException {
        final MockDocumentEnricherFactory factory = new MockDocumentEnricherFactory();
        final DocumentEnricher enricher = factory.getEnricher();
        final NodeDao nodeDao = factory.getNodeDao();
        final InterfaceToNodeCache interfaceToNodeCache = factory.getInterfaceToNodeCache();
        final AtomicInteger nodeDaoGetCounter = factory.getNodeDaoGetCounter();

        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.1"), 1);
        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.2"), 2);
        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.3"), 3);

        nodeDao.save(createOnmsNode(1, "my-requisition"));
        nodeDao.save(createOnmsNode(2, "my-requisition"));
        nodeDao.save(createOnmsNode(3, "my-requisition"));

        final List<FlowDocument> documents = Lists.newArrayList();
        documents.add(createFlowDocument("10.0.0.1", "10.0.0.2"));
        documents.add(createFlowDocument("10.0.0.1", "10.0.0.3"));
        enricher.enrich(documents.stream().map(TestFlow::new).collect(Collectors.toList()), new FlowSource("Default", "127.0.0.1", null));

        // get is also called for each save, so we account for those as well
        assertEquals(6, nodeDaoGetCounter.get());

        // Try to enrich flow documents to existing IpAddresses.
        documents.clear();
        documents.add(createFlowDocument("10.0.0.2", "10.0.0.3"));
        enricher.enrich(documents.stream().map(TestFlow::new).collect(Collectors.toList()), new FlowSource("Default", "127.0.0.1", null));
        // Since above two addresses are cached, no extra calls to nodeDao.
        assertEquals(6, nodeDaoGetCounter.get());


        // Add two more interfaces to the system.
        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.4"), 2);
        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.5"), 3);
        documents.add(createFlowDocument("10.0.0.4", "10.0.0.5"));
        enricher.enrich(documents.stream().map(TestFlow::new).collect(Collectors.toList()), new FlowSource("Default", "127.0.0.1", null));
        // Since above two addresses are added to same nodes, no extra calls to nodeDao
        assertEquals(6, nodeDaoGetCounter.get());


    }

    private static FlowDocument createFlowDocument(String sourceIp, String destIp) {
        final FlowDocument document = new FlowDocument();
        document.setTimestamp(System.currentTimeMillis());
        document.setFirstSwitched(document.getTimestamp() - 20_000L);
        document.setDeltaSwitched(document.getTimestamp() - 10_000L);
        document.setLastSwitched(document.getTimestamp() - 5_000L);
        document.setSrcAddr(sourceIp);
        document.setSrcPort(510);
        document.setDstAddr(destIp);
        document.setDstPort(80);
        document.setProtocol(6); // TCP
        return document;
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
        final DocumentEnricher enricher = factory.getEnricher();

        final FlowDocument flowDocument = new FlowDocument();

        // verify that null values are handled correctly, see issue HZN-1329
        ClassificationRequest classificationRequest;

        classificationRequest = enricher.createClassificationRequest(flowDocument);
        assertEquals(false, classificationRequest.isClassifiable());

        flowDocument.setDstPort(123);

        classificationRequest = enricher.createClassificationRequest(flowDocument);
        assertEquals(false, classificationRequest.isClassifiable());

        flowDocument.setSrcPort(456);

        classificationRequest = enricher.createClassificationRequest(flowDocument);
        assertEquals(false, classificationRequest.isClassifiable());

        flowDocument.setProtocol(6);

        classificationRequest = enricher.createClassificationRequest(flowDocument);
        assertEquals(true, classificationRequest.isClassifiable());
    }

    @Test
    public void testDirection() throws InterruptedException {
        final MockDocumentEnricherFactory factory = new MockDocumentEnricherFactory();
        final DocumentEnricher enricher = factory.getEnricher();

        final FlowDocument d1 = new FlowDocument();
        d1.setSrcAddr("1.1.1.1");
        d1.setSrcPort(1);
        d1.setDstAddr("2.2.2.2");
        d1.setDstPort(2);
        d1.setProtocol(6);
        d1.setDirection(Direction.INGRESS);

        final ClassificationRequest c1 = enricher.createClassificationRequest(d1);
        assertEquals(IpAddr.of("1.1.1.1"), c1.getSrcAddress());
        assertEquals(IpAddr.of("2.2.2.2"), c1.getDstAddress());
        assertEquals(new Integer(1), c1.getSrcPort());
        assertEquals(new Integer(2), c1.getDstPort());

        final FlowDocument d2 = new FlowDocument();
        d2.setSrcAddr("1.1.1.1");
        d2.setSrcPort(1);
        d2.setDstAddr("2.2.2.2");
        d2.setDstPort(2);
        d2.setProtocol(6);
        d2.setDirection(Direction.EGRESS);

        // check that fields stay as theay are even when EGRESS is used
        final ClassificationRequest c2 = enricher.createClassificationRequest(d2);
        assertEquals(IpAddr.of("1.1.1.1"), c2.getSrcAddress());
        assertEquals(IpAddr.of("2.2.2.2"), c2.getDstAddress());
        assertEquals(new Integer(1), c2.getSrcPort());
        assertEquals(new Integer(2), c2.getDstPort());

        final FlowDocument d3 = new FlowDocument();
        d3.setSrcAddr("1.1.1.1");
        d3.setSrcPort(1);
        d3.setDstAddr("2.2.2.2");
        d3.setDstPort(2);
        d3.setProtocol(6);
        d3.setDirection(null);

        final ClassificationRequest c3 = enricher.createClassificationRequest(d3);
        assertEquals(IpAddr.of("1.1.1.1"), c3.getSrcAddress());
        assertEquals(IpAddr.of("2.2.2.2"), c3.getDstAddress());
        assertEquals(new Integer(1), c3.getSrcPort());
        assertEquals(new Integer(2), c3.getDstPort());
    }

    @Test
    public void testClockCorrection() throws InterruptedException {
        final MockDocumentEnricherFactory factory = new MockDocumentEnricherFactory(2400_000L);
        final DocumentEnricher enricher = factory.getEnricher();

        final FlowDocument base = createFlowDocument("10.0.0.1", "10.0.0.3");

        final TestFlow flow1 = new TestFlow(base);
        flow1.setReceivedAt(flow1.getTimestamp());

        final TestFlow flow2 = new TestFlow(base);
        flow2.setReceivedAt(flow2.getTimestamp() - 3600_000L);

        final TestFlow flow3 = new TestFlow(base);
        flow3.setReceivedAt(flow3.getTimestamp() + 3600_000L);

        final List<Flow> flows = Lists.newArrayList(flow1, flow2, flow3);

        final List<FlowDocument> docs = enricher.enrich(flows, new FlowSource("Default", "127.0.0.1", null));

        assertThat(docs, contains(
                pojo(FlowDocument.class)
                        .where(FlowDocument::getTimestamp, is(base.getTimestamp()))
                        .where(FlowDocument::getFirstSwitched, is(base.getFirstSwitched()))
                        .where(FlowDocument::getDeltaSwitched, is(base.getDeltaSwitched()))
                        .where(FlowDocument::getLastSwitched, is(base.getLastSwitched())),
                pojo(FlowDocument.class)
                        .where(FlowDocument::getTimestamp, is(base.getTimestamp() - 3600_000L))
                        .where(FlowDocument::getFirstSwitched, is(base.getFirstSwitched() - 3600_000L))
                        .where(FlowDocument::getDeltaSwitched, is(base.getDeltaSwitched() - 3600_000L))
                        .where(FlowDocument::getLastSwitched, is(base.getLastSwitched() - 3600_000L)),
                pojo(FlowDocument.class)
                        .where(FlowDocument::getTimestamp, is(base.getTimestamp() + 3600_000L))
                        .where(FlowDocument::getFirstSwitched, is(base.getFirstSwitched() + 3600_000L))
                        .where(FlowDocument::getDeltaSwitched, is(base.getDeltaSwitched() + 3600_000L))
                        .where(FlowDocument::getLastSwitched, is(base.getLastSwitched() + 3600_000L))
        ));
    }
}
