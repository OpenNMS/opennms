/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.capsd.Capsd;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
//import org.opennms.netmgt.linkd.Linkd;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:antonio@opennme.it">Antonio Russo</a>
 * @author <a href="mailto:alejandro@opennms.org">Alejandro Galue</a>
 */

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-capsd.xml",
        // import simple defined events
        "classpath:/META-INF/opennms/smallEventConfDao.xml",
        // Override the capsd config with a stripped-down version
        "classpath:/META-INF/opennms/capsdTest.xml",
        // override snmp-config configuration
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
// TODO: this class should be the starting point for Integration tests
// either with linkd and capsd
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class LinkdCapsdIntegrationTest extends LinkdNms7467NetworkBuilder implements InitializingBean {


    @Autowired
    private IpInterfaceDao m_interfaceDao;

    @Autowired
    private Capsd m_capsd;

    //FIXME now linkd is commented out but there should be to found the right
    // context in which it is properly instantiated 
//    @Autowired
//    private Linkd m_linkd;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        assertTrue("Capsd must not be null", m_capsd != null);
//        assertTrue("Linkd must not be null", m_linkd != null);
        
    }


    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource="classpath:linkd/"+CISCO_WS_C2948_IP+"-walk.txt")
    })
    @Transactional
    public final void testCiscoWSC2948CapsdCollection() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(CISCO_WS_C2948_IP);
        

        List<OnmsIpInterface> ips = m_interfaceDao.findByIpAddress(CISCO_WS_C2948_IP);
        assertTrue("Has only one ip interface", ips.size() == 1);

        OnmsIpInterface ip = ips.get(0);

        assertTrue("The ifindex" + ip.getIfIndex() +" is not equal to 3", ip.getIfIndex() == 3);
        assertTrue("The snmp interface is null",ip.getSnmpInterface() != null);
        assertTrue("The mac address is null",ip.getSnmpInterface().getPhysAddr() != null);
        assertTrue("The mac address: " + ip.getSnmpInterface().getPhysAddr() +",  is not corresponding to 0002baaacffe"
                   ,ip.getSnmpInterface().getPhysAddr().equals("0002baaacffe"));

        for (OnmsSnmpInterface snmpinterface: ip.getNode().getSnmpInterfaces()) {
            assertTrue("The mac address is null", snmpinterface.getPhysAddr() != null);
            assertTrue("The mac must be valid", snmpinterface.getPhysAddr().length() == 12);
            assertTrue("The mac for ifindex" + snmpinterface.getIfIndex() + " must correspond: " + snmpinterface.getPhysAddr(), 
                       snmpinterface.getPhysAddr().equals(CISCO_WS_C2948_IF_MAC_MAP.get(snmpinterface.getIfIndex())));
        }
        
 //       assertTrue(!m_linkd.scheduleNodeCollection(ip.getNode().getId()));
//        assertTrue(m_linkd.runSingleSnmpCollection(ip.getNode().getId()));

        m_capsd.stop();

        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=NETGEAR_SW_108_IP, port=161, resource="classpath:linkd/"+NETGEAR_SW_108_IP+"-walk.txt")
    })
    @Transactional
    public final void testNETGEARSW108CapsdCollection() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(NETGEAR_SW_108_IP);
        
        List<OnmsIpInterface> ips = m_interfaceDao.findByIpAddress(NETGEAR_SW_108_IP);
        assertTrue("Has only one ip interface", ips.size() == 1);

        OnmsIpInterface ip = ips.get(0);

        assertTrue("The ifindex " + ip.getIfIndex() +" is not equal to 1", ip.getIfIndex() == 1);
        assertTrue("The snmp interface is null",ip.getSnmpInterface() != null);
        assertTrue("The mac address is null",ip.getSnmpInterface().getPhysAddr() != null);
        assertTrue("The mac address: " + ip.getSnmpInterface().getPhysAddr() +",  is not corresponding to 00223ff00b7c"
                   ,ip.getSnmpInterface().getPhysAddr().equals("00223ff00b7c"));

        for (OnmsSnmpInterface snmpinterface: ip.getNode().getSnmpInterfaces()) {
            assertTrue("The mac address is null", snmpinterface.getPhysAddr() != null);
            assertTrue("The mac must be valid", snmpinterface.getPhysAddr().length() == 12);
            assertTrue("The mac for ifindex must correspond", 
                       snmpinterface.getPhysAddr().equals(NETGEAR_SW_108_IF_MAC_MAP.get(snmpinterface.getIfIndex())));
        }
        
//        assertTrue(!m_linkd.scheduleNodeCollection(ip.getNode().getId()));
//        assertTrue(m_linkd.runSingleSnmpCollection(ip.getNode().getId()));

        m_capsd.stop();

        
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_C870_IP_PRIMARY, port=161, resource="classpath:linkd/"+CISCO_C870_IP+"-walk.txt")
    })
    @Transactional
    public final void testCISCO870CapsdCollection() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(CISCO_C870_IP_PRIMARY);
        
        OnmsIpInterface ipmain = m_interfaceDao.findByIpAddress(CISCO_C870_IP_PRIMARY).get(0);
        assertTrue("should have a master not null ip interface ",ipmain != null);
        
        Set<OnmsIpInterface> ifs = ipmain.getNode().getIpInterfaces();
        
        assertTrue("Should have 4 ip interface. Found: " + ifs.size(), ifs.size() == 4);
        
        for (OnmsIpInterface ipinterface: ifs) {
            assertTrue("The ifindex should not be null for ipaddress: " +ipinterface.getIpHostName(), ipinterface.getIfIndex() != null);
            assertTrue("The ifindex is not corresponding: found: " + ipinterface.getIfIndex() + " should be: "
                       + CISCO_C870_IP_IF_MAP.get(ipinterface.getIpAddress())
                       , ipinterface.getIfIndex().intValue() == CISCO_C870_IP_IF_MAP.get(ipinterface.getIpAddress()).intValue());
            assertTrue("The snmp interface is null",ipinterface.getSnmpInterface() != null);
            assertTrue("The mac address is null",ipinterface.getSnmpInterface().getPhysAddr() != null);
            assertTrue("The mac address is not corresponding"
                   ,ipinterface.getSnmpInterface().getPhysAddr().equals(CISCO_C870_IF_MAC_MAP.get(ipinterface.getIfIndex())));
        }

        assertTrue("Has 16 snmp interface", ipmain.getNode().getSnmpInterfaces().size() == 16);
        
//        assertTrue(!m_linkd.scheduleNodeCollection(ipmain.getNode().getId()));
//        assertTrue(m_linkd.runSingleSnmpCollection(ipmain.getNode().getId()));

        m_capsd.stop();

        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=LINUX_UBUNTU_IP, port=161, resource="classpath:linkd/"+LINUX_UBUNTU_IP+"-walk.txt")
    })
    @Transactional
    public final void testLINUXUBUNTUCapsdCollection() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(LINUX_UBUNTU_IP);
        

        OnmsIpInterface ipmain = m_interfaceDao.findByIpAddress(LINUX_UBUNTU_IP).get(0);
        assertTrue("Should have 1 ip master interface",ipmain != null);
        
        Set<OnmsIpInterface> ifs = ipmain.getNode().getIpInterfaces();
        
        assertTrue("should have 2 ipinterface", ifs.size() == 2);
        for (OnmsIpInterface ip: ifs) {
            assertTrue("The if index should not be null", ip.getIfIndex() != null);
            assertTrue("The ifindex is not corresponding: ", ip.getIfIndex().intValue() == LINUX_UBUNTU_IP_IF_MAP.get(ip.getIpAddress()).intValue());
            assertTrue("The snmp interface is null",ip.getSnmpInterface() != null);
            assertTrue("The mac address is null",ip.getSnmpInterface().getPhysAddr() != null);
            assertTrue("The mac address is not corresponding"
                   ,ip.getSnmpInterface().getPhysAddr().equals(LINUX_UBUNTU_IF_MAC_MAP.get(ip.getIfIndex())));
        }

        Set<OnmsSnmpInterface> snmpifs = ipmain.getNode().getSnmpInterfaces();
        assertTrue("Has 6 snmp interface. Found: " + snmpifs.size(), snmpifs.size() == 6);
        for (OnmsSnmpInterface snmpinterface: snmpifs) {
            if (snmpinterface.getIfIndex() == 1)
            assertTrue("The mac address is not null for ifindex 1", snmpinterface.getPhysAddr() == null);
            else {
            assertTrue("The mac must be valid", snmpinterface.getPhysAddr().length() == 12);
            assertTrue("The mac for ifindex must correspond", 
                       snmpinterface.getPhysAddr().equals(LINUX_UBUNTU_IF_MAC_MAP.get(snmpinterface.getIfIndex())));
            }
        }
//        assertTrue(!m_linkd.scheduleNodeCollection(ipmain.getNode().getId()));
//        assertTrue(m_linkd.runSingleSnmpCollection(ipmain.getNode().getId()));
        m_capsd.stop();
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DARWIN_10_8_IP, port=161, resource="classpath:linkd/"+DARWIN_10_8_IP+"-walk.txt")
    })
    @Transactional
    public final void testDARWIN108CapsdCollection() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(DARWIN_10_8_IP);
        

        List<OnmsIpInterface> ips = m_interfaceDao.findByIpAddress(DARWIN_10_8_IP);
        assertTrue("Has one ip primary interface", ips.size() == 1);

        OnmsIpInterface ip = ips.get(0);

        assertTrue("ipinterface is not null", ip != null);
        assertTrue("has ifindex", ip.getIfIndex() != null);
        assertTrue("The ifindex" + ip.getIfIndex() +" is not equal to 4", ip.getIfIndex() == 4);
        assertTrue("The snmp interface is null",ip.getSnmpInterface() != null);
        assertTrue("The mac address is null",ip.getSnmpInterface().getPhysAddr() != null);
        assertTrue("The mac address: " + ip.getSnmpInterface().getPhysAddr() +",  is not corresponding to 0026b0ed8fb8"
                   ,ip.getSnmpInterface().getPhysAddr().equals("0026b0ed8fb8"));

        for (OnmsSnmpInterface snmpinterface: ip.getNode().getSnmpInterfaces()) {
            if (snmpinterface.getIfIndex() <= 3)
                assertTrue("The mac address is not null for ifindex "+ snmpinterface.getIfIndex(), snmpinterface.getPhysAddr() == null);
            else {
            assertTrue("The mac address is null", snmpinterface.getPhysAddr() != null);
            assertTrue("The mac must be valid", snmpinterface.getPhysAddr().length() == 12);
            assertTrue("The mac for ifindex must correspond: " + snmpinterface.getPhysAddr(), 
                       snmpinterface.getPhysAddr().equals(DARWIN_10_8_IF_MAC_MAP.get(snmpinterface.getIfIndex())));
            }
        }
        m_capsd.stop();        
    }
}
