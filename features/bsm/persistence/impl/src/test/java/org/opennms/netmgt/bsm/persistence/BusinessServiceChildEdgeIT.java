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
 *       http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.persistence;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceChildEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.netmgt.dao.DatabasePopulator;
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
    "classpath:/META-INF/opennms/mockEventIpcManager.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
@Transactional
public class BusinessServiceChildEdgeIT {

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private BusinessServiceDao m_businessServiceDao;

    @Autowired
    private BusinessServiceEdgeDao m_businessServiceEdgeDao;

    @BeforeClass
    public static void setUpClass() {
        MockLogAppender.setupLogging(true, "TRACE", new Properties());
    }

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
        m_databasePopulator.populateDatabase();
    }

    @Test
    public void canCreateReadUpdateAndDeleteEdges() {
        // Create the Parent Business Service
        BusinessServiceEntity parent = new BusinessServiceEntityBuilder()
            .name("Parent Service")
            .reduceFunction(new HighestSeverityEntity())
            .toEntity();
        // Create the Child Business Service
        BusinessServiceEntity child = new BusinessServiceEntityBuilder()
                .name("Child Service")
                .reduceFunction(new HighestSeverityEntity())
                .toEntity();
        Long parentServiceId = m_businessServiceDao.save(parent);
        Long childServiceId = m_businessServiceDao.save(child);
        m_businessServiceDao.flush();

        // Initially there should be no edges
        assertEquals(0, m_businessServiceEdgeDao.countAll());

        // Create an edge
        BusinessServiceChildEdgeEntity edge = new BusinessServiceChildEdgeEntity();
        edge.setMapFunction(new IdentityEntity());
        edge.setBusinessService(parent);
        edge.setChild(child);
        m_businessServiceEdgeDao.save(edge);
        m_businessServiceEdgeDao.flush();

        // Read an edge
        assertEquals(1, m_businessServiceEdgeDao.countAll());
        assertEquals(edge, m_businessServiceEdgeDao.get(edge.getId()));
        assertEquals(parentServiceId, edge.getBusinessService().getId());
        assertEquals(childServiceId, edge.getChild().getId());

        // Update an edge
        edge.setWeight(2);
        m_businessServiceEdgeDao.save(edge);
        m_businessServiceEdgeDao.flush();

        BusinessServiceEdgeEntity otherEdge = m_businessServiceEdgeDao.get(edge.getId());
        assertEquals(edge, otherEdge);
        assertEquals(1, m_businessServiceEdgeDao.countAll());

        // Delete an edge
        m_businessServiceEdgeDao.delete(edge);
        assertEquals(0, m_businessServiceEdgeDao.countAll());
    }
}
