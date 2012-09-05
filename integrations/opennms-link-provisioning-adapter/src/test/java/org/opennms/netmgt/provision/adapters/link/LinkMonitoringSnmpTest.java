/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.adapters.link;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.provision.adapters.link.endpoint.EndPointTypeValidator;
import org.opennms.netmgt.provision.adapters.link.endpoint.dao.DefaultEndPointConfigurationDao;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/snmpConfigFactoryContext.xml"
})
public class LinkMonitoringSnmpTest implements InitializingBean {
    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

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
    
    private DefaultEndPointConfigurationDao m_configDao;
    
    private EndPointImpl getEndPoint(final String sysOid, final String address) throws UnknownHostException {
    	final EndPointImpl endPoint = new EndPointImpl(InetAddressUtils.getLocalHostAddress(), getAgentConfig(address));
        endPoint.setSysOid(sysOid);
        return endPoint;
    }

	private SnmpAgentConfig getAgentConfig(final String address) {
		return m_snmpPeerFactory.getAgentConfig(addr(address));
	}
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Before
    public void setUp() throws InterruptedException, UnknownHostException {
    	SnmpPeerFactory.setInstance(m_snmpPeerFactory);
        final DefaultEndPointConfigurationDao dao = new DefaultEndPointConfigurationDao();
        dao.setConfigResource(new ClassPathResource("/testDWO-configuration.xml"));
        dao.afterPropertiesSet();
        m_configDao = dao;
    }
    
    @Test
    @JUnitSnmpAgent(host="192.168.255.10", resource="classpath:/airPairR3_walk.properties")
    public void dwoTestEndPointImplGetOid() throws UnknownHostException {
        EndPointImpl endPoint = getEndPoint(null, "192.168.255.10");
        SnmpValue snmpVal = endPoint.get(AIR_PAIR_MODEM_LOSS_OF_SIGNAL);
        assertNotNull(snmpVal);
        assertEquals("1", snmpVal.toString());
    }

    @Test
    @JUnitSnmpAgent(host="192.168.255.10", resource="classpath:/airPairR3_walk.properties")
    public void dwoTestLinkMonitorAirPairR3() throws UnknownHostException {
    	SnmpUtils.set(getAgentConfig("192.168.255.10"), SnmpObjId.get(AIR_PAIR_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(1));
    	SnmpUtils.set(getAgentConfig("192.168.255.10"), SnmpObjId.get(AIR_PAIR_R3_DUPLEX_MISMATCH), SnmpUtils.getValueFactory().getCounter32(1));
        
        EndPointImpl endPoint = getEndPoint(AIR_PAIR_R3_SYS_OID, "192.168.255.10");
        
        EndPointTypeValidator validator = m_configDao.getValidator();
        try {
            validator.validate(endPoint);
        }catch (EndPointStatusException e) {
            assertTrue(false);
        }
    }
    
    @Test(expected=EndPointStatusException.class)
    @JUnitSnmpAgent(host="192.168.255.10", resource="classpath:/airPairR3_walk.properties")
    public void dwoTestLinkMonitorAirPair3DownLossOfSignal() throws UnknownHostException, EndPointStatusException {
    	SnmpUtils.set(getAgentConfig("192.168.255.10"), SnmpObjId.get(AIR_PAIR_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(2));
    	SnmpUtils.set(getAgentConfig("192.168.255.10"), SnmpObjId.get(AIR_PAIR_R3_DUPLEX_MISMATCH), SnmpUtils.getValueFactory().getCounter32(1));
        
        EndPointImpl endPoint = getEndPoint(AIR_PAIR_R3_SYS_OID, "192.168.255.10");
        
        m_configDao.getValidator().validate(endPoint);
        
    }
    
    @Test
    @Ignore
    @JUnitSnmpAgent(host="192.168.255.20", resource="/airPairR4_walk.properties")
    public void dwoTestLinkMonitorAirPairR4() throws UnknownHostException {
    	SnmpUtils.set(getAgentConfig("192.168.255.20"), SnmpObjId.get(AIR_PAIR_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(1));
    	SnmpUtils.set(getAgentConfig("192.168.255.20"), SnmpObjId.get(AIR_PAIR_R4_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(1));
        
        EndPointImpl endPoint = getEndPoint(AIR_PAIR_R4_SYS_OID, "192.168.255.20");
        
        try {
           m_configDao.getValidator().validate(endPoint); 
        }catch (EndPointStatusException e) {
            assertTrue("An EndPointStatusException was caught resulting in a failed test",false);
        }
        
    }
    
    @Test
    @JUnitSnmpAgent(host="192.168.255.31", resource="/horizon_compact_walk.properties")
    public void dwoTestLinkMonitorHorizonCompact() throws UnknownHostException {
    	SnmpUtils.set(getAgentConfig("192.168.255.31"), SnmpObjId.get(HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(1));
    	SnmpUtils.set(getAgentConfig("192.168.255.31"), SnmpObjId.get(HORIZON_COMPACT_ETHERNET_LINK_DOWN), SnmpUtils.getValueFactory().getCounter32(1));
        
        EndPointImpl endPoint = getEndPoint(HORIZON_COMPACT_SYS_OID, "192.168.255.31");
        
        try {
            m_configDao.getValidator().validate(endPoint);
        } catch (Throwable e) {
            assertTrue("An EndPointStatusException was thrown which shouldn't have and thats why the test failed", false);
        }
        
    }
    
    @Test(expected=EndPointStatusException.class)
    @JUnitSnmpAgent(host="192.168.255.31", resource="/horizon_compact_walk.properties")
    public void dwoTestLinkMonitorHorizonCompactDownLossOfSignal() throws EndPointStatusException, UnknownHostException {
    	SnmpUtils.set(getAgentConfig("192.168.255.31"), SnmpObjId.get(HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(2));
    	SnmpUtils.set(getAgentConfig("192.168.255.31"), SnmpObjId.get(HORIZON_COMPACT_ETHERNET_LINK_DOWN), SnmpUtils.getValueFactory().getCounter32(1));
        
        EndPoint endPoint = getEndPoint(HORIZON_COMPACT_SYS_OID, "192.168.255.31");
        
        m_configDao.getValidator().validate(endPoint);
    }
    
    @Test(expected=EndPointStatusException.class)
    @JUnitSnmpAgent(host="192.168.255.31", resource="/horizon_compact_walk.properties")
    public void dwoTestLinkMonitorHorizonCompactDownEthernetLinkDown() throws EndPointStatusException, UnknownHostException {
    	SnmpUtils.set(getAgentConfig("192.168.255.31"), SnmpObjId.get(HORIZON_COMPACT_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(1));
    	SnmpUtils.set(getAgentConfig("192.168.255.31"), SnmpObjId.get(HORIZON_COMPACT_ETHERNET_LINK_DOWN), SnmpUtils.getValueFactory().getCounter32(2));
        
        EndPoint endPoint = getEndPoint(HORIZON_COMPACT_SYS_OID, "192.168.255.31");
        
        m_configDao.getValidator().validate(endPoint);
    }
    
    @Test
    @JUnitSnmpAgent(host="192.168.254.10", resource="/horizon_duo_walk.properties")
    public void dwoTestLinkMonitorHorizonDuoCapacity1() throws UnknownHostException, EndPointStatusException {
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(1));
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_SYSTEM_CAPACITY), SnmpUtils.getValueFactory().getCounter32(1));
        
        EndPointImpl endPoint = getEndPoint(HORIZON_DUO_SYS_OID, "192.168.254.10");
        
        m_configDao.getValidator().validate(endPoint);
        
    }
    
    @Test(expected=EndPointStatusException.class)
    @JUnitSnmpAgent(host="192.168.254.10", resource="/horizon_duo_walk.properties")
    public void dwoTestLinkMonitorHorizonDuoCapacity1DownModemLossSignal() throws UnknownHostException, EndPointStatusException {
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(2));
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_SYSTEM_CAPACITY), SnmpUtils.getValueFactory().getCounter32(1));
        
        EndPointImpl endPoint = getEndPoint(HORIZON_DUO_SYS_OID, "192.168.254.10");
        
        m_configDao.getValidator().validate(endPoint);
        
    }
    
    @Test
    @JUnitSnmpAgent(host="192.168.254.10", resource="/horizon_duo_walk.properties")
    public void dwoTestLinkMonitorHorizonDuoCapacity2() throws UnknownHostException, EndPointStatusException {
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(1));
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_SYSTEM_CAPACITY), SnmpUtils.getValueFactory().getCounter32(2));
        
        EndPoint endPoint = getEndPoint(HORIZON_DUO_SYS_OID, "192.168.254.10");
        
        m_configDao.getValidator().validate(endPoint);
        
    }
    
    @Test(expected=EndPointStatusException.class)
    @JUnitSnmpAgent(host="192.168.254.10", resource="/horizon_duo_walk.properties")
    public void dwoTestLinkMonitorHorizonDuoCapacity2DownModemLossSignal() throws UnknownHostException, EndPointStatusException {
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(2));
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_SYSTEM_CAPACITY), SnmpUtils.getValueFactory().getCounter32(2));
        
        EndPoint endPoint = getEndPoint(HORIZON_DUO_SYS_OID, "192.168.254.10");
        
        m_configDao.getValidator().validate(endPoint);
        
    }
    
    @Test
    @JUnitSnmpAgent(host="192.168.254.10", resource="/horizon_duo_walk.properties")
    public void dwoTestLinkMonitorHorizonDuoCapacity3() throws UnknownHostException, EndPointStatusException {
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(1));
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_SYSTEM_CAPACITY), SnmpUtils.getValueFactory().getCounter32(3));
        
        EndPoint endPoint = getEndPoint(HORIZON_DUO_SYS_OID, "192.168.254.10");
        
        m_configDao.getValidator().validate(endPoint);
    }
    
    @Test(expected=EndPointStatusException.class)
    @JUnitSnmpAgent(host="192.168.254.10", resource="/horizon_duo_walk.properties")
    public void dwoTestLinkMonitorHorizonDuoCapacity3DownModemLossSignal() throws UnknownHostException, EndPointStatusException {
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_MODEM_LOSS_OF_SIGNAL), SnmpUtils.getValueFactory().getCounter32(2));
    	SnmpUtils.set(getAgentConfig("192.168.254.10"), SnmpObjId.get(HORIZON_DUO_SYSTEM_CAPACITY), SnmpUtils.getValueFactory().getCounter32(3));
        
        EndPoint endPoint = getEndPoint(HORIZON_DUO_SYS_OID, "192.168.254.10");
        
        m_configDao.getValidator().validate(endPoint);
    }
}
