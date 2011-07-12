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



package org.opennms.netmgt.snmp;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.core.test.snmp.annotations.JUnitMockSnmpStrategyAgents;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpAgentConfigProxyMapper;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.support.ProxySnmpAgentConfigFactory;
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
public class JUnitSnmpAgentExecutionListenerTest {
    OID m_oid = new OID(".1.3.5.1.1.1.0");

    @Before
    public void setUp() throws Exception {
    	MockLogAppender.setupLogging();
    	SnmpPeerFactory.setInstance(new ProxySnmpAgentConfigFactory());
//    	LogUtils.debugf(this, "peer factory = %s", m_snmpPeerFactory.getClass().getName());
//    	Assume.assumeTrue(System.getProperty("org.opennms.snmp.strategyClass", "blah").equals("org.opennms.netmgt.snmp.mock.MockSnmpStrategy"));
    }

    @Test
    @JUnitSnmpAgent(resource="classpath:loadSnmpDataTest.properties", host="192.168.0.254")
    public void testClassAgent() throws Exception {
        assertEquals(new OctetString("TestData"), get(InetAddressUtils.addr("192.168.0.254"), m_oid));
    }
    
    @Test
    @JUnitMockSnmpStrategyAgents({
    		@JUnitSnmpAgent(host="192.168.0.1", port=161, resource="classpath:loadSnmpDataTest.properties"),
    		@JUnitSnmpAgent(host="192.168.0.2", port=161, resource="classpath:differentSnmpData.properties")
    })
    public void testMultipleHosts() throws Exception {
    	assertEquals(new OctetString("TestData"), get(InetAddressUtils.addr("192.168.0.1"), m_oid));
    	assertEquals(new OctetString("DifferentTestData"), get(InetAddressUtils.addr("192.168.0.2"), m_oid));
    }

    private Variable get(final InetAddress address, final OID oid) throws Exception, IOException, TimeoutException {

    	final TransportMapping transport = new DefaultUdpTransportMapping();
    	final Snmp session = new Snmp(transport);
        session.listen();

        final SnmpAgentAddress agentAddress = SnmpAgentConfigProxyMapper.getInstance().getAddress(address);
        final Target target = new CommunityTarget(new UdpAddress(agentAddress.getAddress(), agentAddress.getPort()), new OctetString("public"));
        target.setTimeout(1000);
        target.setRetries(3);
        target.setVersion(SnmpConstants.version2c);
        
        final PDU pdu = new PDU();
        pdu.setType(PDU.GET);
        pdu.addOID(new VariableBinding(oid));
        
        final ResponseEvent e = session.get(pdu, target);
        
        final PDU response = e.getResponse();
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
}
