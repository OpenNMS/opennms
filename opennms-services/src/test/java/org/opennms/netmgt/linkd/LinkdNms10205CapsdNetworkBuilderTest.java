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
import java.util.Properties;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
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
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
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
public class LinkdNms10205CapsdNetworkBuilderTest implements InitializingBean {


    private static final String MUMBAI_IP = "10.205.56.5";
    private static final String CHENNAI_IP = "10.205.56.6";
    private static final String DELHI_IP =  "10.205.56.7";
    private static final String SPACE_EX_SW1_IP = "10.205.56.1";
    private static final String BANGALORE_IP = "10.205.56.9";
    
    @Autowired
    private IpInterfaceDao m_interfaceDao;

    @Autowired
    private Capsd m_capsd;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        Properties p = new Properties();
        p.setProperty("log4j.logger.org.hibernate.SQL", "WARN");

        MockLogAppender.setupLogging(p);
        assertTrue("Capsd must not be null", m_capsd != null);
//        assertTrue("Linkd must not be null", m_linkd != null);
        
    }


    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource="classpath:linkd/nms10205/"+MUMBAI_IP+"-walk.txt")
    })
    @Transactional
    public final void testMumbay() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(MUMBAI_IP);
        

        List<OnmsIpInterface> ips = m_interfaceDao.findByIpAddress(MUMBAI_IP);
        assertTrue("Has only one ip interface", ips.size() == 1);

        OnmsIpInterface ip = ips.get(0);

        for (OnmsIpInterface ipinterface: ip.getNode().getIpInterfaces()) {
            if (ipinterface.getIfIndex() != null )
                System.out.println("MUMBAI_IP_IF_MAP.put(InetAddress.getByName(\""+ipinterface.getIpHostName()+"\"), "+ipinterface.getIfIndex()+");");
        }

        for (OnmsSnmpInterface snmpinterface: ip.getNode().getSnmpInterfaces()) {
            if ( snmpinterface.getIfName() != null)
            System.out.println("MUMBAI_IF_IFNAME_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfName()+"\");");
            if (snmpinterface.getIfDescr() != null)
            System.out.println("MUMBAI_IF_IFDESCR_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfDescr()+"\");");
            if (snmpinterface.getPhysAddr() != null)
            System.out.println("MUMBAI_IF_MAC_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getPhysAddr()+"\");");            
        }
        
        m_capsd.stop();

        
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CHENNAI_IP, port=161, resource="classpath:linkd/nms10205/"+CHENNAI_IP+"-walk.txt")
    })
    @Transactional
    public final void testChennai() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(CHENNAI_IP);
        

        List<OnmsIpInterface> ips = m_interfaceDao.findByIpAddress(CHENNAI_IP);
        assertTrue("Has only one ip interface", ips.size() == 1);

        OnmsIpInterface ip = ips.get(0);

        for (OnmsIpInterface ipinterface: ip.getNode().getIpInterfaces()) {
            if (ipinterface.getIfIndex() != null )
                System.out.println("CHENNAI_IP_IF_MAP.put(InetAddress.getByName(\""+ipinterface.getIpHostName()+"\"), "+ipinterface.getIfIndex()+");");
        }

        for (OnmsSnmpInterface snmpinterface: ip.getNode().getSnmpInterfaces()) {
            if ( snmpinterface.getIfName() != null)
            System.out.println("CHENNAI_IF_IFNAME_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfName()+"\");");
            if (snmpinterface.getIfDescr() != null)
            System.out.println("CHENNAI_IF_IFDESCR_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfDescr()+"\");");
            if (snmpinterface.getPhysAddr() != null)
            System.out.println("CHENNAI_IF_MAC_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getPhysAddr()+"\");");            
        }
        
        m_capsd.stop();
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource="classpath:linkd/nms10205/"+DELHI_IP+"-walk.txt")
    })
    @Transactional
    public final void testDelhi() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(DELHI_IP);
        

        List<OnmsIpInterface> ips = m_interfaceDao.findByIpAddress(DELHI_IP);
        assertTrue("Has only one ip interface", ips.size() == 1);

        OnmsIpInterface ip = ips.get(0);

        for (OnmsIpInterface ipinterface: ip.getNode().getIpInterfaces()) {
            if (ipinterface.getIfIndex() != null )
                System.out.println("DELHI_IP_IF_MAP.put(InetAddress.getByName(\""+ipinterface.getIpHostName()+"\"), "+ipinterface.getIfIndex()+");");
        }

        for (OnmsSnmpInterface snmpinterface: ip.getNode().getSnmpInterfaces()) {
            if ( snmpinterface.getIfName() != null)
            System.out.println("DELHI_IF_IFNAME_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfName()+"\");");
            if (snmpinterface.getIfDescr() != null)
            System.out.println("DELHI_IF_IFDESCR_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfDescr()+"\");");
            if (snmpinterface.getPhysAddr() != null)
            System.out.println("DELHI_IF_MAC_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getPhysAddr()+"\");");            
        }
        
        m_capsd.stop();
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource="classpath:linkd/nms10205/"+SPACE_EX_SW1_IP+"-walk.txt")
    })
    @Transactional
    public final void testSpaceExSw1() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(SPACE_EX_SW1_IP);
        

        List<OnmsIpInterface> ips = m_interfaceDao.findByIpAddress(SPACE_EX_SW1_IP);
        assertTrue("Has only one ip interface", ips.size() == 1);

        OnmsIpInterface ip = ips.get(0);

        for (OnmsIpInterface ipinterface: ip.getNode().getIpInterfaces()) {
            if (ipinterface.getIfIndex() != null )
                System.out.println("SPACE_EX_SW1_IP_IF_MAP.put(InetAddress.getByName(\""+ipinterface.getIpHostName()+"\"), "+ipinterface.getIfIndex()+");");
        }

        for (OnmsSnmpInterface snmpinterface: ip.getNode().getSnmpInterfaces()) {
            if ( snmpinterface.getIfName() != null)
            System.out.println("SPACE_EX_SW1_IF_IFNAME_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfName()+"\");");
            if (snmpinterface.getIfDescr() != null)
            System.out.println("SPACE_EX_SW1_IF_IFDESCR_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfDescr()+"\");");
            if (snmpinterface.getPhysAddr() != null)
            System.out.println("SPACE_EX_SW1_IF_MAC_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getPhysAddr()+"\");");            
        }
        
        m_capsd.stop();
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource="classpath:linkd/nms10205/"+BANGALORE_IP+"-walk.txt")
    })
    @Transactional
    public final void testBangalore() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(BANGALORE_IP);
        

        List<OnmsIpInterface> ips = m_interfaceDao.findByIpAddress(BANGALORE_IP);
        assertTrue("Has only one ip interface", ips.size() == 1);

        OnmsIpInterface ip = ips.get(0);

        for (OnmsIpInterface ipinterface: ip.getNode().getIpInterfaces()) {
            if (ipinterface.getIfIndex() != null )
                System.out.println("BANGALORE_IP_IF_MAP.put(InetAddress.getByName(\""+ipinterface.getIpHostName()+"\"), "+ipinterface.getIfIndex()+");");
        }

        for (OnmsSnmpInterface snmpinterface: ip.getNode().getSnmpInterfaces()) {
            if ( snmpinterface.getIfName() != null)
            System.out.println("BANGALORE_IF_IFNAME_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfName()+"\");");
            if (snmpinterface.getIfDescr() != null)
            System.out.println("BANGALORE_IF_IFDESCR_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfDescr()+"\");");
            if (snmpinterface.getPhysAddr() != null)
            System.out.println("BANGALORE_IF_MAC_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getPhysAddr()+"\");");            
        }
        
        m_capsd.stop();
    }

}
