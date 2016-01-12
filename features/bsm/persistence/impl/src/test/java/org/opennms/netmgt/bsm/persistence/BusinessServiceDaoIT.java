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

import java.util.HashSet;

import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.bsm.persistence.api.BusinessService;
import org.opennms.netmgt.bsm.persistence.api.BusinessServiceDao;
import org.opennms.netmgt.bsm.persistence.api.MostCritical;
import org.opennms.netmgt.bsm.persistence.api.ReductionFunctionDao;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
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
    "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
    "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml" })
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
public class BusinessServiceDaoIT {

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private BusinessServiceDao m_businessServiceDao;

    @Autowired
    private ReductionFunctionDao m_reductionFunctionDao;

    @Autowired
    private NodeDao m_nodeDao;

    MostCritical m_mostCritical;

    @Before
    public void setUp() {
        BeanUtils.assertAutowiring(this);
        m_databasePopulator.populateDatabase();

        m_mostCritical = new MostCritical();
        m_reductionFunctionDao.save(m_mostCritical);
        m_reductionFunctionDao.flush();
    }

    @Test
    @Transactional
    public void canCreateReadUpdateAndDeleteBusinessServices() {
        // Initially there should be no business services
        assertEquals(0, m_businessServiceDao.countAll());

        // Create a business service
        BusinessService bs = new BusinessService();
        bs.setName("Web Servers");
        bs.setAttribute("dc", "RDU");
        bs.setReductionFunction(m_mostCritical);

        m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // Read a business service
        assertEquals(bs, m_businessServiceDao.get(bs.getId()));
        assertEquals( reductionKeys.size(), m_businessServiceDao.get(bs.getId()).getReductionKeys().size());

        // Update a business service
        bs.setName("Application Servers");
        bs.setAttribute("dc", "!RDU");
        bs.setAttribute("cd", "/");

        // Grab the first monitored service from node 1
        OnmsMonitoredService ipService = m_databasePopulator.getNode1()
                .getIpInterfaces().iterator().next()
                .getMonitoredServices().iterator().next();
        bs.addIpService(ipService);

        m_businessServiceDao.update(bs);
        m_businessServiceDao.flush();

        // Verify the update
        assertEquals(bs, m_businessServiceDao.get(bs.getId()));

        // Delete
        m_businessServiceDao.delete(bs);
        m_businessServiceDao.flush();

        // There should be no business services after the delete
        assertEquals(0, m_businessServiceDao.countAll());
    }

    @Test
    public void businessServicesWithRelatedIpServicesAreDeletedOnCascade() throws InterruptedException {
        // Initially there should be no business services
        assertEquals("Check that there are no initial BusinessServices", 0, m_businessServiceDao.countAll());

        // Create a business service with an associated IP Service
        BusinessService bs = new BusinessService();
        bs.setName("Mont Cascades");
        bs.setReductionFunction(m_mostCritical);
        OnmsNode node = m_databasePopulator.getNode1();
        OnmsMonitoredService ipService = node
                .getIpInterfaces().iterator().next()
                .getMonitoredServices().iterator().next();
        bs.addIpService(ipService);

        m_businessServiceDao.save(bs);
        m_businessServiceDao.flush();

        // We should have a single business service with a single IP service associated
        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(1, m_businessServiceDao.get(bs.getId()).getIpServices().size());

        // Now delete the node
        m_nodeDao.delete(node);
        m_nodeDao.flush();

        // The business service should still be present, but the IP service should have been deleted
        // by the foreign key constraint
        assertEquals(1, m_businessServiceDao.countAll());
        assertEquals(0, m_businessServiceDao.get(bs.getId()).getIpServices().size());
    }
}
