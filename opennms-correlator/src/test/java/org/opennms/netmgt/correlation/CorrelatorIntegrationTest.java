package org.opennms.netmgt.correlation;

import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

public class CorrelatorIntegrationTest extends
		AbstractTransactionalDataSourceSpringContextTests {
	
	private MockEventIpcManager m_eventIpcMgr;


	public CorrelatorIntegrationTest() {
		System.setProperty("opennms.home", "src/test/opennms-home");
		
		m_eventIpcMgr = new MockEventIpcManager();
		EventIpcManagerFactory.setIpcManager(m_eventIpcMgr);
	}

	@Override
	protected String[] getConfigLocations() {
		return new String[] {
				"META-INF/opennms/applicationContext-dao.xml",
				"META-INF/opennms/applicationContext-correlator.xml",
		};
	}
	
	
	public void testIt() {
		
		
		
	}

}
