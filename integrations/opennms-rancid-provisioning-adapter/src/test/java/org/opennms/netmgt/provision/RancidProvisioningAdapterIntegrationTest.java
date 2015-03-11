/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath*:/META-INF/opennms/component-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
		"classpath*:/META-INF/opennms/provisiond-extensions.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
/**
 * Test class for Rancid Provisioning
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class RancidProvisioningAdapterIntegrationTest implements InitializingBean {

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private MockEventIpcManager m_mockEventIpcManager;
    
    @Autowired
    private DatabasePopulator m_populator;

    @Autowired
    private RancidProvisioningAdapter m_adapter; 
    
    private static final int NODE_ID = 1;

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
        MockLogAppender.setupLogging(props);
        
        m_populator.populateDatabase();
    }
    
    /**
     * TODO: This test needs to be updated so that it properly connects to the JUnitHttpServer
     * for simulated RANCID REST operations.
     */
    @Test
    @Transactional
    @JUnitHttpServer(port=7081,basicAuth=true)
    public void testAddNode() {
        List<OnmsNode> nodes = m_nodeDao.findAll();
        
        assertTrue(nodes.size() > 0);
        
        m_adapter.addNode(nodes.get(0).getId());
    }
    
    /**
     * TODO: This test needs to be updated so that it properly connects to the JUnitHttpServer
     * for simulated RANCID REST operations.
     */
    @Test
    @Transactional
    @JUnitHttpServer(port=7081,basicAuth=true)
    @Ignore
    public void testAddSameOperationTwice() throws InterruptedException {
        // AdapterOperationChecker verifyOperations = new AdapterOperationChecker(2);
        // m_adapter.getOperationQueue().addListener(verifyOperations);
        OnmsNode node = m_nodeDao.get(NODE_ID);
        assertNotNull(node);
        int firstNodeId = node.getId();

        m_adapter.addNode(firstNodeId);
        m_adapter.addNode(firstNodeId); // should get deduplicated
        m_adapter.updateNode(firstNodeId);

        // assertTrue(verifyOperations.enqueueLatch.await(4, TimeUnit.SECONDS));
        // assertTrue(verifyOperations.dequeueLatch.await(4, TimeUnit.SECONDS));
        // assertTrue(verifyOperations.executeLatch.await(4, TimeUnit.SECONDS));
        assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(firstNodeId).size());
    }

    @Test
    @Transactional
    public void testUpdateNode() {
        // TODO: Add some tests
    }

    @Test
    @Transactional
    public void testDeleteNode() {
        // TODO: Add some tests
    }

    @Test
    @Transactional
    public void testNodeConfigChanged() {
        // TODO: Add some tests
    }
}
