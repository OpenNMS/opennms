package org.opennms.netmgt.poller.remote;

import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

public class PollerTest extends TestCase {
	
	Poller m_poller;
	PollerConfiguration m_config;

	protected void setUp() throws Exception {
		super.setUp();
		
		PollerConfiguration m_config = createMock(PollerConfiguration.class);
		
		m_poller = new Poller();
		m_poller.setPollerConfiguration(m_config);
	}
	
	public void testSchedule() throws Exception {
		
		
		
		m_poller.afterPropertiesSet();
		
	}

}
