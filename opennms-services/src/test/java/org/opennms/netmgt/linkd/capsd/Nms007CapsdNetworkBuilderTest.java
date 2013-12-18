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
import org.opennms.netmgt.linkd.nb.Nms007NetworkBuilder;
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
public class Nms007CapsdNetworkBuilderTest extends Nms007NetworkBuilder implements InitializingBean {

    
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
            @JUnitSnmpAgent(host = FireFly170_IP, port = 161, resource = "classpath:linkd/nms007/MIB2_"+FireFly170_IP + ".txt"),
            @JUnitSnmpAgent(host = FireFly171_IP, port = 161, resource = "classpath:linkd/nms007/MIB2_"+FireFly171_IP + ".txt"),
            @JUnitSnmpAgent(host = FireFly172_IP, port = 161, resource = "classpath:linkd/nms007/MIB2_"+FireFly172_IP + ".txt"),
            @JUnitSnmpAgent(host = FireFly173_IP, port = 161, resource = "classpath:linkd/nms007/MIB2_"+FireFly173_IP + ".txt"),
            @JUnitSnmpAgent(host = FireFly174_IP, port = 161, resource = "classpath:linkd/nms007/MIB2_"+FireFly174_IP + ".txt"),
            @JUnitSnmpAgent(host = FireFly175_IP, port = 161, resource = "classpath:linkd/nms007/MIB2_"+FireFly175_IP + ".txt"),
            @JUnitSnmpAgent(host = FireFly176_IP, port = 161, resource = "classpath:linkd/nms007/MIB2_"+FireFly176_IP + ".txt"),
            @JUnitSnmpAgent(host = FireFly177_IP, port = 161, resource = "classpath:linkd/nms007/MIB2_"+FireFly177_IP + ".txt"),
            @JUnitSnmpAgent(host = FireFly189_IP, port = 161, resource = "classpath:linkd/nms007/MIB2_"+FireFly189_IP + ".txt")
    })
    @Transactional
    public final void testCapsdNms007() throws MarshalException, ValidationException, IOException {
        m_capsd.init();
        m_capsd.start();
        m_capsd.scanSuspectInterface(FireFly170_IP);
        m_capsd.scanSuspectInterface(FireFly171_IP);
        m_capsd.scanSuspectInterface(FireFly172_IP);
        m_capsd.scanSuspectInterface(FireFly173_IP);
        m_capsd.scanSuspectInterface(FireFly174_IP);
        m_capsd.scanSuspectInterface(FireFly175_IP);
        m_capsd.scanSuspectInterface(FireFly176_IP);
        m_capsd.scanSuspectInterface(FireFly177_IP);
        m_capsd.scanSuspectInterface(FireFly189_IP);

        printNode(FireFly170_IP,"FireFly170");
        printNode(FireFly171_IP,"FireFly171");
        printNode(FireFly172_IP,"FireFly172");
        printNode(FireFly173_IP,"FireFly173");
        printNode(FireFly174_IP,"FireFly174");
        printNode(FireFly175_IP,"FireFly175");
        printNode(FireFly176_IP,"FireFly176");
        printNode(FireFly177_IP,"FireFly177");
        printNode(FireFly189_IP,"FireFly189");
        
        m_capsd.stop();
    }
}
