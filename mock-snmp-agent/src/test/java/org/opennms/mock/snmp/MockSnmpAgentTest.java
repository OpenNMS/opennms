//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.mock.snmp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.core.io.ClassPathResource;

public class MockSnmpAgentTest extends TestCase {
	
	private MockSnmpAgent agt;

    @Override
	protected void setUp() throws Exception {
		super.setUp();
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
		MockLogAppender.setupLogging();

		agt = MockSnmpAgent.createAgentAndRun(
				new ClassPathResource("loadSnmpDataTest.properties"),
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
        assertResultFromGet("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        
        agt.updateValue("1.3.5.1.1.3.0", new Integer32(77));
        
        assertResultFromGet("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(77));
        
	}

    private void assertResultFromGet(String oidStr, int expectedSyntax,
            Integer32 expected) throws UnknownHostException, IOException {
        PDU pdu = new PDU();
		OID oid = new OID(oidStr);
        pdu.add(new VariableBinding(oid));
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

			ResponseEvent e = snmp.send(pdu, target);
			PDU response = e.getResponse();
			assertNotNull("request timed out", response);
			
			VariableBinding vb = response.get(0);
			assertNotNull(vb);
			assertNotNull(vb.getVariable());
			assertEquals(oid, vb.getOid());
			assertEquals(expectedSyntax, vb.getSyntax());
			Variable val = vb.getVariable();
			assertNotNull(val);
			assertEquals(expected, val);

		} finally {	
			if (transport != null) {
				transport.close();
			}
		}
    }
}
