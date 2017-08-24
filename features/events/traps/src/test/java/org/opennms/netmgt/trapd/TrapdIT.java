/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import java.net.InetAddress;

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
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * TODO: Merge with {@link org.opennms.netmgt.trapd.TrapHandlerITCase}?
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-trapDaemon.xml",
        // Overrides the port that Trapd binds to and sets newSuspectOnTrap to 'true'
        "classpath:/org/opennms/netmgt/trapd/applicationContext-trapDaemonTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class TrapdIT implements InitializingBean {

    @Autowired
    private TrapdConfigFactory m_trapdConfig;

    @Autowired
    Trapd m_trapd;

    @Autowired
    MockEventIpcManager m_mockEventIpcManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void onSetUpInTransactionIfEnabled() throws Exception {
        m_mockEventIpcManager.setSynchronous(true);

        m_trapd.onStart();
    }

    @After
    public void onTearDownInTransactionIfEnabled() throws Exception {
        m_trapd.onStop();

        m_mockEventIpcManager.getEventAnticipator().verifyAnticipated(3000, 0, 0, 0, 0);
    }

    @Test
    public void testSnmpV1TrapSend() throws Exception {
        String localhost = "127.0.0.1";
        InetAddress localAddr = InetAddressUtils.addr(localhost);

        SnmpV1TrapBuilder pdu = SnmpUtils.getV1TrapBuilder();
        pdu.setEnterprise(SnmpObjId.get(".1.3.6.1.4.1.5813"));
        pdu.setGeneric(1);
        pdu.setSpecific(0);
        pdu.setTimeStamp(666L);
        pdu.setAgentAddress(localAddr);

        EventBuilder defaultTrapBuilder = new EventBuilder("uei.opennms.org/default/trap", "trapd");
        defaultTrapBuilder.setInterface(localAddr);
        defaultTrapBuilder.setSnmpVersion("v1");
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(defaultTrapBuilder.getEvent());

        EventBuilder newSuspectBuilder = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "trapd");
        newSuspectBuilder.setInterface(localAddr);
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(newSuspectBuilder.getEvent());

        pdu.send(localhost, m_trapdConfig.getSnmpTrapPort(), "public");

        // Allow time for Trapd and Eventd to do their magic
        Thread.sleep(5000);
    }

    @Test
    public void testSnmpV2cTrapSend() throws Exception {
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
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(defaultTrapBuilder.getEvent());

        EventBuilder newSuspectBuilder = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "trapd");
        newSuspectBuilder.setInterface(localAddr);
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(newSuspectBuilder.getEvent());

        pdu.send(localhost, m_trapdConfig.getSnmpTrapPort(), "public");

        // Allow time for Trapd and Eventd to do their magic
        Thread.sleep(5000);
    }

    /**
     * Verifies that we can pull the agent address from the snmpTrapAddress
     * varbind in a SNMPv2 trap.
     */
    @Test
    public void testSnmpV2cTrapWithAddressFromVarbind() throws Exception {
        // Enable the feature (disabled by default)
        m_trapdConfig.getConfig().setUseAddressFromVarbind(true);

        String localhost = "127.0.0.1";
        InetAddress remoteAddr = InetAddress.getByName("10.255.1.1");

        SnmpObjId enterpriseId = SnmpObjId.get(".1.3.6.1.4.1.5813");
        SnmpObjId trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(1));
        SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), SnmpUtils.getValueFactory().getObjectId(trapOID));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), SnmpUtils.getValueFactory().getObjectId(enterpriseId));
        // The varbind with the address
        pdu.addVarBind(TrapUtils.SNMP_TRAP_ADDRESS_OID, SnmpUtils.getValueFactory().getIpAddress(InetAddress.getByName("10.255.1.1")));

        EventBuilder defaultTrapBuilder = new EventBuilder("uei.opennms.org/default/trap", "trapd");
        defaultTrapBuilder.setInterface(remoteAddr);
        defaultTrapBuilder.setSnmpVersion("v2c");
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(defaultTrapBuilder.getEvent());

        EventBuilder newSuspectBuilder = new EventBuilder(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, "trapd");
        // The address in the newSuspect event should match the one specified in the varbind
        newSuspectBuilder.setInterface(remoteAddr);
        m_mockEventIpcManager.getEventAnticipator().anticipateEvent(newSuspectBuilder.getEvent());

        pdu.send(localhost, m_trapdConfig.getSnmpTrapPort(), "public");

        // Allow time for Trapd and Eventd to do their magic
        Thread.sleep(5000);
    }
}
