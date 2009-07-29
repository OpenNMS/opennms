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
// 2007 Apr 16: Don't use test.overridden.properties; use beans and override them instead. - dj@opennms.org
// 2007 Apr 05: Use DaoTestConfigBean to set system properties. - dj@opennms.org
// 2007 Apr 05: Set rrd.base.dir property. - dj@opennms.org
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.poller.remote;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.opennms.netmgt.dao.db.AbstractTransactionalTemporaryDatabaseSpringContextTests;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.test.DaoTestConfigBean;

public class PollerBackEndIntegrationTest extends AbstractTransactionalTemporaryDatabaseSpringContextTests {
    private PollerBackEnd m_backEnd;
    private SessionFactory m_sessionFactory;
    
    @Override
    protected void setUpConfiguration() {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();
    }
    
    @Override
    protected String[] getConfigLocations() {
        return new String[] {
                "classpath:/META-INF/opennms/mockEventIpcManager.xml",
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath*:/META-INF/opennms/component-dao.xml",
                "classpath:/META-INF/opennms/applicationContext-daemon.xml",
                "classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml",
                "classpath:/META-INF/opennms/applicationContext-exportedPollerBackEnd.xml",
                "classpath:/org/opennms/netmgt/poller/remote/applicationContext-configOverride.xml",
        };
    }
    
    public void setPollerBackEnd(PollerBackEnd backEnd) {
        m_backEnd = backEnd;
    }
    
    public void setSessionFactory(SessionFactory sessionFactory) {
        m_sessionFactory = sessionFactory;
    }
    
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
        
        
        assertEquals(initialCount + locations.size(), jdbcTemplate.queryForInt("select count(*) from location_monitors"));
        
    }

    public void testPollingStarted() {
        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        
        m_backEnd.pollerStarting(locationMonitorId, getPollerDetails());
        
        assertEquals("STARTED", queryForString("select status from location_monitors where id = ?", locationMonitorId));
        assertEquals(2, queryForInt("select count(*) from location_monitor_details where locationMonitorId = ?", locationMonitorId));
        assertEquals("WonkaOS", queryForString("select propertyValue from location_monitor_details where locationMonitorId = ? and property = ?", locationMonitorId, "os.name"));
    }
    
    public void testPollingStopped() {

        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        
        m_backEnd.pollerStarting(locationMonitorId, getPollerDetails());
        
        assertEquals("STARTED", queryForString("select status from location_monitors where id = ?", locationMonitorId));

        m_backEnd.pollerStopping(locationMonitorId);
        
        assertEquals("STOPPED", queryForString("select status from location_monitors where id = ?", locationMonitorId));
        
        
    }
    
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
    
    public void testGetServiceMonitorLocators() {
        
        Collection<ServiceMonitorLocator> results = m_backEnd.getServiceMonitorLocators(DistributionContext.REMOTE_MONITOR);
        
        assertNotNull(results);
        
        assertTrue(results.size() > 0);
    }

    
    public void testReportResults() {
        jdbcTemplate.execute("INSERT INTO node (nodeId, nodeCreateTime) VALUES (1, now())");
        jdbcTemplate.execute("INSERT INTO ipInterface (id, nodeId, ipAddr)  VALUES (1, 1, '192.168.1.1')");
        jdbcTemplate.execute("INSERT INTO service (serviceId, serviceName) VALUES (1, 'HTTP')");
        jdbcTemplate.execute("INSERT INTO ifServices (id, nodeId, ipAddr, serviceId, ipInterfaceId) VALUES (1, 1, '192.168.1.1', 1, 1)");
        
        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        int serviceId = findServiceId();
        
        String ipAddr = queryForString("select ipaddr from ifservices where id = ?", serviceId);
        
        // make sure there is no rrd data
        File rrdFile = new File("target/test-data/distributed/"+locationMonitorId+"/"+ipAddr+"/http" + RrdUtils.getExtension());
        if (rrdFile.exists()) {
            rrdFile.delete();
        }
        
        assertFalse(rrdFile.exists());
        
        PollStatus status = PollStatus.available(1234.0);
        
        m_backEnd.reportResult(locationMonitorId, serviceId, status);
        
        assertEquals(1, queryForInt("select count(*) from location_specific_status_changes where locationMonitorId = ?", locationMonitorId));
        
        assertTrue("rrd file doesn't exist at " + rrdFile.getAbsolutePath(), rrdFile.exists());
    }

    private int findServiceId() {
        return jdbcTemplate.queryForInt("select id from ifservices, service where ifservices.serviceid = service.serviceid and service.servicename='HTTP' limit 1");
    }

    private void flush() {
        m_sessionFactory.getCurrentSession().flush();
    }
    
    private String queryForString(String sql, Object... args) {
        flush();
        return (String) jdbcTemplate.queryForObject(sql, args, String.class);
    }
    
    public int queryForInt(String sql, Object... args) {
        flush();
        return jdbcTemplate.queryForInt(sql, args);
    }
    
    public Map<String, String> getPollerDetails() {
        Map<String, String> pollerDetails = new HashMap<String, String>();
        pollerDetails.put("os.name", "WonkaOS");
        pollerDetails.put("os.version", "1.2.3");
        return pollerDetails;
    }
}
