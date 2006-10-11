package org.opennms.netmgt.poller.remote;

import org.opennms.netmgt.test.BaseIntegrationTestCase;

public class PollerFrontEndIntegrationTest extends BaseIntegrationTestCase {

    private PollerFrontEnd m_frontEnd;
    private PollerSettings m_settings;

    @Override
    protected String[] getConfigLocations() {
        System.setProperty("test.overridden.properties", "file:src/test/test-configurations/PollerBackEndIntegrationTest/test.overridden.properties");
        System.setProperty("opennms.home", "src/test/test-configurations/PollerBackEndIntegrationTest");
        return new String[] { 
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml",
                "classpath:/META-INF/opennms/applicationContext-pollerFrontEnd.xml",
        };
    }
    
    public void setPollerFrontEnd(PollerFrontEnd frontEnd) {
        m_frontEnd = frontEnd;
    }
    
    public void setPollerSettings(PollerSettings settings) {
        m_settings = settings;
    }
    
    public void testRegister() throws Exception {
       
        assertFalse(m_frontEnd.isRegistered());
        
        m_frontEnd.register("RDU");
        
        assertTrue(m_frontEnd.isRegistered());
        assertEquals(1, queryForInt("select count(*) from location_monitors where id=?", m_settings.getMonitorId()));
        
    }
    
    public void testY() throws Exception {
        
        m_frontEnd.register("RDU");
        
     
        
    }

}
