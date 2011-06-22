/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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

package org.opennms.netmgt.poller.remote;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml",
        "classpath:/META-INF/opennms/applicationContext-exportedPollerBackEnd-rmi.xml",
        "classpath:/org/opennms/netmgt/poller/remote/applicationContext-configOverride.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class PollerBackEndIntegrationTest{
    
    @Resource(name="daemon")
    PollerBackEnd m_backEnd;
    
    @Autowired
    SessionFactory m_sessionFactory;
    
    @Autowired
    JdbcTemplate m_jdbcTemplate;
    
    @Before
    public void setUp(){
        MockLogAppender.setupLogging();
    }
    
    @Test
    @Transactional
    public void testRegister() {
        
        Collection<OnmsMonitoringLocationDefinition> locations = m_backEnd.getMonitoringLocations();
        assertNotNull("locations list should not be null", locations);
        assertFalse("locations list should not be empty", locations.isEmpty());
        
        int initialCount = queryForInt("select count(*) from location_monitors");
        
        for (OnmsMonitoringLocationDefinition location : locations) {
            int locationMonitorId = m_backEnd.registerLocationMonitor(location.getName());
            assertTrue(locationMonitorId > 0);
            assertEquals("REGISTERED", queryForString("select status from location_monitors where id = ?", locationMonitorId));
        }
        
        
        assertEquals(initialCount + locations.size(), m_jdbcTemplate.queryForInt("select count(*) from location_monitors"));
        
    }
    
    @Test
    @Transactional
    public void testPollingStarted() {
        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        
        m_backEnd.pollerStarting(locationMonitorId, getPollerDetails());
        
        assertEquals("STARTED", queryForString("select status from location_monitors where id = ?", locationMonitorId));
        assertEquals(2, queryForInt("select count(*) from location_monitor_details where locationMonitorId = ?", locationMonitorId));
        assertEquals("WonkaOS", queryForString("select propertyValue from location_monitor_details where locationMonitorId = ? and property = ?", locationMonitorId, "os.name"));
    }
    
    @Test
    @Transactional
    public void testPollingStopped() {

        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        
        m_backEnd.pollerStarting(locationMonitorId, getPollerDetails());
        
        assertEquals("STARTED", queryForString("select status from location_monitors where id = ?", locationMonitorId));

        m_backEnd.pollerStopping(locationMonitorId);
        
        assertEquals("STOPPED", queryForString("select status from location_monitors where id = ?", locationMonitorId));
        
        
    }
    
    @Test
    @Transactional
    public void testPollerDisconnected() throws Exception {

        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        
        m_backEnd.pollerStarting(locationMonitorId, getPollerDetails());
        
        assertEquals("STARTED", queryForString("select status from location_monitors where id = ?", locationMonitorId));
        
        
        Thread.sleep(1500);

        m_backEnd.checkForDisconnectedMonitors();

        assertEquals("STARTED", queryForString("select status from location_monitors where id = ?", locationMonitorId));

        Thread.sleep(2000);
        
        m_backEnd.checkForDisconnectedMonitors();
        
        assertEquals("DISCONNECTED", queryForString("select status from location_monitors where id = ?", locationMonitorId));
        
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
        m_jdbcTemplate.execute("INSERT INTO node (nodeId, nodeCreateTime) VALUES (1, now())");
        m_jdbcTemplate.execute("INSERT INTO ipInterface (id, nodeId, ipAddr)  VALUES (1, 1, '192.168.1.1')");
        m_jdbcTemplate.execute("INSERT INTO service (serviceId, serviceName) VALUES (1, 'HTTP')");
        m_jdbcTemplate.execute("INSERT INTO ifServices (id, nodeId, ipAddr, serviceId, ipInterfaceId) VALUES (1, 1, '192.168.1.1', 1, 1)");
        
        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        int serviceId = findServiceId();
        
        String ipAddr = queryForString("select ipaddr from ifservices where id = ?", serviceId);
        
        // make sure there is no rrd data
        File rrdFile = new File("target/test-data/distributed/"+locationMonitorId+"/"+ipAddr+"/http" + RrdUtils.getExtension());
        if (rrdFile.exists()) {
            rrdFile.delete();
        }
        
        assertFalse(rrdFile.exists());

        m_backEnd.reportResult(locationMonitorId, serviceId, PollStatus.available(1234.0));
        Thread.sleep(1000);
        m_backEnd.reportResult(locationMonitorId, serviceId, PollStatus.unavailable());

        assertEquals(2, queryForInt("select count(*) from location_specific_status_changes where locationMonitorId = ?", locationMonitorId));

        assertTrue("rrd file doesn't exist at " + rrdFile.getAbsolutePath(), rrdFile.exists());
    }

    private int findServiceId() {
        return m_jdbcTemplate.queryForInt("select id from ifservices, service where ifservices.serviceid = service.serviceid and service.servicename='HTTP' limit 1");
    }

    private void flush() {
        m_sessionFactory.getCurrentSession().flush();
    }
    
    private String queryForString(String sql, Object... args) {
        flush();
        return (String) m_jdbcTemplate.queryForObject(sql, args, String.class);
    }
    
    public int queryForInt(String sql, Object... args) {
        flush();
        return m_jdbcTemplate.queryForInt(sql, args);
    }
    
    public Map<String, String> getPollerDetails() {
        Map<String, String> pollerDetails = new HashMap<String, String>();
        pollerDetails.put("os.name", "WonkaOS");
        pollerDetails.put("os.version", "1.2.3");
        return pollerDetails;
    }
}
