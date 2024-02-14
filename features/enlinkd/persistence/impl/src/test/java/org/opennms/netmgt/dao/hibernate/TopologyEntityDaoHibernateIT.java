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
package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityDao;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.persistence.api.CdpLinkDao;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class TopologyEntityDaoHibernateIT {
    @Autowired
    private TopologyEntityDao topologyEntityDao;

    @Autowired
    private DatabasePopulator populator;

    @Autowired
    private CdpLinkDao cdpLinkDao;

    @BeforeTransaction
    public void setUp() {
        populator.addExtension(new DatabasePopulator.Extension<CdpLinkDao>() {
            @Override
            public DatabasePopulator.DaoSupport getDaoSupport() {
                return new DatabasePopulator.DaoSupport<>(CdpLinkDao.class, cdpLinkDao);
            }

            @Override
            public void onPopulate(DatabasePopulator populator, CdpLinkDao dao) {
                CdpLink cdpLink = new CdpLink();
                cdpLink.setNode(populator.getNode1());
                cdpLink.setCdpCacheDeviceId("cdpCacheDeviceId");
                cdpLink.setCdpInterfaceName("cdpInterfaceName");
                cdpLink.setCdpCacheDevicePort("cdpCacheDevicePort");
                cdpLink.setCdpCacheAddressType(CdpLink.CiscoNetworkProtocolType.chaos);
                cdpLink.setCdpCacheAddress("CdpCacheAddress");
                cdpLink.setCdpCacheDeviceIndex(33);
                cdpLink.setCdpCacheDevicePlatform("CdpCacheDevicePlatform");
                cdpLink.setCdpCacheIfIndex(33);
                cdpLink.setCdpCacheVersion("CdpCacheVersion");
                cdpLink.setCdpLinkLastPollTime(new Date());
                dao.save(cdpLink);
            }

            @Override
            public void onShutdown(DatabasePopulator populator, CdpLinkDao dao) {
            }
        });
        populator.populateDatabase();
    }

    @AfterTransaction
    public void tearDown() {
        populator.resetDatabase();
    }

    @Test
    @Transactional
    public void testGetAllVertices() {
        List<NodeTopologyEntity> vertices = this.topologyEntityDao.getNodeTopologyEntities();
        Assert.assertNotNull(vertices);
        Assert.assertFalse(vertices.isEmpty());
        NodeTopologyEntity vertex = vertices.get(0);
        assertNotNull(vertex.getId());
        assertFalse(vertex.getLocation().trim().isEmpty());
        assertFalse(vertex.getLabel().trim().isEmpty());
        assertNotNull(vertex.getType());
    }

    @Test
    @Transactional
    public void testGetAllCdpLinkInfos() {
        List<CdpLinkTopologyEntity> cdpLinks = this.topologyEntityDao.getCdpLinkTopologyEntities();
        Assert.assertNotNull(cdpLinks);
        Assert.assertFalse(cdpLinks.isEmpty());
        CdpLinkTopologyEntity info = cdpLinks.get(0);
        assertNotNull(info.getId());
        assertNotNull(info.getNodeId());
    }

}