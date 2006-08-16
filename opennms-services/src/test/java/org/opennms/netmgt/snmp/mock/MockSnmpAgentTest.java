package org.opennms.netmgt.snmp.mock;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.MockLogAppender;
import org.opennms.netmgt.mock.MockUtil;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

public class MockSnmpAgentTest extends TestCase {
	
	private MockSnmpAgent agt;

    @Override
	protected void setUp() throws Exception {
		super.setUp();
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
		MockLogAppender.setupLogging();

		agt = MockSnmpAgent.createAgentAndRun(
				new File("target/test-classes/org/opennms/netmgt/snmp/mock/loadSnmpDataTest.properties"),
				"127.0.0.1/1691");	// Homage to Empire
	}
	
    @Override
	public void runTest() throws Throwable {
		super.runTest();
		MockLogAppender.assertNoWarningsOrGreater();
	}
	
	@Override
	protected void tearDown() throws Exception {
        agt.shutDownAndWait();
		super.tearDown();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
	}

	/**
	 * Make sure that we can setUp() and tearDown() the agent.
	 */
	public void testAgentSetup() {
		assertNotNull("agent should be non-null", agt);
	}
	
	/**
	 * Test that we can setUp() and tearDown() twice to ensure that the
	 * MockSnmpAgent tears itself down properly. In particular, we want to make
	 * sure that the UDP listener gets torn down so listening port is free for
	 * later instances of the agent.
	 * 
	 * @throws Exception
	 */
	public void testSetUpTearDownTwice() throws Exception {
		// don't need the first setUp(), since it's already been done by JUnit
		tearDown();
		setUp();
		// don't need the second tearDown(), since it will be done by JUnit
	}

	public void testGet() throws IOException, InterruptedException {
		PDU pdu = new PDU();
		pdu.add(new VariableBinding(new OID("1.3.5.1.1.1.0.1")));
		pdu.setType(PDU.GET);
		
		CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString("public"));
		target.setAddress(new UdpAddress(InetAddress.getByName("127.0.0.1"), 1691));
		target.setVersion(SnmpConstants.version1);
		
		TransportMapping transport = null;
		try {
			transport = new DefaultUdpTransportMapping();
			Snmp snmp = new Snmp(transport);
			transport.listen();

			ResponseEvent response = snmp.send(pdu, target);
			if (response.getResponse() == null) {
				fail("request timed out");
			}
		} finally {	
			if (transport != null) {
				transport.close();
			}
		}
	}
}
