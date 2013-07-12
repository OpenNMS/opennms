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

package org.opennms.netmgt.linkd.capsd;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Properties;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.capsd.Capsd;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.linkd.nb.Nms17216NetworkBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
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
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        // Override the capsd config with a stripped-down version
        "classpath:/META-INF/opennms/capsdTest.xml",
        // override snmp-config configuration
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
@JUnitTemporaryDatabase
public class Nms17216CapsdNetworkBuilderTest extends Nms17216NetworkBuilder implements InitializingBean {

    
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

        super.setIpInterfaceDao(m_interfaceDao);
        MockLogAppender.setupLogging(p);
        assertTrue("Capsd must not be null", m_capsd != null);        
    }


    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ROUTER1_IP, port=161, resource="classpath:linkd/nms17216/router1-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER2_IP, port=161, resource="classpath:linkd/nms17216/router2-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER3_IP, port=161, resource="classpath:linkd/nms17216/router3-walk.txt"),
            @JUnitSnmpAgent(host=ROUTER4_IP, port=161, resource="classpath:linkd/nms17216/router4-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource="classpath:linkd/nms17216/switch1-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource="classpath:linkd/nms17216/switch2-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource="classpath:linkd/nms17216/switch3-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource="classpath:linkd/nms17216/switch4-walk.txt"),
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource="classpath:linkd/nms17216/switch5-walk.txt")
    })
    @Transactional
    public final void testCapsdNms17216() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(ROUTER1_IP);
        m_capsd.scanSuspectInterface(ROUTER2_IP);
        m_capsd.scanSuspectInterface(ROUTER3_IP);
        m_capsd.scanSuspectInterface(ROUTER4_IP);
        m_capsd.scanSuspectInterface(SWITCH1_IP);
        m_capsd.scanSuspectInterface(SWITCH2_IP);
        m_capsd.scanSuspectInterface(SWITCH3_IP);
        m_capsd.scanSuspectInterface(SWITCH4_IP);
        m_capsd.scanSuspectInterface(SWITCH5_IP);

        printNode(ROUTER1_IP,"ROUTER1");
        printNode(ROUTER2_IP,"ROUTER2");
        printNode(ROUTER3_IP,"ROUTER3");
        printNode(ROUTER4_IP,"ROUTER4");
        printNode(SWITCH1_IP,"SWITCH1");
        printNode(SWITCH2_IP,"SWITCH2");
        printNode(SWITCH3_IP,"SWITCH3");
        printNode(SWITCH4_IP,"SWITCH4");
        printNode(SWITCH5_IP,"SWITCH5");
        
        m_capsd.stop();
    }
}
