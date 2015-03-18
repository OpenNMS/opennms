/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;


import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AtInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;

import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.OnmsAtInterface;
import org.opennms.netmgt.model.OnmsNode;

import org.opennms.test.JUnitConfigurationEnvironment;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class OnmsAtInterfaceDaoHibernateTest implements InitializingBean {
    @Autowired
    private NodeDao m_nodeDao;
    
	@Autowired
    private AtInterfaceDao m_atInterfaceDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	@Before
	public void setUp() {
	    for (final OnmsNode node : m_nodeDao.findAll()) {
	        m_nodeDao.delete(node);
	    }
	    m_nodeDao.flush();
		m_databasePopulator.populateDatabase();
	}


    @Test
    public void testSaveAtInterface() {
        final OnmsAtInterface atinterface = new OnmsAtInterface(m_databasePopulator.getNode6(), m_databasePopulator.getNode6().getPrimaryInterface().getIpAddress(),"0080aa12aa12");

        atinterface.setSourceNodeId(m_databasePopulator.getNode1().getId());
        atinterface.setIfIndex(1);
        atinterface.setLastPollTime(new Date());
        atinterface.setStatus(StatusType.ACTIVE);
        
        m_atInterfaceDao.save(atinterface);
        m_atInterfaceDao.flush();

        assertEquals(2, m_atInterfaceDao.countAll());
    }
    

    @Test 
    public void testFindByNodeAndAddress() throws UnknownHostException {
    	final OnmsAtInterface atinterface = m_atInterfaceDao.findByNodeAndAddress(m_databasePopulator.getNode2().getId(), InetAddress.getByName("192.168.2.1"), "AA:BB:CC:DD:EE:FF");
	    	
    	checkAtInterface(atinterface);

    	final OnmsAtInterface atinterface2 = m_atInterfaceDao.findByNodeAndAddress(m_databasePopulator.getNode6().getId(), InetAddress.getByName("192.168.2.1"), "AA:BB:CC:DD:EE:FF");
    	assertEquals(true, atinterface2 == null);
    	
    }

    @Test
    public void testGetAtInterfaceForAddress() throws UnknownHostException {
    	Collection<OnmsAtInterface> atinterfaces = m_atInterfaceDao.getAtInterfaceForAddress(InetAddress.getByName("192.168.2.1"));
    	assertEquals(1, atinterfaces.size());
    	assertEquals(1, m_atInterfaceDao.countAll());
    	
    	final OnmsAtInterface atinterface = atinterfaces.iterator().next();
    	checkAtInterface(atinterface);
    	
    	atinterfaces = m_atInterfaceDao.getAtInterfaceForAddress(InetAddress.getByName("192.168.3.1"));
       	assertEquals(1, atinterfaces.size());
    	assertEquals(1, m_atInterfaceDao.countAll());

    	final OnmsAtInterface atinterface2 = atinterfaces.iterator().next();
    	assertEquals("192.168.3.1", atinterface2.getIpAddress().getHostAddress());
    	assertEquals(m_databasePopulator.getNode3().getId(),atinterface2.getNode().getId());
    	assertEquals("", atinterface2.getMacAddress());
    	
        atinterface2.setSourceNodeId(m_databasePopulator.getNode1().getId());
        atinterface2.setIfIndex(1);
        atinterface2.setMacAddress("0080aa11aa11");
        atinterface2.setLastPollTime(new Date());
        atinterface2.setStatus(StatusType.ACTIVE);
    	
        
        m_atInterfaceDao.saveOrUpdate(atinterface2);

    	atinterfaces = m_atInterfaceDao.getAtInterfaceForAddress(InetAddress.getByName("192.168.3.1"));
       	assertEquals(1, atinterfaces.size());
    	assertEquals(2, m_atInterfaceDao.countAll());

    	final OnmsAtInterface atinterface3 = atinterfaces.iterator().next();
    	assertEquals("192.168.3.1", atinterface3.getIpAddress().getHostAddress());
    	assertEquals(m_databasePopulator.getNode3().getId(),atinterface3.getNode().getId());
    	assertEquals("0080aa11aa11", atinterface3.getMacAddress());
    	
    	atinterfaces = m_atInterfaceDao.getAtInterfaceForAddress(InetAddress.getByName("192.168.168.1"));
       	assertEquals(0, atinterfaces.size());
    	
    }
    
    private void checkAtInterface(OnmsAtInterface atinterface) {
    	assertEquals(true, atinterface != null);
		
    	assertEquals("192.168.2.1", atinterface.getIpAddress().getHostAddress());
    	assertEquals(1, atinterface.getIfIndex().intValue());
    	assertEquals(m_databasePopulator.getNode1().getId(),atinterface.getSourceNodeId());
    	assertEquals(m_databasePopulator.getNode2().getId(),atinterface.getNode().getId());
    	assertEquals("AA:BB:CC:DD:EE:FF", atinterface.getMacAddress());
    	assertEquals(StatusType.ACTIVE, atinterface.getStatus());
    	
    }
}
