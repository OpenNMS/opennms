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

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.model.OnmsNode;

import com.google.common.collect.Lists;

public class DocumentEnricherTest {

    private DocumentEnricher enricher;
    private AtomicInteger nodeDaoGetCounter;

    @Before
    public void setUp() {
        final MockDocumentEnricherFactory factory = new MockDocumentEnricherFactory();
        enricher = factory.getEnricher();
        final NodeDao nodeDao = factory.getNodeDao();
        final InterfaceToNodeCache interfaceToNodeCache = factory.getInterfaceToNodeCache();
        nodeDaoGetCounter = factory.getNodeDaoGetCounter();

        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.1"), 1);
        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.2"), 2);
        interfaceToNodeCache.setNodeId("Default", InetAddressUtils.addr("10.0.0.3"), 3);

        nodeDao.save(createOnmsNode(1, "my-requisition"));
        nodeDao.save(createOnmsNode(2, "my-requisition"));
        nodeDao.save(createOnmsNode(3, "my-requisition"));
    }

    @Test
    public void verifyCacheUsage() {
        final List<FlowDocument> documents = Lists.newArrayList();
        documents.add(createFlowDocument("10.0.0.1", "10.0.0.2"));
        documents.add(createFlowDocument("10.0.0.1", "10.0.0.3"));
        enricher.enrich(documents, new FlowSource("Default", "127.0.0.1"));

        // get is also called for each save, so we account for those as well
        assertEquals(6, nodeDaoGetCounter.get());
    }

    private static FlowDocument createFlowDocument(String sourceIp, String destIp) {
        final FlowDocument document = new FlowDocument();
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
}
