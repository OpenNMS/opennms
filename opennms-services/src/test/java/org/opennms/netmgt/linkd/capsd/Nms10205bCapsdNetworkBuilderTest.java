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
import org.opennms.netmgt.linkd.nb.Nms10205bNetworkBuilder;
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
public class Nms10205bCapsdNetworkBuilderTest extends Nms10205bNetworkBuilder implements InitializingBean {

    
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
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource="classpath:linkd/nms10205b/"+MUMBAI_NAME+"_"+MUMBAI_IP+".txt"),
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource="classpath:linkd/nms10205b/"+DELHI_NAME+"_"+DELHI_IP+".txt"),
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource="classpath:linkd/nms10205b/"+BANGALORE_NAME+"_"+BANGALORE_IP+".txt"),
            @JUnitSnmpAgent(host=BAGMANE_IP, port=161, resource="classpath:linkd/nms10205b/"+BAGMANE_NAME+"_"+BAGMANE_IP+".txt"),
            @JUnitSnmpAgent(host=MYSORE_IP, port=161, resource="classpath:linkd/nms10205b/"+MYSORE_NAME+"_"+MYSORE_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource="classpath:linkd/nms10205b/"+SPACE_EX_SW1_NAME+"_"+SPACE_EX_SW1_IP+".txt"),
            @JUnitSnmpAgent(host=SPACE_EX_SW2_IP, port=161, resource="classpath:linkd/nms10205b/"+SPACE_EX_SW2_NAME+"_"+SPACE_EX_SW2_IP+".txt"),
            @JUnitSnmpAgent(host=J6350_42_IP, port=161, resource="classpath:linkd/nms10205b/"+"J6350-42"+"_"+J6350_42_IP+".txt"),
            @JUnitSnmpAgent(host=SRX_100_IP, port=161, resource="classpath:linkd/nms10205b/"+"SRX-100_"+SRX_100_IP+".txt")
    })
    @Transactional
    public final void testCapsdNms10205b() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();

        m_capsd.scanSuspectInterface(MUMBAI_IP);
        m_capsd.scanSuspectInterface(DELHI_IP);
        m_capsd.scanSuspectInterface(BANGALORE_IP);
        m_capsd.scanSuspectInterface(BAGMANE_IP);
        m_capsd.scanSuspectInterface(MYSORE_IP);
        m_capsd.scanSuspectInterface(SPACE_EX_SW1_IP);
        m_capsd.scanSuspectInterface(SPACE_EX_SW2_IP);
        m_capsd.scanSuspectInterface(J6350_42_IP);
        m_capsd.scanSuspectInterface(SRX_100_IP);

        printNode(MUMBAI_IP,"MUMBAI");
        printNode(DELHI_IP,"DELHI");
        printNode(BANGALORE_IP,"BANGALORE");
        printNode(BAGMANE_IP,"BAGMANE");
        printNode(MYSORE_IP,"MYSORE");
        printNode(SPACE_EX_SW1_IP,"SPACE_EX_SW1");
        printNode(SPACE_EX_SW2_IP,"SPACE_EX_SW2");
        printNode(J6350_42_IP,"J6350_42");
        printNode(SRX_100_IP,"SRX_100");

        m_capsd.stop();        
    }    
}
