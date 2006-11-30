package org.opennms.netmgt.poller.remote;

import java.io.File;
import java.util.Properties;

import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.test.BaseIntegrationTestCase;
import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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
                },
                false
        );
        
        Properties props = new Properties();
        props.setProperty("configCheckTrigger.repeatInterval", "1000");
        
        PropertyOverrideConfigurer testPropertyConfigurer = new PropertyOverrideConfigurer();
        testPropertyConfigurer.setProperties(props);
        m_frontEndContext.addBeanFactoryPostProcessor(testPropertyConfigurer);
        
        m_frontEndContext.refresh();
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
        EventIpcManagerFactory.setIpcManager(new MockEventIpcManager());
        String configFile = "/tmp/remote-poller.configuration";
        File config = new File(configFile);
        config.delete();
        System.setProperty("opennms.poller.configuration.resource", "file://"+configFile);
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
        
        assertEquals(1, getJdbcTemplate().queryForInt("select count(*) from location_monitors where id=?", monitorId));
        assertEquals(5, getJdbcTemplate().queryForInt("select count(*) from location_monitor_details where locationMonitorId = ?", monitorId));

        assertEquals(System.getProperty("os.name"), getJdbcTemplate().queryForObject("select propertyValue from location_monitor_details where locationMonitorId = ? and property = ?", String.class, monitorId, "os.name"));
        
        Thread.sleep(10000);
        
        assertEquals(0, getJdbcTemplate().queryForInt("select count(*) from location_monitors where status='UNRESPONSIVE' and id=?", monitorId));
        
        m_frontEnd.stop();
        assertTrue("Could not found any pollResults", 0 < getJdbcTemplate().queryForInt("select count(*) from location_specific_status_changes where locationMonitorId = ?", monitorId));
        
      
    }
    
}
