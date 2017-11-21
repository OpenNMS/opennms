/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.adapters.netflow;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Netflow5AdapterTestFactory.class})
public class Netflow5AdapterTest {

    private Netflow5Adapter netflow5Adapter;

    private NodeDao nodeDao;

    private InterfaceToNodeCache interfaceToNodeCache;

    private AtomicInteger nodeDaoGetCounter;

    @Before
    public void setUp() {
        final Netflow5AdapterTestFactory factory = new Netflow5AdapterTestFactory();
        netflow5Adapter = factory.createAdapter();

        nodeDao = factory.getNodeDao();
        interfaceToNodeCache = factory.getInterfaceToNodeCache();
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
        final List<NetflowDocument> documents = new ArrayList<>();
        documents.add(createFlowDocument("10.0.0.1", "10.0.0.2"));
        documents.add(createFlowDocument("10.0.0.1", "10.0.0.3"));
        netflow5Adapter.enrich(documents, "Default", "127.0.0.1");

        Assert.assertEquals(3, nodeDaoGetCounter.get());
    }

    private NetflowDocument createFlowDocument(String sourceIp, String destIp) {
        final NetflowDocument document = new NetflowDocument();
        document.setIpv4SourceAddress(sourceIp);
        document.setIpv4DestAddress(destIp);
        return document;
    }

    private OnmsNode createOnmsNode(int nodeId, String foreignSource) {
        OnmsNode node = new OnmsNode();
        node.setId(nodeId);
        node.setForeignSource(foreignSource);
        node.setForeignId(nodeId + "");
        return node;
    }

}