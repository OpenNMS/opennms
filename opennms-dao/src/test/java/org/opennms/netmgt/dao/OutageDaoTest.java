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
// 2008 Jul 05: Fix all broken tests (bug #1607). - dj@opennms.org
// 2008 Mar 25: Convert to use AbstractTransactionalDaoTestCase. - dj@opennms.org
// 2007 Jul 03: Eliminate a warning. - dj@opennms.org
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mhuot
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    JUnitSnmpAgentExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitTemporaryDatabase()
public class OutageDaoTest {
	@Autowired
    private NodeDao m_nodeDao;
	
	@Autowired
	private ServiceTypeDao m_serviceTypeDao;

	@Autowired
	private OutageDao m_outageDao;

	@Autowired
	private DistPollerDao m_distPollerDao;

	@Autowired
	private EventDao m_eventDao;

	@Autowired
	private MonitoredServiceDao m_monitoredServiceDao;

	@Autowired
	private IpInterfaceDao m_ipInterfaceDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;

	@Before
	public void setUp() {
		m_databasePopulator.populateDatabase();
	}
	
	@Test
	@Transactional
	public void testSave() {
        OnmsServiceType serviceType = m_serviceTypeDao.findByName("ICMP");
        assertNotNull(serviceType);

        final OnmsDistPoller distPoller = m_distPollerDao.load("localhost");
		OnmsNode node = new OnmsNode(distPoller);
        OnmsIpInterface ipInterface = new OnmsIpInterface("172.16.1.1", node);
        OnmsMonitoredService monitoredService = new OnmsMonitoredService(ipInterface, serviceType);
        m_nodeDao.save(node);

        OnmsEvent event = getEvent();
        m_eventDao.save(event);

        OnmsOutage outage = new OnmsOutage();
        outage.setServiceLostEvent(event);
        outage.setIfLostService(new Date());
        outage.setMonitoredService(monitoredService);
        m_outageDao.save(outage);

        //it works we're so smart! hehe
        outage = m_outageDao.load(outage.getId());
        assertEquals("ICMP", outage.getMonitoredService().getServiceType().getName());
//        outage.setEventBySvcRegainedEvent();
        
    }
    
	@Test
	@Transactional
    public void testGetMatchingOutages() {
        OnmsNode node = new OnmsNode(m_distPollerDao.load("localhost"));
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.1", "ICMP", node);

        ServiceSelector selector = new ServiceSelector("ipAddr IPLIKE 172.16.1.1", Arrays.asList(new String[] { "ICMP" }));
    	Collection<OnmsOutage> outages = m_outageDao.matchingCurrentOutages(selector);
    	assertEquals("outage count", 1, outages.size());
    }
    
	@Test
	@Transactional
    public void testGetMatchingOutagesWithEmptyServiceList() {
        OnmsNode node = new OnmsNode(m_distPollerDao.load("localhost"));
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.1", "ICMP", node);
        
        ServiceSelector selector = new ServiceSelector("ipAddr IPLIKE 172.16.1.1", new ArrayList<String>(0));
    	Collection<OnmsOutage> outages = m_outageDao.matchingCurrentOutages(selector);
    	assertEquals(1, outages.size());
    }

	@Test
	@Transactional
    public void testDuplicateOutages() {
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        OnmsNode node = new OnmsNode(m_distPollerDao.load("localhost"));
        node.setLabel("shoes");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.1", "ICMP", node);
        insertEntitiesAndOutage("172.20.1.1", "ICMP", node);
        
        node = new OnmsNode(m_distPollerDao.load("localhost"));
        node.setLabel("megaphone");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.2", "ICMP", node);
        insertEntitiesAndOutage("172.17.1.2", "ICMP", node);
        insertEntitiesAndOutage("172.18.1.2", "ICMP", node);

        node = new OnmsNode(m_distPollerDao.load("localhost"));
        node.setLabel("grunties");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.3", "ICMP", node);

        List<OutageSummary> outages = m_outageDao.getNodeOutageSummaries(0);
        System.err.println(outages);
        assertEquals(3, outages.size());
    }

	@Test
	@Transactional
    public void testLimitDuplicateOutages() {
        for (final OnmsNode node : m_nodeDao.findAll()) {
            m_nodeDao.delete(node);
        }
        OnmsNode node = new OnmsNode(m_distPollerDao.load("localhost"));
        node.setLabel("shoes");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.1", "ICMP", node);
        insertEntitiesAndOutage("172.20.1.1", "ICMP", node);
        
        node = new OnmsNode(m_distPollerDao.load("localhost"));
        node.setLabel("megaphone");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.2", "ICMP", node);
        insertEntitiesAndOutage("172.17.1.2", "ICMP", node);
        insertEntitiesAndOutage("172.18.1.2", "ICMP", node);

        node = new OnmsNode(m_distPollerDao.load("localhost"));
        node.setLabel("grunties");
        m_nodeDao.save(node);
        insertEntitiesAndOutage("172.16.1.3", "ICMP", node);

        List<OutageSummary> outages = m_outageDao.getNodeOutageSummaries(2);
        System.err.println(outages);
        assertEquals(2, outages.size());

        outages = m_outageDao.getNodeOutageSummaries(3);
        System.err.println(outages);
        assertEquals(3, outages.size());

        outages = m_outageDao.getNodeOutageSummaries(4);
        System.err.println(outages);
        assertEquals(3, outages.size());

        outages = m_outageDao.getNodeOutageSummaries(5);
        System.err.println(outages);
        assertEquals(3, outages.size());
    }

    private OnmsOutage insertEntitiesAndOutage(final String ipAddr, final String serviceName, OnmsNode node) {
        OnmsIpInterface ipInterface = getIpInterface(ipAddr, node);
        OnmsServiceType serviceType = getServiceType(serviceName);
        OnmsMonitoredService monitoredService = getMonitoredService(ipInterface, serviceType);
        
        OnmsEvent event = getEvent();

        OnmsOutage outage = getOutage(monitoredService, event);
        
        m_nodeDao.flush();
        m_outageDao.flush();
        

        return outage;
    }

    private OnmsOutage getOutage(OnmsMonitoredService monitoredService, OnmsEvent event) {
        OnmsOutage outage = new OnmsOutage();
        outage.setMonitoredService(monitoredService);
        outage.setServiceLostEvent(event);
        outage.setIfLostService(new Date());
        m_outageDao.save(outage);
        return outage;
    }

    private OnmsEvent getEvent() {
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(m_distPollerDao.load("localhost"));
        event.setEventUei("foo!");
        event.setEventTime(new Date());
        event.setEventCreateTime(new Date());
        event.setEventSeverity(1);
        event.setEventSource("your mom");
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        m_eventDao.save(event);
        return event;
    }

    private OnmsMonitoredService getMonitoredService(OnmsIpInterface ipInterface, OnmsServiceType serviceType) {
        final OnmsCriteria criteria = new OnmsCriteria(OnmsMonitoredService.class)
            .add(Restrictions.eq("ipInterface", ipInterface))
            .add(Restrictions.eq("serviceType", serviceType));
        final List<OnmsMonitoredService> services = m_monitoredServiceDao.findMatching(criteria);
        OnmsMonitoredService monitoredService;
        if (services.size() > 0) {
            monitoredService = services.get(0);
        } else {
            monitoredService = new OnmsMonitoredService(ipInterface, serviceType);
        }
        m_monitoredServiceDao.save(monitoredService);
        return monitoredService;
    }

    private OnmsServiceType getServiceType(final String serviceName) {
        OnmsServiceType serviceType = m_serviceTypeDao.findByName(serviceName);
        assertNotNull(serviceType);
        return serviceType;
    }

    private OnmsIpInterface getIpInterface(String ipAddr, OnmsNode node) {
        OnmsIpInterface ipInterface = m_ipInterfaceDao.findByNodeIdAndIpAddress(node.getId(), ipAddr);
        if (ipInterface == null) {
            ipInterface = new OnmsIpInterface(ipAddr, node);
            ipInterface.setIsManaged("M");
            m_ipInterfaceDao.save(ipInterface);
        }
        return ipInterface;
    }
}
