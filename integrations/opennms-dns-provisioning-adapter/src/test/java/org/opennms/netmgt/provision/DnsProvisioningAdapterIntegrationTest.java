/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Test class for DNS Provisioning
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DnsProvisioningAdapterIntegrationTest implements InitializingBean {

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private DatabasePopulator m_populator;

    @Autowired
    private DnsProvisioningAdapter m_adapter; 

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        
        m_populator.populateDatabase();
    }
    
    @Test
    @Transactional
    public void testAddNode() {
        List<OnmsNode> nodes = m_nodeDao.findAll();
        
        assertTrue(nodes.size() > 0);
        
        try {
            m_adapter.addNode(nodes.get(0).getId());
        } catch (ProvisioningAdapterException pae) {
            //do nothing for now, this is the current expectation since the adapter is not yet implemented
        }
    }
    
    @Test
    @Transactional
    public void testAddSameOperationTwice() throws InterruptedException {
        SimpleQueuedProvisioningAdapter adapter = new TestAdapter();
        
        try {
            adapter.addNode(1);
            Thread.sleep(1000);
            adapter.addNode(1);  //should get thrown away
            adapter.updateNode(1);
            org.junit.Assert.assertEquals(2, adapter.getOperationQueue().getOperationQueueForNode(1).size());
            Thread.sleep(10000);
            org.junit.Assert.assertEquals(0, adapter.getOperationQueue().getOperationQueueForNode(1).size());
        } catch (ProvisioningAdapterException pae) {
            //do nothing for now, this is the current expectation since the adapter is not yet implemented
        }
    }

    @Test
    @Transactional
    @Ignore
    public void testUpdateNode() {
        fail("Not yet implemented");
    }

    @Test
    @Transactional
    @Ignore
    public void testDeleteNode() {
        fail("Not yet implemented");
    }

    @Test
    @Transactional
    @Ignore
    public void testNodeConfigChanged() {
        fail("Not yet implemented");
    }

    class TestAdapter extends SimpleQueuedProvisioningAdapter {

        @Override
        AdapterOperationSchedule createScheduleForNode(int nodeId, AdapterOperationType adapterOperationType) {
            return new AdapterOperationSchedule(3, 3, 1, TimeUnit.SECONDS);
        };
        
        @Override
        public String getName() {
            return "TestAdapter";
        }

        @Override
        public boolean isNodeReady(AdapterOperation op) {
            return true;
        }

        @Override
        public void processPendingOperationForNode(AdapterOperation op)
                throws ProvisioningAdapterException {
            System.out.println(op);
        }
        
    }
}
