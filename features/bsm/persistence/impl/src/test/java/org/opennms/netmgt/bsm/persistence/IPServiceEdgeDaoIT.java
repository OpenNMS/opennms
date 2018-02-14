/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.IPServiceEdgeEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.MapFunctionDao;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ReductionFunctionDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
public class IPServiceEdgeDaoIT {

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private BusinessServiceDao m_businessServiceDao;

    @Autowired
    private BusinessServiceEdgeDao m_businessServiceEdgeDao;

    @Autowired
    private ReductionFunctionDao m_reductionFunctionDao;

    @Autowired
    private MapFunctionDao m_mapFunctionDao;

    @Autowired
    private NodeDao m_nodeDao;

    private HighestSeverityEntity m_highestSeverity;

    private IdentityEntity m_identity;

    @BeforeClass
    public static void classSetUp() {
        final Properties props = new Properties();
        MockLogAppender.setupLogging(true, "TRACE", props);
    }

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
        m_databasePopulator.populateDatabase();

        m_highestSeverity = new HighestSeverityEntity();
        m_reductionFunctionDao.save(m_highestSeverity);
        m_reductionFunctionDao.flush();

        m_identity = new IdentityEntity();
        m_mapFunctionDao.save(m_identity);
        m_mapFunctionDao.flush();
    }

    @Test
    public void ipServiceEdgesWithRelatedIpServicesAreDeletedOnCascade() throws InterruptedException {
        // Create a business service
        BusinessServiceEntity bs = new BusinessServiceEntity();
        bs.setName("Mont Cascades");
        bs.setAttribute("dc", "RDU");
        bs.setReductionFunction(m_highestSeverity);
        m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // Initially there should be no edges
        assertEquals(0, m_businessServiceEdgeDao.countAll());

        // Create an edge
        IPServiceEdgeEntity edge = new IPServiceEdgeEntity();
        edge.setMapFunction(m_identity);
        edge.setBusinessService(bs);

        // Grab the first monitored service from node 1
        OnmsNode node = m_databasePopulator.getNode1();
        OnmsMonitoredService ipService = node.getIpInterfaces().iterator().next()
                .getMonitoredServices().iterator().next();
        edge.setIpService(ipService);
        m_businessServiceEdgeDao.save(edge);
        m_businessServiceEdgeDao.flush();

        bs.addEdge(edge);
        m_businessServiceDao.update(bs);
        m_businessServiceDao.flush();

        // We should have a single business service with a single IP service associated
        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(1, m_businessServiceDao.get(bs.getId()).getIpServiceEdges().size());

        // Now delete the node
        m_nodeDao.delete(node);
        m_nodeDao.flush();

        // The business service should still be present, but the IP service should have been deleted
        // by the foreign key constraint
        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(0, m_businessServiceDao.get(bs.getId()).getIpServiceEdges().size());
    }
}
