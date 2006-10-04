package org.opennms.netmgt.poller.remote;

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
        
        for (OnmsMonitoringLocationDefinition location : locations) {
            int locationId = m_backEnd.registerLocationMonitor(location.getName());
            assertTrue(locationId > 0);
        }
        
        flush();
        
        assertEquals(1, jdbcTemplate.queryForInt("select count(*) from location_monitors"));
        
    }
    
    
    public void testReportResults() {
        int locationMonitorID = m_backEnd.registerLocationMonitor("RDU");
        int serviceId = findServiceId();
        
        PollStatus status = PollStatus.available(1234);
        
        m_backEnd.reportResult(locationMonitorID, serviceId, status);
        
        flush();
        
        assertEquals(1, jdbcTemplate.queryForInt("select count(*) from location_specific_status_changes"));
    }

    private int findServiceId() {
        return jdbcTemplate.queryForInt("select id from ifservices, service where ifservices.serviceid = service.serviceid and service.servicename='HTTP' limit 1");
    }

    private void flush() {
        m_sessionFactory.getCurrentSession().flush();
    }
    
    
    
}
