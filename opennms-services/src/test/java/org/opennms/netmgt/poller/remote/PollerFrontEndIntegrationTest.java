package org.opennms.netmgt.poller.remote;

import java.io.File;

import org.opennms.netmgt.test.BaseIntegrationTestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

public class PollerFrontEndIntegrationTest extends BaseIntegrationTestCase {

    private PollerFrontEnd m_frontEnd;
    private PollerSettings m_settings;
    private ClassPathXmlApplicationContext m_frontEndContext;
    
    

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        
        m_frontEndContext = new ClassPathXmlApplicationContext(
                new String[] { 
                        "classpath:/META-INF/opennms/applicationContext-remotePollerBackEnd.xml",
                        "classpath:/META-INF/opennms/applicationContext-pollerFrontEnd.xml",
                }
        );
        
        m_frontEnd = (PollerFrontEnd)m_frontEndContext.getBean("pollerFrontEnd");
        m_settings = (PollerSettings)m_frontEndContext.getBean("pollerSettings");
        
    }
    
    

    @Override
    protected void onTearDown() throws Exception {
        m_frontEndContext.stop();
        m_frontEndContext.close();
        super.onTearDown();
    }



    @Override
    protected String[] getConfigLocations() {
        String configFile = "/tmp/remote-poller.configuration";
        File config = new File(configFile);
        config.delete();
        System.setProperty("opennms.configuration.resource", "file://"+configFile);
        System.setProperty("test.overridden.properties", "file:src/test/test-configurations/PollerBackEndIntegrationTest/test.overridden.properties");
        System.setProperty("opennms.home", "src/test/test-configurations/PollerBackEndIntegrationTest");
        return new String[] {
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                "classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml",
                "classpath:/META-INF/opennms/applicationContext-exportedPollerBackEnd.xml",
        };

    }
    
    public void testRegister() throws Exception {
       
        assertFalse(m_frontEnd.isRegistered());
        
        m_frontEnd.register("RDU");
        
        assertTrue(m_frontEnd.isRegistered());
        Integer monitorId = m_settings.getMonitorId();
        assertEquals(1, queryForInt("select count(*) from location_monitors where id=?", monitorId));
        
        Thread.sleep(10000);
        
        // The monitor should not be marked unresponsive as it should be checking in with the server
        assertEquals(0, queryForInt("select count(*) from location_monitors where status='UNRESPONSIVE' and id=?", monitorId));
        
        m_frontEnd.stop();
        
        assertTrue("Could not found any pollResults", 0 < queryForInt("select count(*) from location_specific_status_changes where locationMonitorId = ?", monitorId));
    }
    
}
