/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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



package org.opennms.core.test.snmp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.test.mock.MockLogAppender;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * @author brozow
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners(JUnitSnmpAgentExecutionListener.class)
@JUnitSnmpAgent(resource="classpath:loadSnmpDataTest.properties")
public class JUnitSnmpAgentExecutionListenerTest {
    
    OID m_oid = new OID(".1.3.5.1.1.1.0");

    @Before
    public void setUp() {
    	MockLogAppender.setupLogging();
    }

    @Test
    public void testClassAgent() throws Exception {
        assertEquals(new OctetString("TestData"), get(localhost(), 9161, m_oid));
    }
    
    @Test(expected=java.util.concurrent.TimeoutException.class)
    @JUnitSnmpAgent(resource="classpath:loadSnmpDataTest.properties", port=9162)
    public void testNoClassAgent() throws Exception {
        assertEquals(new OctetString("TestData"), get(localhost(), 9161, m_oid));
    }
    
    @Test
    @JUnitSnmpAgent(resource="classpath:differentSnmpData.properties", port=9162)
    public void testMethodAgent() throws Exception {
        assertEquals(new OctetString("DifferentTestData"), get(localhost(), 9162, m_oid));
    }
    


    /**
     * @param localhost
     * @param i
     * @param string
     * @return
     * @throws IOException 
     */
    private Variable get(InetAddress localhost, int port, OID oid) throws Exception, IOException, TimeoutException {

        TransportMapping transport = new DefaultUdpTransportMapping();
        Snmp session = new Snmp(transport);
        session.listen();

        Target target = new CommunityTarget(new UdpAddress(localhost, port), new OctetString("public"));
        target.setTimeout(1000);
        target.setRetries(3);
        target.setVersion(SnmpConstants.version2c);
        
        PDU pdu = new PDU();
        pdu.setType(PDU.GET);
        pdu.addOID(new VariableBinding(oid));
        
        ResponseEvent e = session.get(pdu, target);
        
        PDU response = e.getResponse();
        if (response == null) {
            if (e.getError() != null) { 
                throw e.getError();
            } else {
                throw new TimeoutException("SNMP timed out to "+target);
            }

        }
        
        assertEquals(oid, response.get(0).getOid());

        return response.get(0).getVariable();
        
    }

    /**
     * This method needs to return the same value as the default value of the <code>host</code>
     * parameter inside 
     * {@link JUnitSnmpAgentExecutionListener#beforeTestMethod(org.springframework.test.context.TestContext)}
     * 
     * @return
     * @throws UnknownHostException 
     */
    private InetAddress localhost() throws UnknownHostException {
        return InetAddress.getLocalHost();
        // return InetAddressUtils.addr("127.0.0.1");
    }

}
