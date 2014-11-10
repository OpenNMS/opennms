/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.nb;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.capsd.Capsd;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * This class is useful for generating the linkd network builder
 * classes. 
 * 
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
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        // Override the capsd config with a stripped-down version
        "classpath:/META-INF/opennms/capsdTest.xml",
        // override snmp-config configuration
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
@JUnitTemporaryDatabase
public class LinkdTestCapsdNetworkBuilder extends TestNetworkBuilder implements InitializingBean {

    @Autowired
    private Capsd m_capsd;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

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
    }


    @Test
    @Ignore
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = SSeMalmNobe_IP, port = 161, resource = SSeMalmNobe_SNMP_RESOURCE),
    })
    @Transactional
    public final void testCapsd() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(SSeMalmNobe_IP);

        printNode(SSeMalmNobe_IP,SSeMalmNobe_ROOT);
        
        m_capsd.stop();
    }

    @Test
    @Ignore
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = CISCO_C870_IP, port = 161, resource = CISCO_C870_SNMP_RESOURCE),
    })
    @Transactional
    public final void testCapsdA() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(CISCO_C870_IP);

        printNode(CISCO_C870_IP,CISCO_C870_ROOT);
        
        m_capsd.stop();
    }

    @Test
    @Ignore
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = FROH_SNMP_RESOURCE),
    })
    @Transactional
    public final void testCapsdB() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(FROH_IP);

        printNode(FROH_IP,FROH_ROOT);
        
        m_capsd.stop();
    }

    @Test
    @Ignore
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = OEDIPUS_SNMP_RESOURCE),
    })
    @Transactional
    public final void testCapsdC() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(OEDIPUS_IP);

        printNode(OEDIPUS_IP,OEDIPUS_ROOT);
        
        m_capsd.stop();
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = SIEGFRIE_SNMP_RESOURCE),
    })
    @Transactional
    public final void testCapsdD() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(SIEGFRIE_IP);

        printNode(SIEGFRIE_IP,SIEGFRIE_ROOT);
        
        m_capsd.stop();
    }

    protected final void printNode(String ipAddr, String prefix) {

        List<OnmsIpInterface> ips = m_ipInterfaceDao.findByIpAddress(ipAddr);
        assertTrue("Has only one ip interface", ips.size() == 1);

        OnmsIpInterface ip = ips.get(0);

        for (OnmsIpInterface ipinterface: ip.getNode().getIpInterfaces()) {
            if (ipinterface.getIfIndex() != null )
                printipInterface(prefix, ipinterface);
        }

        for (OnmsSnmpInterface snmpinterface: ip.getNode().getSnmpInterfaces()) {
            printSnmpInterface(prefix, snmpinterface);
        }
    }

    protected void printipInterface(String nodeStringId,OnmsIpInterface ipinterface) {
        System.out.println(nodeStringId+"_IP_IF_MAP.put(InetAddressUtils.addr(\""+ipinterface.getIpHostName()+"\"), "+ipinterface.getIfIndex()+");");
    }
    
    protected void printSnmpInterface(String nodeStringId,OnmsSnmpInterface snmpinterface) {
        if ( snmpinterface.getIfName() != null)
            System.out.println(nodeStringId+"_IF_IFNAME_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfName()+"\");");
        if (snmpinterface.getIfDescr() != null)
            System.out.println(nodeStringId+"_IF_IFDESCR_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfDescr()+"\");");
        if (snmpinterface.getPhysAddr() != null)
            System.out.println(nodeStringId+"_IF_MAC_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getPhysAddr()+"\");");            
        if (snmpinterface.getIfAlias() != null)
            System.out.println(nodeStringId+"_IF_IFALIAS_MAP.put("+snmpinterface.getIfIndex()+", \""+snmpinterface.getIfAlias()+"\");");            
        if (snmpinterface.getNetMask() != null && !snmpinterface.getNetMask().getHostAddress().equals("127.0.0.1"))
            System.out.println(nodeStringId+"_IF_NETMASK_MAP.put("+snmpinterface.getIfIndex()+", InetAddressUtils.addr(\""+snmpinterface.getNetMask().getHostAddress()+"\"));");
    }

}
