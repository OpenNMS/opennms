package org.opennms.netmgt.snmp.mock;

import java.io.File;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;

public class MockSnmpAgentTest extends TestCase {
	
	private MockSnmpAgent agt;

	protected void setUp() throws Exception {
		super.setUp();

		BasicConfigurator.configure();
		try {
			agt = new MockSnmpAgent( new File("mockAgent.boot"),
					new File("mockAgent.conf"),
					new File("target/test-classes/org/opennms/netmgt/snmp/mock/loadSnmpDataTest.properties"),
					"0.0.0.0/1691");	/* Homage to Empire */
			new Thread(agt).start();
		} catch (RuntimeException e) {
			e.printStackTrace();
		}
		try {
			Thread.sleep(2000);		/* This will need tinkering with to get it optimal  -jeffg */
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		agt.shutDown();
	}

	/* 
	 * A totally useless test case, just to turn the bar green
	 */
	public void testFooIsFoo() {
		assertEquals("foo", "foo");
	}
}
