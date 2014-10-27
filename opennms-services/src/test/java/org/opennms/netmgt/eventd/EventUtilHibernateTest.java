/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml"
        
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@Transactional
public class EventUtilHibernateTest {
    
    @Autowired
    private EventUtilDaoImpl eventUtilDaoImpl;
    
    @Autowired
    private DatabasePopulator m_populator;
    
    private static boolean m_populated = false;
    
    @Before
    public void setUp() throws Exception {
    	m_populator.populateDatabase();
    	/*try {
           if (!m_populated) {
                m_populator.populateDatabase();
            }
        } catch (Throwable e) {
            e.printStackTrace(System.err);
        } finally {
            m_populated = true;
        }
        **/
        
    }

    @After
    public void tearDown() throws Exception {
    	
    }

    @Test
    public void testGetNodeLabel() throws SQLException {
    	String label = eventUtilDaoImpl.getNodeLabel(m_populator.getNode3().getId());
		assertEquals("node3",label);
		label = eventUtilDaoImpl.getNodeLabel(m_populator.getNode1().getId());
		assertEquals("node1",label);
		label = eventUtilDaoImpl.getNodeLabel(m_populator.getNode2().getId());
		assertEquals("node2",label);
		
    }
    
    @Test
    public void testGetIfAlias() throws SQLException {
    	String alias = eventUtilDaoImpl.getIfAlias(m_populator.getNode1().getId(), "192.168.1.1");
    	assertEquals("Initial ifAlias value", alias);
    }
    
    @Test
    public void testGetAssetFieldValue() throws SQLException {
    	String asset = eventUtilDaoImpl.getAssetFieldValue("parm", m_populator.getNode3().getId());
    	assertEquals("node3", asset);
    	asset = eventUtilDaoImpl.getAssetFieldValue("parm", m_populator.getNode1().getId());
    	assertEquals("node1", asset);
    	asset = eventUtilDaoImpl.getAssetFieldValue("parm", m_populator.getNode2().getId());
    	assertEquals("node2", asset);
    }

}
