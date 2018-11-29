/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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