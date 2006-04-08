package org.opennms.netmgt.dao;

import java.util.Collection;
import java.util.Iterator;

import org.opennms.netmgt.model.OnmsSnmpInterface;

public class SnmpInterfaceDaoTest extends AbstractDaoTestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testGet() throws Exception {
		Collection snmpIfs = getSnmpInterfaceDao().findAll();
		assertEquals(3, snmpIfs.size());
		for (Iterator it = snmpIfs.iterator(); it.hasNext();) {
			OnmsSnmpInterface snmpIf = (OnmsSnmpInterface) it.next();
			assertEquals(10000000, snmpIf.getIfSpeed().intValue());
			assertNotNull(snmpIf.getNode());
			assertEquals(1, snmpIf.getNode().getId().intValue());
			assertEquals("node1", snmpIf.getNode().getLabel());
			
		}
		
	}

}
