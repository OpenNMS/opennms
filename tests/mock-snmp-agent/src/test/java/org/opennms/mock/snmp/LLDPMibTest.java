/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.mock.snmp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.InetAddress;
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
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
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
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;


@RunWith(Parameterized.class)
public class LLDPMibTest  {
	
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
	private long m_timeout = -1; // -1 means use the default
	
	public LLDPMibTest(int version) {
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
        MPv3.setEnterpriseID(5813);
        m_usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(m_usm);

        m_agent = MockSnmpAgent.createAgentAndRun(classPathResource("penrose-lldp-mib.properties"), "127.0.0.1/1691");	
        
        m_requestedVarbinds = new ArrayList<AnticipatedRequest>();
    }
    
    @After
    public void tearDown() throws Exception {
    	if (m_agent != null) {
    		m_agent.shutDownAndWait();
    	}
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
    	
    	request(".1.0.8802.1.1.2.1.3.1").andExpect(".1.0.8802.1.1.2.1.3.1.0", SMIConstants.SYNTAX_INTEGER32, new Integer32(4));
    	
    	doGetNext();
    	
    }

    @Test
    public void testGetNextMultipleVarbinds() throws Exception {
    	
    	request(".1.0.8802.1.1.2.1.3.1").andExpect(".1.0.8802.1.1.2.1.3.1.0", SMIConstants.SYNTAX_INTEGER32, new Integer32(4));

    	doGetNext();

    	m_agent.getUsm().setEngineBoots(15);
    	
    	byte[] hexString = new byte[] {  (byte)0x80, (byte)0x71, (byte)0x1F, (byte)0x8F, (byte)0xAF, (byte)0xC0 };
    	request(".1.0.8802.1.1.2.1.3.1").andExpect(".1.0.8802.1.1.2.1.3.1.0", SMIConstants.SYNTAX_INTEGER32, new Integer32(4));
    	request(".1.0.8802.1.1.2.1.3.2").andExpect(".1.0.8802.1.1.2.1.3.2.0", SMIConstants.SYNTAX_OCTET_STRING, new OctetString(hexString));
    	request(".1.0.8802.1.1.2.1.3.3").andExpect(".1.0.8802.1.1.2.1.3.3.0", SMIConstants.SYNTAX_OCTET_STRING, new OctetString("penrose-mx480".getBytes()));
    	
    	doGetNext();

        // This statement breaks the internal state of the SNMP4J agent
        // m_agent.getUsm().setLocalEngine(m_agent.getUsm().getLocalEngineID(), 15, 200);
        m_agent.getUsm().removeEngineTime(m_usm.getLocalEngineID());
        m_usm.removeEngineTime(m_agent.getUsm().getLocalEngineID());

    	request(".1.0.8802.1.1.2.1.3.1").andExpect(".1.0.8802.1.1.2.1.3.1.0", SMIConstants.SYNTAX_INTEGER32, new Integer32(4));
    	doGetNext();

    }
    
    private void doGetNext() throws Exception {
    	requestAndVerifyResponse(PDU.GETNEXT, m_version);
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

	private PDU sendRequestV1V2(PDU pdu, int version) throws Exception {
		PDU response;
		CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString("public"));
        target.setAddress(new UdpAddress(InetAddress.getByName("127.0.0.1"), 1691));
		target.setVersion(version);
		if (m_timeout > 0) { 
			target.setTimeout(m_timeout);
		}

        TransportMapping transport = null;
        try {
            transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();

            ResponseEvent e = snmp.send(pdu, target);
            response = e.getResponse();
        } finally { 
            if (transport != null) {
                transport.close();
            }
        }
		return response;
	}

	private PDU sendRequestV3(PDU pdu) throws IOException {
		PDU response;
    	
        OctetString userId = new OctetString("opennmsUser");
        OctetString pw = new OctetString("0p3nNMSv3");

        UserTarget target = new UserTarget();
        target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
        target.setSecurityName(userId);
        target.setAddress(new UdpAddress(InetAddress.getByName("127.0.0.1"), 1691));
        target.setVersion(SnmpConstants.version3);
		if (m_timeout > 0) { 
			target.setTimeout(m_timeout);
		} else { 
			target.setTimeout(5000);
		}
        
        TransportMapping transport = null;
        try {
            USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
            SecurityModels.getInstance().addSecurityModel(usm);
            transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);

            UsmUser user = new UsmUser(userId, AuthMD5.ID, pw, PrivDES.ID, pw);
            snmp.getUSM().addUser(userId, user);

            transport.listen();
            
            ResponseEvent e = snmp.send(pdu, target);
            response = e.getResponse();
        } finally { 
            if (transport != null) {
                transport.close();
            }
        }
		return response;
	}
	
	private URL classPathResource(String path) {
		return getClass().getClassLoader().getResource(path);
	}


}
