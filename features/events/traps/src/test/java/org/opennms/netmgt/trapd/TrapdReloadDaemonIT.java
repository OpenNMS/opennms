/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.TrapdConfigFactory;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-trapDaemon.xml",
        "classpath:/org/opennms/netmgt/trapd/applicationContext-trapDaemonTest.xml"}
)
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class TrapdReloadDaemonIT implements InitializingBean {
    
    private final static int SNMP_PORT_AFTER_RELOAD = 1163;

    @Autowired
    private Trapd m_trapd = null;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    @Autowired
    private TrapdConfigFactory m_trapdConfig;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }
    
    @Before
    public void setup() throws IOException {
        m_eventMgr.setSynchronous(true);
        int newPort = getAvailablePort(1164, 2162);
        TrapdConfigBean config = new TrapdConfigBean(m_trapdConfig);
        config.setSnmpTrapPort(newPort);
        m_trapdConfig.update(config);
        m_trapd.start();
    }

    @Test
    public void testTrapdReloadDaemon() throws Exception {
        String localhost = "127.0.0.1";
        InetAddress localAddr = InetAddressUtils.addr(localhost);
        SnmpObjId enterpriseId = SnmpObjId.get(".1.3.6.1.4.1.5813");
        SnmpObjId trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(1));
        SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), SnmpUtils.getValueFactory().getObjectId(trapOID));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), SnmpUtils.getValueFactory().getObjectId(enterpriseId));
        EventBuilder defaultTrapBuilder = new EventBuilder("uei.opennms.org/default/trap", "trapd");
        defaultTrapBuilder.setInterface(localAddr);
        defaultTrapBuilder.setSnmpVersion("v2c");
        m_eventMgr.getEventAnticipator().anticipateEvent(defaultTrapBuilder.getEvent());
        EventBuilder newSuspectBuilder = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "trapd");
        newSuspectBuilder.setInterface(localAddr);
        m_eventMgr.getEventAnticipator().anticipateEvent(newSuspectBuilder.getEvent());
        pdu.send(localhost, m_trapdConfig.getSnmpTrapPort(), "public");
        m_eventMgr.getEventAnticipator().verifyAnticipated(5000, 0, 0, 0, 0);
        m_eventMgr.getEventAnticipator().reset();

        // Verify reload by changing port in configuration to 1163  and new-suspect-on-trap = false;
        File opennmsHome = Paths.get("src", "test", "resources", "trapd").toFile();
        System.setProperty("opennms.home", opennmsHome.getAbsolutePath());
        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "trapd-reload-daemon-test");
        eventBuilder.addParam(EventConstants.PARM_DAEMON_NAME, m_trapd.getName());
        // Anticipate reload successful event
        EventBuilder reloadDaemonBuilder = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, m_trapd.getName());
        reloadDaemonBuilder.addParam(EventConstants.PARM_DAEMON_NAME, m_trapd.getName());
        m_eventMgr.getEventAnticipator().anticipateEvent(reloadDaemonBuilder.getEvent());
        // Send reload event to trapd
        m_trapd.handleReloadEvent(eventBuilder.getEvent());
        m_eventMgr.getEventAnticipator().verifyAnticipated(5000, 0, 0, 0, 0);
        m_eventMgr.getEventAnticipator().reset();

        // Anticipate default trap event again by sending trap to the new port
        m_eventMgr.getEventAnticipator().anticipateEvent(defaultTrapBuilder.getEvent());
        pdu.send(localhost, SNMP_PORT_AFTER_RELOAD, "public");
        // The updated value of "new-suspect-on-trap = false" is not currently propagated to the TrapSinkConsumer
        // so we should continue to receive a newSuspectEvent
        m_eventMgr.getEventAnticipator().anticipateEvent(newSuspectBuilder.getEvent());
        m_eventMgr.getEventAnticipator().verifyAnticipated(5000, 0, 0, 0, 0);
        m_eventMgr.getEventAnticipator().reset();
    }

    @After
    public void destroy() {
        m_trapd.stop();
    }

    private static int getAvailablePort(int current, final int max) {
        while (current < max) {
            try (final ServerSocket socket = new ServerSocket(current)) {
                return socket.getLocalPort();
            } catch (final Throwable e) {}
            current++;
        }
        throw new IllegalStateException("Can't find an available network port");
    }
}
