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

package org.opennms.netmgt.dao.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.LocationMonitorIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
public class LocationMonitorDaoHibernateTest implements InitializingBean {
	@Autowired
	private LocationMonitorDao m_locationMonitorDao;
	
	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private DatabasePopulator m_databasePopulator;
	
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	@Test
	@Transactional
	public void testSaveLocationMonitor() {
    	Map <String, String> pollerDetails = new HashMap<String, String>();
    	pollerDetails.put("os.name", "BogOS");
    	pollerDetails.put("os.version", "sqrt(-1)");
    	
    	OnmsLocationMonitor mon = new OnmsLocationMonitor();
    	mon.setStatus(MonitorStatus.STARTED);
    	mon.setLastCheckInTime(new Date());
    	mon.setDetails(pollerDetails);
    	mon.setDefinitionName("RDU");
    	
    	m_locationMonitorDao.save(mon);
    	
    	m_locationMonitorDao.flush();
    	m_locationMonitorDao.clear();

    	OnmsLocationMonitor mon2 = m_locationMonitorDao.get(mon.getId());
    	assertNotSame(mon, mon2);
    	assertEquals(mon.getStatus(), mon2.getStatus());
    	assertEquals(mon.getLastCheckInTime(), mon2.getLastCheckInTime());
    	assertEquals(mon.getDefinitionName(), mon2.getDefinitionName());
    	assertEquals(mon.getDetails(), mon2.getDetails());
    }
    
    
    
    @Test
	@Transactional
	public void testSetConfigResourceProduction() throws FileNotFoundException {
        ((LocationMonitorDaoHibernate)m_locationMonitorDao).setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("monitoring-locations.xml")));
    }
    
	@Test
	@Transactional
    public void testSetConfigResourceExample() throws FileNotFoundException {
    	((LocationMonitorDaoHibernate)m_locationMonitorDao).setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("examples/monitoring-locations.xml")));
    }
    
	@Test
	@Transactional
    public void testSetConfigResourceNoLocations() throws FileNotFoundException {
    	((LocationMonitorDaoHibernate)m_locationMonitorDao).setMonitoringLocationConfigResource(new FileSystemResource("src/test/resources/monitoring-locations-no-locations.xml"));
    }

    
	@Test
	@Transactional
    public void testBogusConfig() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new MarshallingResourceFailureException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
        	((LocationMonitorDaoHibernate)m_locationMonitorDao).setMonitoringLocationConfigResource(new FileSystemResource("some bogus filename"));
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

	@Test
	@Transactional
    public void testFindMonitoringLocationDefinitionNull() throws FileNotFoundException {
    	((LocationMonitorDaoHibernate)m_locationMonitorDao).setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("monitoring-locations.xml")));
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException(ThrowableAnticipator.IGNORE_MESSAGE));
        try {
            m_locationMonitorDao.findMonitoringLocationDefinition(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }
    
	@Test
	@Transactional
    public void testFindMonitoringLocationDefinitionBogus() throws FileNotFoundException {
    	((LocationMonitorDaoHibernate)m_locationMonitorDao).setMonitoringLocationConfigResource(new InputStreamResource(ConfigurationTestUtils.getInputStreamForConfigFile("monitoring-locations.xml")));
        assertNull("should not have found monitoring location definition--"
                   + "should have returned null",
                   m_locationMonitorDao.findMonitoringLocationDefinition("bogus"));
    }
    
	@Test
	@Transactional
    public void testFindStatusChangesForNodeForUniqueMonitorAndInterface() {
		m_databasePopulator.populateDatabase();
		
        OnmsLocationMonitor monitor1 = new OnmsLocationMonitor();
        monitor1.setDefinitionName("Outer Space");
        m_locationMonitorDao.save(monitor1);

        OnmsLocationMonitor monitor2 = new OnmsLocationMonitor();
        monitor2.setDefinitionName("Really Outer Space");
        m_locationMonitorDao.save(monitor2);

        OnmsNode node1 = m_nodeDao.get(1);
        assertNotNull("node 1 should not be null", node1);

        OnmsNode node2 = m_nodeDao.get(2);
        assertNotNull("node 2 should not be null", node2);
        
        // Add node1/192.168.1.1 on monitor1
        addStatusChangesForMonitorAndService(monitor1, node1.getIpInterfaceByIpAddress("192.168.1.1").getMonitoredServices());
        
        // Add node1/192.168.1.2 on monitor1
        addStatusChangesForMonitorAndService(monitor1, node1.getIpInterfaceByIpAddress("192.168.1.2").getMonitoredServices());
        
        // Add node1/192.168.1.1 on monitor2
        addStatusChangesForMonitorAndService(monitor2, node1.getIpInterfaceByIpAddress("192.168.1.1").getMonitoredServices());
        
        // Add node1/fe80:0000:0000:0000:aaaa:bbbb:cccc:dddd%5 on monitor1
        addStatusChangesForMonitorAndService(monitor1, node1.getIpInterfaceByIpAddress("fe80::aaaa:bbbb:cccc:dddd%5").getMonitoredServices());
        
        // Add node2/192.168.2.1 on monitor1 to test filtering on a specific node (this shouldn't show up in the results)
        addStatusChangesForMonitorAndService(monitor1, node2.getIpInterfaceByIpAddress("192.168.2.1").getMonitoredServices());

        // Add another copy for node1/192.168.1.1 on monitor1 to test distinct
        addStatusChangesForMonitorAndService(monitor1, node1.getIpInterfaceByIpAddress("192.168.1.1").getMonitoredServices());
        
        Collection<LocationMonitorIpInterface> statuses = m_locationMonitorDao.findStatusChangesForNodeForUniqueMonitorAndInterface(1);
        assertEquals("number of statuses found", 4, statuses.size());

        /*
        for (LocationMonitorIpInterface status : statuses) {
            OnmsLocationMonitor m = status.getLocationMonitor();
            OnmsIpInterface i = status.getIpInterface();
            
            System.err.println("monitor " + m.getId() + " " + m.getDefinitionName() + ", IP " + i.getIpAddress());
        }
        */

    }

    private void addStatusChangesForMonitorAndService(OnmsLocationMonitor monitor, Set<OnmsMonitoredService> services) {
        for (OnmsMonitoredService service : services) {
            OnmsLocationSpecificStatus status = new OnmsLocationSpecificStatus();
            status.setLocationMonitor(monitor);
            status.setMonitoredService(service);
            status.setPollResult(PollStatus.available());
            m_locationMonitorDao.saveStatusChange(status);
            //System.err.println("Adding status for " + status.getMonitoredService() + " from " + status.getLocationMonitor().getId());
        }
    }
}
