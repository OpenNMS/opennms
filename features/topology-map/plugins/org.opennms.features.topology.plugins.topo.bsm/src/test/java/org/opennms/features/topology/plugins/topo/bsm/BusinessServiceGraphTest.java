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
package org.opennms.features.topology.plugins.topo.bsm;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

public class BusinessServiceGraphTest {

    @Test
    public void testVertexRefId() {
        // Mock the manager to return a node label
        BusinessServiceManager managerMock = mock(BusinessServiceManager.class);
        when(managerMock.getNodeById(anyInt())).thenReturn(new Node() {
            @Override
            public String getLabel() {
                return "localhost";
            }

            @Override
            public Integer getId() {
                return 1;
            }
        });

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
        BusinessServiceGraph businessServiceGraph = new BusinessServiceGraph();
        businessServiceGraph.add(bsVertex1, bsVertex1, bsVertex2, bsVertex2, bsVertex3, bsVertex3); // adding twice on purpose
        businessServiceGraph.add(ipServiceVertex1, ipServiceVertex1, ipServiceVertex2, ipServiceVertex2); // adding twice on purpose
        businessServiceGraph.add(rkVertex1, rkVertex1); // adding twice on purpose
        businessServiceGraph.add(rkVertex2, rkVertex2); // adding twice on purpose
        businessServiceGraph.add(rkVertex3, rkVertex3); // adding twice on purpose

        // In total there should be 5 vertices
        Assert.assertEquals(5, businessServiceGraph.getVertices().size());
        Assert.assertSame(ipServiceVertex2, businessServiceGraph.getVertex(ipServiceVertex1));
        Assert.assertSame(ipServiceVertex2, businessServiceGraph.getVertex(ipServiceVertex2));

        Assert.assertSame(bsVertex2, businessServiceGraph.getVertex(bsVertex1));
        Assert.assertSame(bsVertex2, businessServiceGraph.getVertex(bsVertex2));
        Assert.assertSame(bsVertex3, businessServiceGraph.getVertex(bsVertex3));
        Assert.assertNotSame(bsVertex1, businessServiceGraph.getVertex(bsVertex3));
        Assert.assertNotSame(bsVertex2, businessServiceGraph.getVertex(bsVertex3));

        Assert.assertSame(rkVertex2, businessServiceGraph.getVertex(rkVertex1));
        Assert.assertSame(rkVertex2, businessServiceGraph.getVertex(rkVertex2));
        Assert.assertNotSame(rkVertex1, businessServiceGraph.getVertex(rkVertex3));
        Assert.assertSame(rkVertex3, businessServiceGraph.getVertex(rkVertex3));
    }
}
