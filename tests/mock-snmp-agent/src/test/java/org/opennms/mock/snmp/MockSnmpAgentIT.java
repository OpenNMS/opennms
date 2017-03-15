/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.mock.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.mock.snmp.responder.Sleeper;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.SNMP4JSettings;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;


@RunWith(Parameterized.class)
public class MockSnmpAgentIT  {
    @Parameters
    public static Collection<Object[]> versions() {
        return Arrays.asList(new Object[][] {
                { SnmpConstants.version1 },
                { SnmpConstants.version2c },
                { SnmpConstants.version3 },
        });
    }

    private MockSnmpAgent m_agent;
    private USM m_usm;
    private ArrayList<AnticipatedRequest> m_requestedVarbinds;
    private int m_version;

    private static long DEFAULT_TIMEOUT = 5000;

    public MockSnmpAgentIT(int version) {
        m_version = version;
    }

    private class AnticipatedRequest {
        private String m_requestedOid;
        private Variable m_requestedValue;
        private String m_expectedOid;
        private int m_expectedSyntax;
        private Variable m_expectedValue;

        public AnticipatedRequest(String requestedOid, Variable requestedValue) {
            m_requestedOid = requestedOid;
            m_requestedValue = requestedValue;
        }


        public void andExpect(String expectedOid, int expectedSyntax, Variable expectedValue) {
            m_expectedOid = expectedOid;
            m_expectedSyntax = expectedSyntax;
            m_expectedValue = expectedValue;
        }

        public VariableBinding getRequestVarbind() {
            OID oid = new OID(m_requestedOid);
            if (m_requestedValue != null) {
                return new VariableBinding(oid, m_requestedValue);
            } else {
                return new VariableBinding(oid);
            }
        }

        public void verify(VariableBinding vb) {
            assertNotNull("variable binding should not be null", vb);
            Variable val = vb.getVariable();
            assertNotNull("variable should not be null", val);
            assertEquals("OID (value: " + val + ")", new OID(m_expectedOid), vb.getOid());
            assertEquals("syntax", m_expectedSyntax, vb.getSyntax());
            assertEquals("value", m_expectedValue, val);
        }

    }


    @Before
    public void setUp() throws Exception {
        // Create a global USM that all client calls will use
        SNMP4JSettings.setEnterpriseID(5813);
        m_usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(m_usm);

        m_agent = MockSnmpAgent.createAgentAndRun(classPathResource("loadSnmpDataTest.properties"), "127.0.0.1/0");
        Thread.sleep(200);
        System.err.println("Started MockSnmpAgent on port " + m_agent.getPort());

        m_requestedVarbinds = new ArrayList<AnticipatedRequest>();
    }

    @After
    public void tearDown() throws Exception {
        reset();
        if (m_agent != null) {
            m_agent.shutDownAndWait();
        }
        Thread.sleep(200);
    }

    public AnticipatedRequest request(String requestedOid, Variable requestedValue) {
        AnticipatedRequest r = new AnticipatedRequest(requestedOid, requestedValue);
        m_requestedVarbinds.add(r);
        return r;
    }

    public AnticipatedRequest request(String requestOid) {
        return request(requestOid, null);
    }

    public void reset() {
        m_requestedVarbinds.clear();
    }


    /**
     * Make sure that we can setUp() and tearDown() the agent.
     * @throws InterruptedException 
     */
    @Test
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
    @Test
    public void testSetUpTearDownTwice() throws Exception {
        // don't need the first setUp(), since it's already been done by JUnit
        tearDown();
        setUp();
        // don't need the second tearDown(), since it will be done by JUnit
    }

    @Test
    public void testGetNext() throws Exception {

        request("1.3.5.1.1.3").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));

        doGetNext();
    }

    @Test
    public void testGet() throws Exception {

        request("1.3.5.1.1.3.0").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));

        doGet();

        m_agent.updateValue("1.3.5.1.1.3.0", new Integer32(77));

        request("1.3.5.1.1.3.0").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(77));

        doGet();

    }

    @Test
    public void testUpdateFromFile() throws Exception {
        request("1.3.5.1.1.3.0").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));

        doGet();

        m_agent.updateValuesFromResource(classPathResource("differentSnmpData.properties"));

        request("1.3.5.1.1.3.0").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(77));

        doGet();

    }

    @Test
    public void testGetNextMultipleVarbinds() throws Exception {

        request("1.3.5.1.1.3").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGetNext();

        m_agent.getUsm().setEngineBoots(15);

        request("1.3.5.1.1.3").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGetNext();

        request("1.3.5.1.1.3").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGetNext();

        // This statement breaks the internal state of the SNMP4J agent
        // m_agent.getUsm().setLocalEngine(m_agent.getUsm().getLocalEngineID(), 15, 200);
        m_agent.getUsm().removeEngineTime(m_usm.getLocalEngineID());
        m_usm.removeEngineTime(m_agent.getUsm().getLocalEngineID());

        request("1.3.5.1.1.3").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGetNext();

    }

    @Test
    public void testSet() throws Exception {

        final String oid = "1.3.5.1.1.3.0";


        // current value is 42
        request(oid).andExpect(oid, SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGet();

        // set a value of 17
        request(oid, new Integer32(17)).andExpect(oid, SMIConstants.SYNTAX_INTEGER32, new Integer32(17));
        doSet();

        // request new value and expect 17
        request(oid).andExpect(oid, SMIConstants.SYNTAX_INTEGER, new Integer32(17)); 
        doGet();

    }

    @Test
    public void testUpdateFromFileWithUSMTimeReset() throws Exception {
        request("1.3.5.1.1.3.0").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGet();

        m_agent.getUsm().setEngineBoots(15);

        request("1.3.5.1.1.3.0").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGet();
        request("1.3.5.1.1.3.0").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGet();

        // This statement breaks the internal state of the SNMP4J agent
        // m_agent.getUsm().setLocalEngine(m_agent.getUsm().getLocalEngineID(), 15, 200);
        m_agent.getUsm().removeEngineTime(m_usm.getLocalEngineID());
        m_usm.removeEngineTime(m_agent.getUsm().getLocalEngineID());

        request("1.3.5.1.1.3.0").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGet();
        request("1.3.5.1.1.3.0").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGet();

        m_usm.removeEngineTime(m_agent.getUsm().getLocalEngineID());

        request("1.3.5.1.1.3.0").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGet();
        request("1.3.5.1.1.3.0").andExpect("1.3.5.1.1.3.0", SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGet();
    }

    @Test
    public void testSineWaveResponder() throws Exception {
        String oid = "1.3.5.1.1.10.0";
        request(oid).andExpect(oid, SMIConstants.SYNTAX_INTEGER, new Integer32(0));
        doGet();

        oid = "1.3.5.1.1.10.30";
        request(oid).andExpect(oid, SMIConstants.SYNTAX_INTEGER, new Integer32(50));
        doGet();

        oid = "1.3.5.1.1.10.45";
        request(oid).andExpect(oid, SMIConstants.SYNTAX_INTEGER, new Integer32(71));
        doGet();

        oid = "1.3.5.1.1.10.90";
        request(oid).andExpect(oid, SMIConstants.SYNTAX_INTEGER, new Integer32(100));
        doGet();
    }

    @Test
    public void testSleeperResponder() throws Exception {
        final String myOid = "1.3.5.1.1.11.0";

        // Verify that the sleeper responds correctly
        Sleeper.getInstance().setVariable(new Integer32(1));
        request(myOid).andExpect(myOid, SMIConstants.SYNTAX_INTEGER, new Integer32(1));
        doGet();

        // Set the timeout
        Sleeper.getInstance().setSleepTime(DEFAULT_TIMEOUT + 1000);

        // Make another request
        PDU pdu = createPDU(m_version);
        OID oid = new OID(myOid);
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);

        PDU response = sendRequest(pdu,m_version);
        // Verify that the request does in fact timeout
        assertNull("request timed out", response);

        // Clear the timeout
        Sleeper.getInstance().setSleepTime(0);

        // Update the variable
        Sleeper.getInstance().setVariable(new OctetString("Bingo!"));
        request(myOid).andExpect(myOid, SMIConstants.SYNTAX_OCTET_STRING, new OctetString("Bingo!"));
        doGet();
    }

    @Test
    public void testDynamicVariableCacheAfterUpdateFromFile() throws Exception {
        final String oid = "1.3.5.1.1.10.0";
        request(oid).andExpect(oid, SMIConstants.SYNTAX_INTEGER, new Integer32(0));
        doGet();

        m_agent.updateValuesFromResource(classPathResource("differentSnmpData.properties"));

        Sleeper.getInstance().resetWithVariable(new Integer32(42));
        request(oid).andExpect(oid, SMIConstants.SYNTAX_INTEGER, new Integer32(42));
        doGet();
    }

    @Test
    public void testErrorResponder() throws Exception {
        // When using the Error responder, the resulting VariableBinding
        // should always be null
        String oid = "1.3.5.1.1.99.1";
        request(oid).andExpect(oid, SMIConstants.SYNTAX_NULL, new Null());
        doGet();

        oid = "1.3.5.1.1.99.2";
        request(oid).andExpect(oid, SMIConstants.SYNTAX_NULL, new Null());
        doGet();
    }

    private void doGet() throws Exception {
        requestAndVerifyResponse(PDU.GET, m_version);
    }

    private void doGetNext() throws Exception {
        requestAndVerifyResponse(PDU.GETNEXT, m_version);
    }

    private void doSet() throws Exception {
        requestAndVerifyResponse(PDU.SET, m_version);
    }

    private void requestAndVerifyResponse(int pduType, int version) throws Exception {
        PDU pdu = createPDU(version);

        for(AnticipatedRequest a : m_requestedVarbinds) {
            pdu.add(a.getRequestVarbind());
        }
        pdu.setType(pduType);

        PDU response = sendRequest(pdu, version);

        assertNotNull("request timed out", response);
        System.err.println("Response is: "+response);
        assertTrue("unexpected report pdu: " + ((VariableBinding)response.getVariableBindings().get(0)).getOid(), response.getType() != PDU.REPORT);

        assertEquals("Unexpected number of varbinds returned.", m_requestedVarbinds.size(), response.getVariableBindings().size());

        for(int i = 0; i < m_requestedVarbinds.size(); i++) {
            AnticipatedRequest a = m_requestedVarbinds.get(i);
            VariableBinding vb = response.get(i);
            a.verify(vb);
        }

        reset();

    }

    private PDU createPDU(int version) {
        if (version == SnmpConstants.version3) {
            return new ScopedPDU();
        } else {
            return new PDU();
        }
    }

    private PDU sendRequest(PDU pdu, int version) throws Exception {
        if (version == SnmpConstants.version3) {
            return sendRequestV3(pdu);
        } else {
            return sendRequestV1V2(pdu, version);
        }
    }

    private PDU sendRequestV1V2(PDU pdu, int version) {
        PDU response = null;
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(new UdpAddress(InetAddressUtils.addr("127.0.0.1"), m_agent.getPort()));
        target.setVersion(version);

        TransportMapping<UdpAddress> transport = null;
        Snmp snmp = null;
        try {
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            transport.listen();

            ResponseEvent e = snmp.send(pdu, target);
            response = e.getResponse();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally { 
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            if (transport != null) {
                try {
                    transport.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    private PDU sendRequestV3(PDU pdu) {
        PDU response = null;

        OctetString userId = new OctetString("opennmsUser");
        OctetString pw = new OctetString("0p3nNMSv3");

        UserTarget target = new UserTarget();
        target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
        target.setSecurityName(userId);
        target.setAddress(new UdpAddress(InetAddressUtils.addr("127.0.0.1"), m_agent.getPort()));
        target.setVersion(SnmpConstants.version3);
        target.setTimeout(DEFAULT_TIMEOUT);

        TransportMapping<UdpAddress> transport = null;
        Snmp snmp = null;
        try {
            USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
            SecurityModels.getInstance().addSecurityModel(usm);
            transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);

            UsmUser user = new UsmUser(userId, AuthMD5.ID, pw, PrivDES.ID, pw);
            snmp.getUSM().addUser(userId, user);

            transport.listen();

            ResponseEvent e = snmp.send(pdu, target);
            response = e.getResponse();
        } catch (final IOException e) {
            e.printStackTrace();
        } finally { 
            if (snmp != null) {
                try {
                    snmp.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            if (transport != null) {
                try {
                    transport.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    private URL classPathResource(String path) {
        return getClass().getClassLoader().getResource(path);
    }


}
