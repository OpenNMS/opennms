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

import java.util.List;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.TopologyServiceClient;
import org.opennms.features.topology.api.topo.AbstractSearchQuery;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
@Transactional
public class BusinessServiceSearchProviderIT {

    @Autowired
    private BusinessServiceDao businessServiceDao;

    @Autowired
    private BusinessServiceManager businessServiceManager;

    @Before
    public void before() {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    public void verifyQuery() {
        BusinessServiceEntity bs1 = new BusinessServiceEntityBuilder()
                .name("Test Service")
                .reduceFunction(new HighestSeverityEntity())
                .addReductionKey("bs1.key1", new IdentityEntity(), 1)
                .addReductionKey("bs1.key2", new IdentityEntity(), 1)
                .toEntity();
        BusinessServiceEntity bs2 = new BusinessServiceEntityBuilder()
                .name("Real Service 2")
                .reduceFunction(new HighestSeverityEntity())
                .addReductionKey("bs2.key1", new IdentityEntity(), 1)
                .addReductionKey("bs2.key2", new IdentityEntity(), 1)
                .toEntity();
        businessServiceDao.save(bs1);
        businessServiceDao.save(bs2);
        businessServiceDao.flush();

        // prepare mocks
        TopologyServiceClient topologyServiceClientMock = EasyMock.createNiceMock(TopologyServiceClient.class);
        EasyMock.expect(topologyServiceClientMock.getVertex(EasyMock.anyObject(BusinessServiceVertex.class)))
                .andReturn(new AbstractVertex("bsm", "0", "Dummy Vertex")); // always return a vertex, it just needs to be not null

        GraphContainer graphContainerMock = EasyMock.createNiceMock(GraphContainer.class);
        EasyMock.expect(graphContainerMock.getTopologyServiceClient()).andReturn(topologyServiceClientMock).anyTimes();
        EasyMock.replay(graphContainerMock, topologyServiceClientMock);

        // try searching
        final BusinessServiceSearchProvider provider = new BusinessServiceSearchProvider();
        provider.setBusinessServiceManager(businessServiceManager);
        final SearchQuery query = new AbstractSearchQuery("Test"){
            @Override
            public boolean matches(String label) {
                return true; // always match, it does not matter
            }
        };
        final List<SearchResult> result = provider.query(query, graphContainerMock);
        Assert.assertEquals(1, result.size());
        EasyMock.verify(graphContainerMock, topologyServiceClientMock);
    }
}
