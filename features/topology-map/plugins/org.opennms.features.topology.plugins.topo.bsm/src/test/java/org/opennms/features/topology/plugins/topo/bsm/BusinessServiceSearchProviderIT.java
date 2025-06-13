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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
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
        TopologyServiceClient topologyServiceClientMock = mock(TopologyServiceClient.class);
        when(topologyServiceClientMock.getVertex(any(BusinessServiceVertex.class)))
                .thenReturn(new AbstractVertex("bsm", "0", "Dummy Vertex")); // always return a vertex, it just needs to be not null

        GraphContainer graphContainerMock = mock(GraphContainer.class);
        when(graphContainerMock.getTopologyServiceClient()).thenReturn(topologyServiceClientMock);

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
    }
}
