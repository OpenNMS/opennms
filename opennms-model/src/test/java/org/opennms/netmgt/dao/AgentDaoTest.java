package org.opennms.netmgt.dao;

import java.util.Collection;
import java.util.Iterator;

import org.opennms.netmgt.model.OnmsAgent;
import org.opennms.netmgt.model.OnmsSnmpAgent;

public class AgentDaoTest extends AbstractDaoTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testFindAgents() {
		Collection agents = getAgentDao().findAll();
		assertNotNull(agents);
		assertFalse(agents.isEmpty());
		System.err.println("Num agents = "+agents.size());
		for (Iterator it = agents.iterator(); it.hasNext();) {
			OnmsAgent agent = (OnmsAgent) it.next();
			assertTrue(agent instanceof OnmsSnmpAgent);
		}
	}

}
