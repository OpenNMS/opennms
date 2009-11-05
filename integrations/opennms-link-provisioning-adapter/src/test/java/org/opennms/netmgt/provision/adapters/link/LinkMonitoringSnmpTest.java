/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.adapters.link;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.mock.snmp.MockSnmpAgentAware;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.DefaultEndPointConfigurationDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpValue;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/snmpConfigFactoryContext.xml"
})
@JUnitSnmpAgent(resource="classpath:/airPairR3_walk.properties")
@TestExecutionListeners({
    JUnitSnmpAgentExecutionListener.class
})
public class LinkMonitoringSnmpTest implements MockSnmpAgentAware {
    
    private static final String AIR_PAIR_R3_SYS_OID = ".1.3.6.1.4.1.7262.1";
    private static final String AIR_PAIR_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.1.19.3.1.0";
    private static final String AIR_PAIR_R3_DUPLEX_MISMATCH = ".1.3.6.1.4.1.7262.1.19.2.3.0";
    private static final String AIR_PAIR_R4_SYS_OID = ".1.3.6.1.4.1.7262.1";
    private static final String AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.1.19.3.1.0";
    private static final String HORIZON_COMPACT_SYS_OID = ".1.3.6.1.4.1.7262.2.2";
    private static final String HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.2.2.8.4.4.1.0";
    private static final String HORIZON_COMPACT_ETHERNET_LINK_DOWN = ".1.3.6.1.4.1.7262.2.2.8.3.1.9.0";
    private static final String HORIZON_DUO_SYS_OID = ".1.3.6.1.4.1.7262.2.3";
    private static final String HORIZON_DUO_SYSTEM_CAPACITY = ".1.3.6.1.4.1.7262.2.3.1.1.5.0";
    private static final String HORIZON_DUO_MODEM_LOSS_OF_SIGNAL = ".1.3.6.1.4.1.7262.2.3.7.4.1.1.1.2.1";
    
    private MockSnmpAgent m_snmpAgent;
    private SnmpAgentConfig m_agentConfig;
    private DefaultEndPointConfigurationDao m_configDao;
    
    @Before
    public void setup() throws InterruptedException, UnknownHostException {
        
        if(m_agentConfig == null) {
            m_agentConfig = new SnmpAgentConfig();
            m_agentConfig.setAddress(InetAddress.getLocalHost());
            m_agentConfig.setPort(9161);
            m_agentConfig.setReadCommunity("public");
        }
        
        
        DefaultEndPointConfigurationDao dao = new DefaultEndPointConfigurationDao();
        dao.setConfigResource(new ClassPathResource("/testDWO-configuration.xml"));
        dao.afterPropertiesSet();
        m_configDao = dao;
        
        
    }
    
    @After
    public void tearDown() throws InterruptedException{
        
    }
    
    @Test
    public void dwoTestEndPointImplGetOid() throws UnknownHostException {
        EndPointImpl endPoint = getEndPoint(null);
        SnmpValue snmpVal = endPoint.get(AIR_PAIR_MODEM_LOSS_OF_SIGNAL);
        assertNotNull(snmpVal);
        assertEquals("1", snmpVal.toString());
    }

    private EndPointImpl getEndPoint(String sysOid) throws UnknownHostException {
        EndPointImpl endPoint = new EndPointImpl(InetAddress.getLocalHost(), m_agentConfig);
        endPoint.setSysOid(sysOid);
        return endPoint;
    }
    
    @Test
    public void dwoTestLinkMonitorAirPairR3() throws UnknownHostException {
        assertNotNull(m_snmpAgent);
        
        m_snmpAgent.updateCounter32Value(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(AIR_PAIR_R3_DUPLEX_MISMATCH, 1);
        
        EndPointImpl endPoint = getEndPoint(AIR_PAIR_R3_SYS_OID);
        
        EndPointTypeValidator validator = m_configDao.getValidator();
        try {
            validator.validate(endPoint);
        }catch (EndPointStatusException e) {
            assertTrue(false);
        }
    }
    
    @Test(expected=EndPointStatusException.class)
    public void dwoTestLinkMonitorAirPair3DownLossOfSignal() throws UnknownHostException, EndPointStatusException {
        m_snmpAgent.updateCounter32Value(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, 2);
        m_snmpAgent.updateCounter32Value(AIR_PAIR_R3_DUPLEX_MISMATCH, 1);
        
        EndPointImpl endPoint = getEndPoint(AIR_PAIR_R3_SYS_OID);
        
        m_configDao.getValidator().validate(endPoint);
        
    }
    
    @Test
    @Ignore
    public void dwoTestLinkMonitorAirPairR4() throws UnknownHostException {
        ClassPathResource resource = new ClassPathResource("/airPairR4_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(AIR_PAIR_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL, 1);
        
        EndPointImpl endPoint = getEndPoint(AIR_PAIR_R4_SYS_OID);
        
        try {
           m_configDao.getValidator().validate(endPoint); 
        }catch (EndPointStatusException e) {
            assertTrue("An EndPointStatusException was caught resulting in a failed test",false);
        }
        
    }
    
    @Test
    public void dwoTestLinkMonitorHorizonCompact() throws UnknownHostException {
        ClassPathResource resource = new ClassPathResource("/horizon_compact_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(HORIZON_COMPACT_ETHERNET_LINK_DOWN, 1);
        
        EndPointImpl endPoint = getEndPoint(HORIZON_COMPACT_SYS_OID);
        
        try {
            m_configDao.getValidator().validate(endPoint);
        } catch (Exception e) {
            assertTrue("An EndPointStatusException was thrown which shouldn't have and thats why the test failed", false);
        }
        
    }
    
    @Test(expected=EndPointStatusException.class)
    public void dwoTestLinkMonitorHorizonCompactDownLossOfSignal() throws EndPointStatusException, UnknownHostException {
        ClassPathResource resource = new ClassPathResource("/horizon_compact_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL, 2);
        m_snmpAgent.updateCounter32Value(HORIZON_COMPACT_ETHERNET_LINK_DOWN, 1);
        
        EndPoint endPoint = getEndPoint(HORIZON_COMPACT_SYS_OID);
        
        m_configDao.getValidator().validate(endPoint);
    }
    
    @Test(expected=EndPointStatusException.class)
    public void dwoTestLinkMonitorHorizonCompactDownEthernetLinkDown() throws EndPointStatusException, UnknownHostException {
        ClassPathResource resource = new ClassPathResource("/horizon_compact_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(HORIZON_COMPACT_ETHERNET_LINK_DOWN, 2);
        
        EndPoint endPoint = getEndPoint(HORIZON_COMPACT_SYS_OID);
        
        m_configDao.getValidator().validate(endPoint);
    }
    
    @Test
    public void dwoTestLinkMonitorHorizonDuoCapacity1() throws UnknownHostException, EndPointStatusException {
        ClassPathResource resource = new ClassPathResource("/horizon_duo_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_SYSTEM_CAPACITY, 1);
        
        EndPointImpl endPoint = getEndPoint(HORIZON_DUO_SYS_OID);
        
        m_configDao.getValidator().validate(endPoint);
        
    }
    
    @Test(expected=EndPointStatusException.class)
    public void dwoTestLinkMonitorHorizonDuoCapacity1DownModemLossSignal() throws UnknownHostException, EndPointStatusException {
        ClassPathResource resource = new ClassPathResource("/horizon_duo_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 2);
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_SYSTEM_CAPACITY, 1);
        
        EndPointImpl endPoint = getEndPoint(HORIZON_DUO_SYS_OID);
        
        m_configDao.getValidator().validate(endPoint);
        
    }
    
    @Test
    public void dwoTestLinkMonitorHorizonDuoCapacity2() throws UnknownHostException, EndPointStatusException {
        ClassPathResource resource = new ClassPathResource("/horizon_duo_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_SYSTEM_CAPACITY, 2);
        
        EndPoint endPoint = getEndPoint(HORIZON_DUO_SYS_OID);
        
        m_configDao.getValidator().validate(endPoint);
        
    }
    
    @Test(expected=EndPointStatusException.class)
    public void dwoTestLinkMonitorHorizonDuoCapacity2DownModemLossSignal() throws UnknownHostException, EndPointStatusException {
        ClassPathResource resource = new ClassPathResource("/horizon_duo_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 2);
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_SYSTEM_CAPACITY, 2);
        
        EndPoint endPoint = getEndPoint(HORIZON_DUO_SYS_OID);
        
        m_configDao.getValidator().validate(endPoint);
        
    }
    
    @Test
    public void dwoTestLinkMonitorHorizonDuoCapacity3() throws UnknownHostException, EndPointStatusException {
        ClassPathResource resource = new ClassPathResource("/horizon_duo_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 1);
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_SYSTEM_CAPACITY, 3);
        
        EndPoint endPoint = getEndPoint(HORIZON_DUO_SYS_OID);
        
        m_configDao.getValidator().validate(endPoint);
    }
    
    @Test(expected=EndPointStatusException.class)
    public void dwoTestLinkMonitorHorizonDuoCapacity3DownModemLossSignal() throws UnknownHostException, EndPointStatusException {
        ClassPathResource resource = new ClassPathResource("/horizon_duo_walk.properties");
        m_snmpAgent.updateValuesFromResource(resource);
        
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL, 2);
        m_snmpAgent.updateCounter32Value(HORIZON_DUO_SYSTEM_CAPACITY, 3);
        
        EndPoint endPoint = getEndPoint(HORIZON_DUO_SYS_OID);
        
        m_configDao.getValidator().validate(endPoint);
    }
   
    
    public void setMockSnmpAgent(MockSnmpAgent agent) {
        m_snmpAgent = agent;
    }


}
