//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Mar 25: Convert to use AbstractTransactionalDaoTestCase. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class MonitoredServiceDaoTest {

	@Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;
	
	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
	}

	@Test
	@Transactional
	public void testLazy() {
    	
    	List<OnmsMonitoredService> allSvcs = m_monitoredServiceDao.findAll();
    	assertTrue(allSvcs.size() > 1);
    	
    	OnmsMonitoredService svc = allSvcs.iterator().next();
    	assertEquals(addr("192.168.1.1"), svc.getIpAddress());
    	assertEquals(1, svc.getIfIndex().intValue());
    	assertEquals(1, svc.getIpInterface().getNode().getId().intValue());
    	assertEquals("M", svc.getIpInterface().getIsManaged());
    	//assertEquals("SNMP", svc.getServiceType().getName());
    	
    }
    
    @Test
    @Transactional
    public void testGetByCompositeId() {
    	OnmsMonitoredService monSvc = m_monitoredServiceDao.get(m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), "SNMP");
    	assertNotNull(monSvc);
    	
    	OnmsMonitoredService monSvc2 = m_monitoredServiceDao.get(m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), monSvc.getIfIndex(), monSvc.getServiceId());
    	assertNotNull(monSvc2);
    	
    }
    
}
