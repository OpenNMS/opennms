/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.bsm;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.internal.BusinessServiceImpl;
import org.opennms.netmgt.bsm.service.internal.IpServiceImpl;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Node;
import org.opennms.netmgt.bsm.test.BsmTestUtils;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;

public class BusinessServiceVertexProviderTest {

    @Test
    public void testVertexRefId() {
        // Mock the manager to return a node label
        BusinessServiceManager managerMock = EasyMock.createNiceMock(BusinessServiceManager.class);
        EasyMock.expect(managerMock.getNodeById(EasyMock.anyInt())).andReturn(new Node() {
            @Override
            public String getLabel() {
                return "localhost";
            }

            @Override
            public Integer getId() {
                return 1;
            }
        }).anyTimes();
        EasyMock.replay(managerMock);

        // create 3 business service vertices, where the first 2 should be equal
        BusinessServiceEntityBuilder builder = new BusinessServiceEntityBuilder().id(10L).name("Name");
        BusinessService bs1 = new BusinessServiceImpl(managerMock, builder.toEntity());
        BusinessService bs2 = new BusinessServiceImpl(managerMock, builder.toEntity());
        BusinessService bs3 = new BusinessServiceImpl(managerMock, builder.id(11L).toEntity()); // is different
        BusinessServiceVertex bsVertex1 = new BusinessServiceVertex(bs1, 0);
        BusinessServiceVertex bsVertex2 = new BusinessServiceVertex(bs2, 0);
        BusinessServiceVertex bsVertex3 = new BusinessServiceVertex(bs3, 0);

        // create 2 ip Service vertices where all of them should be equal
        IpService ipService1 = new IpServiceImpl(managerMock, BsmTestUtils.createMonitoredService(1, 1, "127.0.0.1", "SSH"));
        IpService ipService2 = new IpServiceImpl(managerMock, BsmTestUtils.createMonitoredService(1, 1, "127.0.0.1", "SSH"));
        IpServiceVertex ipServiceVertex1 = new IpServiceVertex(ipService1, 0);
        IpServiceVertex ipServiceVertex2 = new IpServiceVertex(ipService2, 0);

        // create 3 reduction key vertices where 2 of them should be equal
        ReductionKeyVertex rkVertex1 = new ReductionKeyVertex("key1", 0);
        ReductionKeyVertex rkVertex2 = new ReductionKeyVertex("key1", 0);
        ReductionKeyVertex rkVertex3 = new ReductionKeyVertex("key2", 0);

        // Add all the above vertices. Some of them even twice to ensure that the getRefId() methods work correctly
        BusinessServiceVertexProvider vertexProvider = new BusinessServiceVertexProvider(BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE);
        vertexProvider.add(bsVertex1, bsVertex1, bsVertex2, bsVertex2, bsVertex3, bsVertex3); // adding twice on purpose
        vertexProvider.add(ipServiceVertex1, ipServiceVertex1, ipServiceVertex2, ipServiceVertex2); // adding twice on purpose
        vertexProvider.add(rkVertex1, rkVertex1); // adding twice on purpose
        vertexProvider.add(rkVertex2, rkVertex2); // adding twice on purpose
        vertexProvider.add(rkVertex3, rkVertex3); // adding twice on purpose

        // In total there should be 5 vertices
        Assert.assertEquals(5, vertexProvider.getVertices().size());
        Assert.assertSame(ipServiceVertex2, vertexProvider.getVertex(ipServiceVertex1));
        Assert.assertSame(ipServiceVertex2, vertexProvider.getVertex(ipServiceVertex2));

        Assert.assertSame(bsVertex2, vertexProvider.getVertex(bsVertex1));
        Assert.assertSame(bsVertex2, vertexProvider.getVertex(bsVertex2));
        Assert.assertSame(bsVertex3, vertexProvider.getVertex(bsVertex3));
        Assert.assertNotSame(bsVertex1, vertexProvider.getVertex(bsVertex3));
        Assert.assertNotSame(bsVertex2, vertexProvider.getVertex(bsVertex3));

        Assert.assertSame(rkVertex2, vertexProvider.getVertex(rkVertex1));
        Assert.assertSame(rkVertex2, vertexProvider.getVertex(rkVertex2));
        Assert.assertNotSame(rkVertex1, vertexProvider.getVertex(rkVertex3));
        Assert.assertSame(rkVertex3, vertexProvider.getVertex(rkVertex3));

        EasyMock.verify(managerMock);
    }
}
