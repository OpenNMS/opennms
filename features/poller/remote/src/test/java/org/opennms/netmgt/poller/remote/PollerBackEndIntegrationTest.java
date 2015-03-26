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

package org.opennms.netmgt.poller.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml",
        "classpath:/META-INF/opennms/applicationContext-exportedPollerBackEnd-http.xml",
        "classpath:/org/opennms/netmgt/poller/remote/applicationContext-configOverride.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
    "opennms.pollerBackend.monitorCheckInterval=500",
    "opennms.pollerBackend.disconnectedTimeout=3000"
})
@JUnitTemporaryDatabase
public class PollerBackEndIntegrationTest implements InitializingBean {
    
    @Resource(name="daemon")
    PollerBackEnd m_backEnd;
    
    @Autowired
    SessionFactory m_sessionFactory;
    
    @Autowired
    JdbcTemplate m_jdbcTemplate;
    
    @Autowired
    DistPollerDao m_distPollerDao;
    
    @Autowired
    NodeDao m_nodeDao;
    
    @Autowired
    ServiceTypeDao m_serviceTypeDao;

    @Autowired
    LocationMonitorDao m_locationMonitorDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp(){
        MockLogAppender.setupLogging();
    }
    
    @Test
    @Transactional
    public void testRegister() {
        
        final Collection<OnmsMonitoringLocationDefinition> locations = m_backEnd.getMonitoringLocations();
        assertNotNull("locations list should not be null", locations);
        assertFalse("locations list should not be empty", locations.isEmpty());

        final int initialCount = m_locationMonitorDao.findAll().size();
        
        for (final OnmsMonitoringLocationDefinition location : locations) {
            final int locationMonitorId = m_backEnd.registerLocationMonitor(location.getName());
            assertTrue(locationMonitorId > 0);
            assertEquals(MonitorStatus.REGISTERED, m_locationMonitorDao.get(locationMonitorId).getStatus());
        }
        
        assertEquals(initialCount + locations.size(), m_locationMonitorDao.findAll().size());
    }
    
    @Test
    @Transactional
    public void testPollingStarted() {
        final int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        
        m_backEnd.pollerStarting(locationMonitorId, getPollerDetails());

        final OnmsLocationMonitor monitor = m_locationMonitorDao.get(locationMonitorId);
        assertNotNull(monitor);
        final Map<String, String> details = monitor.getDetails();
        assertNotNull(details);
        assertEquals(MonitorStatus.STARTED, monitor.getStatus());
        assertEquals(2, details.keySet().size());
        assertEquals("WonkaOS", details.get("os.name"));
    }
    
    @Test
    @Transactional
    public void testPollingStopped() {

        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        
        m_backEnd.pollerStarting(locationMonitorId, getPollerDetails());
        
        assertEquals(MonitorStatus.STARTED, m_locationMonitorDao.get(locationMonitorId).getStatus());

        m_backEnd.pollerStopping(locationMonitorId);
        
        assertEquals(MonitorStatus.STOPPED, m_locationMonitorDao.get(locationMonitorId).getStatus());
    }
    
    @Test
    @Transactional
    public void testPollerDisconnected() throws Exception {

        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        
        m_backEnd.pollerStarting(locationMonitorId, getPollerDetails());

        assertEquals(MonitorStatus.STARTED, m_locationMonitorDao.get(locationMonitorId).getStatus());
        
        Thread.sleep(1500);

        m_backEnd.checkForDisconnectedMonitors();

        assertEquals(MonitorStatus.STARTED, m_locationMonitorDao.get(locationMonitorId).getStatus());

        Thread.sleep(2000);
        
        m_backEnd.checkForDisconnectedMonitors();
        
        assertEquals(MonitorStatus.DISCONNECTED, m_locationMonitorDao.get(locationMonitorId).getStatus());
        
    }
    
    @Test
    @Transactional
    public void testGetServiceMonitorLocators() {
        
        Collection<ServiceMonitorLocator> results = m_backEnd.getServiceMonitorLocators(DistributionContext.REMOTE_MONITOR);
        
        assertNotNull(results);
        
        assertTrue(results.size() > 0);
    }

    @Test
    @Transactional
    public void testReportResults() throws InterruptedException {
        final OnmsNode node = new OnmsNode(m_distPollerDao.findAll().get(0), "foo");
        final OnmsIpInterface iface = new OnmsIpInterface(InetAddressUtils.addr("192.168.1.1"), node);
        OnmsServiceType serviceType = m_serviceTypeDao.findByName("HTTP");
        if (serviceType == null) {
            serviceType = new OnmsServiceType("HTTP");
            m_serviceTypeDao.save(serviceType);
            m_serviceTypeDao.flush();
        }
        final OnmsMonitoredService service = new OnmsMonitoredService(iface, serviceType);
        iface.setMonitoredServices(Collections.singleton(service));
        m_nodeDao.save(node);
        m_nodeDao.flush();
        
        final int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        final int serviceId = service.getId();

        // make sure there is no rrd data
        final File rrdFile = new File("target/test-data/distributed/"+locationMonitorId+"/"+ InetAddressUtils.str(iface.getIpAddress()) +"/http" + RrdUtils.getExtension());
        if (rrdFile.exists()) {
            rrdFile.delete();
        }
        
        assertFalse(rrdFile.exists());

        m_backEnd.reportResult(locationMonitorId, serviceId, PollStatus.available(1234.0));
        Thread.sleep(1000);
        m_backEnd.reportResult(locationMonitorId, serviceId, PollStatus.unavailable());

        final Collection<OnmsLocationSpecificStatus> statuses = m_locationMonitorDao.getStatusChangesForLocationBetween(new Date(0L), new Date(), "RDU");
        assertEquals(2, statuses.size());

        final Iterator<OnmsLocationSpecificStatus> statusIterator = statuses.iterator();
        final OnmsLocationSpecificStatus status1 = statusIterator.next();
        final OnmsLocationSpecificStatus status2 = statusIterator.next();

        assertEquals(Double.valueOf(1234D), status1.getPollResult().getResponseTime());
        assertNull(status2.getPollResult().getResponseTime());

        assertTrue("rrd file doesn't exist at " + rrdFile.getAbsolutePath(), rrdFile.exists());
    }

    public Map<String, String> getPollerDetails() {
        final Map<String, String> pollerDetails = new HashMap<String, String>();
        pollerDetails.put("os.name", "WonkaOS");
        pollerDetails.put("os.version", "1.2.3");
        return pollerDetails;
    }
}
