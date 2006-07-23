package org.opennms.web.services;

import junit.framework.TestCase;

public class PollerServiceTest extends TestCase {
	
	DefaultPollerService m_pollerService;
	
	
	
	public void testPoll() {
		
		
		
		m_pollerService.poll(1, "192.168.1.1", 1, 3, 7);
	}

}
