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
// Modifications:
//
// 2008 Jun 05: Remove Thread.sleep in testAgentSetup (functinality
//              now exists to do the same in MockSnmpAgent with a
//              system property) and enhance asserts. - dj@opennms.org
// 2008 Jun 05: Format code, eliminate warnings. - dj@opennms.org
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
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.UsmUser;
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

    private MockSnmpAgent m_agent;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        MockLogAppender.setupLogging();

        m_agent = MockSnmpAgent.createAgentAndRun(new ClassPathResource("loadSnmpDataTest.properties"), "127.0.0.1/1691");	// Homage to Empire
    }

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Override
    protected void tearDown() throws Exception {
        m_agent.shutDownAndWait();
        super.tearDown();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
    }

    /**
     * Make sure that we can setUp() and tearDown() the agent.
     * @throws InterruptedException 
     */
    public void testAgentSetup() {
        assertNotNull("agent should be non-null", m_agent);
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

    public void testGetNext() throws Exception {
        assertResultFromGetNext("1.3.5.1.1.3", "1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
    }

    public void testGet() throws Exception {
        assertResultFromGet("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));

        m_agent.updateValue("1.3.5.1.1.3.0", new Integer32(77));

        assertResultFromGet("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(77));

    }

    private void assertResultFromGet(String oidStr, int expectedSyntax, Integer32 expected) throws Exception {
        assertResult(PDU.GET, oidStr, oidStr, expectedSyntax, expected);
    }

    private void assertResultFromGetNext(String oidStr, String expectedOid, int expectedSyntax, Integer32 expected) throws UnknownHostException, IOException {
        assertResult(PDU.GETNEXT, oidStr, expectedOid, expectedSyntax, expected);
    }

    private void assertResult(int pduType, String oidStr, String expectedOid, int expectedSyntax, Integer32 expected) throws UnknownHostException, IOException {
        assertV3Result(pduType, oidStr, expectedOid, expectedSyntax, expected);
    }

    @SuppressWarnings("unused")
    private void assertV1Result(int pduType, String oidStr, String expectedOid, int expectedSyntax, Integer32 expected) throws UnknownHostException, IOException {
        PDU pdu = new PDU();
        OID oid = new OID(oidStr);
        pdu.add(new VariableBinding(oid));
        pdu.setType(pduType);

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
            assertEquals(new OID(expectedOid), vb.getOid());
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

    @SuppressWarnings("unused")
    private void assertV2Result(int pduType, String oidStr, String expectedOid, int expectedSyntax, Integer32 expected) throws UnknownHostException, IOException {
        PDU pdu = new PDU();
        OID oid = new OID(oidStr);
        pdu.add(new VariableBinding(oid));
        pdu.setType(pduType);

        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(new UdpAddress(InetAddress.getByName("127.0.0.1"), 1691));
        target.setVersion(SnmpConstants.version2c);

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
            assertEquals(new OID(expectedOid), vb.getOid());
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

    private void assertV3Result(int pduType, String oidStr, String expectedOid, int expectedSyntax, Integer32 expected) throws UnknownHostException, IOException {
        PDU pdu = new ScopedPDU();
        OID oid = new OID(oidStr);
        pdu.add(new VariableBinding(oid));
        pdu.setType(pduType);

        OctetString userId = new OctetString("opennmsUser");
        OctetString pw = new OctetString("0p3nNMSv3");

        UserTarget target = new UserTarget();
        target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
        target.setSecurityName(userId);
        target.setAddress(new UdpAddress(InetAddress.getByName("127.0.0.1"), 1691));
        target.setVersion(SnmpConstants.version3);
        target.setTimeout(5000);
        
        TransportMapping transport = null;
        try {
            transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);

            UsmUser user = new UsmUser(userId, AuthMD5.ID, pw, PrivDES.ID, pw);
            snmp.getUSM().addUser(userId, user);

            transport.listen();
            
            ResponseEvent e = snmp.send(pdu, target);
            PDU response = e.getResponse();
            assertNotNull("request timed out", response);
            MockUtil.println("Response is: "+response);
            assertEquals("unexpected report pdu", PDU.REPORT, response.getType());
            
            VariableBinding vb = response.get(0);
            assertNotNull("variable binding should not be null", vb);
            Variable val = vb.getVariable();
            assertNotNull("variable should not be null", val);
            assertEquals("OID (value: " + val + ")", new OID(expectedOid), vb.getOid());
            assertEquals("syntax", expectedSyntax, vb.getSyntax());
            assertEquals("value", expected, val);

        } finally { 
            if (transport != null) {
                transport.close();
            }
        }
    }




}
