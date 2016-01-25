/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertNotNull;

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEdgeDao;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IgnoreEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.MapFunctionDao;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.MostCriticalEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ReductionFunctionDao;
import org.opennms.netmgt.bsm.test.BusinessServiceEntityBuilder;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
    "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath*:/META-INF/opennms/component-dao.xml",
    "classpath:/META-INF/opennms/mockEventIpcManager.xml",
    "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
// TODO MVR write a test that verifies that the the reduction function is properly deleted if not anymore referenced
public class BusinessServiceDaoIT {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessServiceDaoIT.class);

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private BusinessServiceDao m_businessServiceDao;

    @Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private ReductionFunctionDao m_reductionFunctionDao;

    @Autowired
    private BusinessServiceEdgeDao m_edgeDao;

    @Autowired
    private MapFunctionDao mapFunctionDao;

    private MostCriticalEntity m_mostCritical;

    private IgnoreEntity m_ignore;

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
        m_databasePopulator.setPopulateInSeparateTransaction(false);
        m_databasePopulator.populateDatabase();
        m_mostCritical = new MostCriticalEntity();
        m_ignore = new IgnoreEntity();
    }

    @Test
    @Transactional
    public void canCreateReadUpdateAndDeleteBusinessServices() {
        final int ifServiceCount = m_monitoredServiceDao.countAll();

        // Initially there should be no business services
        assertEquals(0, m_businessServiceDao.countAll());

        // Create a business service
        BusinessServiceEntity bs = new BusinessServiceEntity();
        bs.setName("Web Servers");
        bs.getAttributes().put("dc", "RDU");

        m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // Read a business service
        assertEquals(bs, m_businessServiceDao.get(bs.getId()));
        assertEquals(2, m_businessServiceDao.get(bs.getId()).getReductionKeyEdges().size());

        // Update a business service
        bs.setName("Application Servers");
        bs.getAttributes().put("dc", "!RDU");
        bs.getAttributes().put("cd", "/");

        // Grab the first monitored service from node 1
        OnmsMonitoredService ipService = m_databasePopulator.getNode1()
                .getIpInterfaces().iterator().next()
                .getMonitoredServices().iterator().next();
        bs.addIpServiceEdge(ipService, m_ignore);
        m_businessServiceDao.update(bs);
        m_businessServiceDao.flush();

        // Verify the update
        assertEquals(bs, m_businessServiceDao.get(bs.getId()));

        // Delete
        m_businessServiceDao.delete(bs);
        m_businessServiceDao.flush();

        // There should be no business services after the delete
        assertEquals(0, m_businessServiceDao.countAll());

        // No if service should have been deleted
        assertEquals(ifServiceCount, m_monitoredServiceDao.countAll());
    }

    @Test
    @Transactional
    public void verifyBusinessServicesWithRelatedIpServicesAreDeletedOnCascade() throws InterruptedException {
        // Initially there should be no business services
        assertEquals("Check that there are no initial BusinessServices", 0, m_businessServiceDao.countAll());

        // Create a business service with an associated IP Service
        final BusinessServiceEntity bs = new BusinessServiceEntity();
        bs.setName("Mont Cascades");
        bs.setReductionFunction(m_mostCritical);
        final OnmsNode node = m_databasePopulator.getNode1();
        final OnmsMonitoredService ipService = getMonitoredServiceFromNode1();
        bs.addIpServiceEdge(ipService, m_ignore);

        m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // We should have a single business service with a single IP service associated
        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(1, m_businessServiceDao.get(bs.getId()).getIpServices().size());
        assertNotNull(m_monitoredServiceDao.get(ipService.getId()));

        // Now delete the node
        m_nodeDao.delete(node);
        m_nodeDao.flush();

        // The business service should still be present, but the IP service should have been deleted by the foreign
        // key constraint. We have to clear the session, otherwise hibernate does not know about the node deletion
        m_businessServiceDao.clear();
        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(0, m_businessServiceDao.get(bs.getId()).getIpServices().size());
    }

    @Test
    @Transactional
    public void verifyDeleteOnCascade() {
        BusinessServiceEntity child = new BusinessServiceEntityBuilder()
                .name("Child2")
                .reduceFunction(new MostCriticalEntity())
                .toEntity();

        BusinessServiceEntity parent = new BusinessServiceEntityBuilder()
                .name("Web Servers")
                .addAttribute("dc", "RDU")
                .addReductionKey("TestReductionKeyA")
                .addReductionKey("TestReductionKeyB")
                .addIpService(getMonitoredServiceFromNode1())
                .reduceFunction(m_mostCritical)
                .addChildren(child)
                .toEntity();

        m_businessServiceDao.save(child);
        m_businessServiceDao.save(parent);

        assertEquals(2, m_businessServiceDao.countAll());
        assertEquals(2, m_reductionFunctionDao.countAll());
        assertEquals(4, m_edgeDao.countAll());

        // Deletion of child does not delete the edges referencing to that child
        m_businessServiceDao.delete(child);
        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(1, m_reductionFunctionDao.countAll());
        assertEquals(4, m_edgeDao.countAll());

        // TODO MVR is there any way to do this automatically with hibernate annotations?
        // we have to manually delete the edge, than we are good
        parent.removeEdge(parent.getChildEdges().iterator().next());
        m_businessServiceDao.update(parent);
        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(1, m_reductionFunctionDao.countAll());
        assertEquals(3, m_edgeDao.countAll());

        // Deletion of parent should delete all references
        m_businessServiceDao.delete(parent);
        assertEquals(0, m_businessServiceDao.countAll());
        assertEquals(0, m_reductionFunctionDao.countAll());
        assertEquals(0, m_edgeDao.countAll());
    }

    private OnmsMonitoredService getMonitoredServiceFromNode1() {
        final OnmsMonitoredService ipService = m_databasePopulator.getNode1()
                .getIpInterfaces().iterator().next()
                .getMonitoredServices().iterator().next();
        return Objects.requireNonNull(ipService);
    }
}
