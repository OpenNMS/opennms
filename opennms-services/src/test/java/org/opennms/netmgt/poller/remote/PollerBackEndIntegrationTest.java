package org.opennms.netmgt.poller.remote;

import java.io.File;
import java.util.Collection;

import org.hibernate.SessionFactory;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public class PollerBackEndIntegrationTest extends
        AbstractTransactionalDataSourceSpringContextTests {

    private PollerBackEnd m_backEnd;
    private SessionFactory m_sessionFactory;
    
    @Override
    protected String[] getConfigLocations() {
        System.setProperty("test.overriden.properties", "file:src/test/test-configurations/PollerBackEndIntegrationTest/test.overriden.properties");
        System.setProperty("opennms.home", "src/test/test-configurations/PollerBackEndIntegrationTest");
        return new String[] { 
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml"
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
        assertNotNull(locations);
        assertFalse(locations.isEmpty());
        
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
        
        m_backEnd.pollerStarting(locationMonitorId);
        
        assertEquals("STARTED", queryForString("select status from location_monitors where id = ?", locationMonitorId));
        
    }
    
    public void testPollingStopped() {

        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        
        m_backEnd.pollerStarting(locationMonitorId);
        
        assertEquals("STARTED", queryForString("select status from location_monitors where id = ?", locationMonitorId));

        m_backEnd.pollerStopping(locationMonitorId);
        
        assertEquals("STOPPED", queryForString("select status from location_monitors where id = ?", locationMonitorId));
        
    }
    
    public void testPollerUnresponsive() throws Exception {

        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        
        m_backEnd.pollerStarting(locationMonitorId);
        
        assertEquals("STARTED", queryForString("select status from location_monitors where id = ?", locationMonitorId));
        
        
        Thread.sleep(1500);

        m_backEnd.checkforUnresponsiveMonitors();

        assertEquals("STARTED", queryForString("select status from location_monitors where id = ?", locationMonitorId));

        Thread.sleep(2000);
        
        m_backEnd.checkforUnresponsiveMonitors();
        
        assertEquals("UNRESPONSIVE", queryForString("select status from location_monitors where id = ?", locationMonitorId));
        
    }

    
    public void testReportResults() {
        
        int locationMonitorId = m_backEnd.registerLocationMonitor("RDU");
        int serviceId = findServiceId();
        
        String ipAddr = queryForString("select ipaddr from ifservices where id = ?", serviceId);
        
        // make sure there is no rrd data
        File rrdFile = new File("target/test-data/RDU-"+locationMonitorId+"/"+ipAddr+"/http.rrd");
        if (rrdFile.exists())
            rrdFile.delete();
        
        assertFalse(rrdFile.exists());
        
        PollStatus status = PollStatus.available(1234);
        
        m_backEnd.reportResult(locationMonitorId, serviceId, status);
        
        assertEquals(1, queryForInt("select count(*) from location_specific_status_changes"));
        
        assertTrue(rrdFile.exists());
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
}
