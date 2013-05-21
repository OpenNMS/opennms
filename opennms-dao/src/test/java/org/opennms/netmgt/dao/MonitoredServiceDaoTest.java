/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class MonitoredServiceDaoTest implements InitializingBean {

	@Autowired
    private MonitoredServiceDao m_monitoredServiceDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;
	
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
	}

	@Test
	@Transactional
	@JUnitTemporaryDatabase
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
    @JUnitTemporaryDatabase
    public void testGetByCompositeId() {
    	OnmsMonitoredService monSvc = m_monitoredServiceDao.get(m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), "SNMP");
    	assertNotNull(monSvc);
    	
    	OnmsMonitoredService monSvc2 = m_monitoredServiceDao.get(m_databasePopulator.getNode1().getId(), addr("192.168.1.1"), monSvc.getIfIndex(), monSvc.getServiceId());
    	assertNotNull(monSvc2);
    	
    }
    
}
