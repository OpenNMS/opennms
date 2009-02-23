/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 22, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/provisiond-extensions.xml"})
@JUnitTemporaryDatabase()
/**
 * Test class for DNS Provisioning
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class DnsProvisioningAdapterTest {

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private MockEventIpcManager m_mockEventIpcManager;
    
    @Autowired
    private DatabasePopulator m_populator;

    @Autowired
    private DnsProvisioningAdapter m_adapter; 
    
    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        
        Assert.notNull(m_nodeDao, "Autowiring failed, node dao is null");
        Assert.notNull(m_mockEventIpcManager, "Autowiring failed, ipc manager is null");
        Assert.notNull(m_populator, "Autowiring failed, db populater is null");
        Assert.notNull(m_adapter, "Autowiring failed, adapter is null");

        m_populator.populateDatabase();
    }
    
    @Test
    @Transactional
    public void testAddNode() {
        m_adapter.setNodeDao(m_nodeDao);
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

}
