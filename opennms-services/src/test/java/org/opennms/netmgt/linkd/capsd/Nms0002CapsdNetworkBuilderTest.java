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
import org.opennms.netmgt.linkd.nb.Nms0002NetworkBuilder;
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
public class Nms0002CapsdNetworkBuilderTest extends Nms0002NetworkBuilder implements InitializingBean {

    
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
//            @JUnitSnmpAgent(host = RPict001_IP, port = 161, resource = "classpath:linkd/nms0002UkRoFakeLink/" + RPict001_NAME+".txt"),
//            @JUnitSnmpAgent(host = RNewt103_IP, port = 161, resource = "classpath:linkd/nms0002UkRoFakeLink/" + RNewt103_NAME+".txt"),
//            @JUnitSnmpAgent(host = Rluck001_IP, port = 161, resource = "classpath:linkd/nms0002ciscojuniper/" + Rluck001_NAME +".txt"),
//            @JUnitSnmpAgent(host = Sluck001_IP, port = 161, resource = "classpath:linkd/nms0002ciscojuniper/" + Sluck001_NAME+ ".txt"),
//            @JUnitSnmpAgent(host = RDeEssnBrue_IP, port = 161, resource = "classpath:linkd/nms0002ciscoalcatel2/" + RDeEssnBrue_NAME+ ".txt"),
//            @JUnitSnmpAgent(host = SDeEssnBrue165_IP, port = 161, resource = "classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue165_NAME+ ".txt"),
//            @JUnitSnmpAgent(host = SDeEssnBrue081_IP, port = 161, resource = "classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue081_NAME+ ".txt"),
//            @JUnitSnmpAgent(host = SDeEssnBrue121_IP, port = 161, resource = "classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue121_NAME+ ".txt"),
            @JUnitSnmpAgent(host = SDeEssnBrue142_IP, port = 161, resource = "classpath:linkd/nms0002ciscoalcatel2/" + SDeEssnBrue142_NAME+ ".txt")
    })
    @Transactional
    public final void testCapsdNms0002() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        /*
        m_capsd.scanSuspectInterface(Rluck001_IP);
        m_capsd.scanSuspectInterface(Sluck001_IP);

        printNode(Rluck001_IP,"Rluck001");
        printNode(Sluck001_IP,"Sluck001");
        
        m_capsd.scanSuspectInterface(RPict001_IP);
        m_capsd.scanSuspectInterface(RNewt103_IP);
        
        printNode(RPict001_IP,"RPict001");
        printNode(RNewt103_IP,"RNewt103");
  
        m_capsd.scanSuspectInterface(RDeEssnBrue_IP);
        
        printNode(RDeEssnBrue_IP,"RDeEssnBrue");
  

        m_capsd.scanSuspectInterface(SDeEssnBrue165_IP);
        
        printNode(SDeEssnBrue165_IP,"SDeEssnBrue165");
        m_capsd.scanSuspectInterface(SDeEssnBrue081_IP);
        printNode(SDeEssnBrue081_IP,"SDeEssnBrue081");
        m_capsd.scanSuspectInterface(SDeEssnBrue121_IP);
        printNode(SDeEssnBrue121_IP,"SDeEssnBrue121");
        */

        m_capsd.scanSuspectInterface(SDeEssnBrue142_IP);
        
        printNode(SDeEssnBrue142_IP,"SDeEssnBrue142");
        m_capsd.stop();
    }
}
