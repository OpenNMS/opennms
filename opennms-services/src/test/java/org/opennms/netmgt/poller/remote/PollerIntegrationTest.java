package org.opennms.netmgt.poller.remote;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

public class PollerIntegrationTest extends
		AbstractDependencyInjectionSpringContextTests {

    
    public PollerIntegrationTest() {
        System.setProperty("opennms.home", "src/test/test-configurations/PollerBackEndIntegrationTest");
    }

	protected String[] getConfigLocations() {
		return new String[] { 
                "classpath:/META-INF/opennms/applicationContext-dao.xml",
                // FIXME
               // "classpath:/META-INF/opennms/applicationContext-pollerBackEnd.xml",
               // "classpath:/META-INF/opennms/applicationContext-ws-svclayer.xml"
               };
	}
	
//	public void setPoller(Poller poller) {
//		m_poller = poller;
//	}
	
	public void testPoller() throws Exception {
		
		Thread.sleep(10000);
	}

}
