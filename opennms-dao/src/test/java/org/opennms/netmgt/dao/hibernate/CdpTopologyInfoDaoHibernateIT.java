/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.CdpTopologyInfoDao;
import org.opennms.netmgt.model.CdpLinkInfo;
import org.opennms.netmgt.model.VertexInfo;
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
public class CdpTopologyInfoDaoHibernateIT {
    @Autowired
    private CdpTopologyInfoDao cdpTopologyInfoDao;

    @Autowired
    private DatabasePopulator populator;

    @BeforeTransaction
    public void setUp() {
        populator.populateDatabase();
    }

    @AfterTransaction
    public void tearDown() {
        populator.resetDatabase();
    }

    @Test
    @Transactional
    public void testGetAllVertices() {
        List<VertexInfo> vertices = this.cdpTopologyInfoDao.getVertexInfos();
        Assert.assertNotNull(vertices);
        Assert.assertFalse(vertices.isEmpty());
        VertexInfo vertex = vertices.get(0);
        assertNotNull(vertex.getId());
        assertFalse(vertex.getLocation().trim().isEmpty());
        assertFalse(vertex.getLabel().trim().isEmpty());
        assertNotNull(vertex.getType());
    }

    @Test
    @Transactional
    public void testGetAllCdpLinkInfos() {
        List<CdpLinkInfo> cdpLinks = this.cdpTopologyInfoDao.getCdpLinkInfo();
        Assert.assertNotNull(cdpLinks);
        Assert.assertFalse(cdpLinks.isEmpty());
        CdpLinkInfo info = cdpLinks.get(0);
        assertNotNull(info.getId());
        assertNotNull(info.getNodeId());
    }

}