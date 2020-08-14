/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmpinterfacepoller.pollable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.mock.*;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.DefaultPollContext;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.Poller;
import org.opennms.netmgt.poller.pollables.PollableNetwork;
import org.opennms.netmgt.snmpinterfacepoller.SnmpPoller;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import static org.mockito.Mockito.mock;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-snmpinterfacepollerd.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SnmpPollerIT {

    @Autowired
    private SnmpPoller m_snmpPoller;

    @Autowired
    private PollContext m_pollContext;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        NetworkBuilder nb = new NetworkBuilder();

        nb.addNode("cisco2691").setForeignSource("linkd").setForeignId("cisco2691").setSysObjectId(".1.3.6.1.4.1.9.1.122").setType(OnmsNode.NodeType.ACTIVE);
        OnmsSnmpInterface null0 = new OnmsSnmpInterface(nb.getCurrentNode(), 4);
        null0.setIfSpeed(10000000l);
        null0.setPoll("P");
        null0.setIfType(6);
        null0.setCollectionEnabled(false);
        null0.setIfOperStatus(2);
        null0.setIfDescr("Null0");
        nb.addInterface("10.1.4.2", null0).setIsSnmpPrimary("P").setIsManaged("M");
        OnmsSnmpInterface fa0 = new OnmsSnmpInterface(nb.getCurrentNode(), 2);
        fa0.setIfSpeed(100000000l);
        fa0.setPoll("P");
        fa0.setIfType(6);
        fa0.setCollectionEnabled(false);
        fa0.setIfOperStatus(1);
        fa0.setIfDescr("FastEthernet0");
        nb.addInterface("10.1.5.1", fa0).setIsSnmpPrimary("S").setIsManaged("M");
        OnmsSnmpInterface eth0 = new OnmsSnmpInterface(nb.getCurrentNode(), 1);
        eth0.setIfSpeed(100000000l);
        eth0.setPoll("P");
        eth0.setIfType(6);
        eth0.setCollectionEnabled(false);
        eth0.setIfOperStatus(1);
        eth0.setIfDescr("Ethernet0");
        nb.addInterface("10.1.7.1", eth0).setIsSnmpPrimary("S").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());

        nb.addNode("cisco1700").setForeignSource("linkd").setForeignId("cisco1700").setSysObjectId(".1.3.6.1.4.1.9.1.200").setType(OnmsNode.NodeType.ACTIVE);
        OnmsSnmpInterface eth1 = new OnmsSnmpInterface(nb.getCurrentNode(), 2);
        eth1.setIfSpeed(100000000l);
        eth1.setPoll("P");
        eth1.setIfType(6);
        eth1.setCollectionEnabled(false);
        eth1.setIfOperStatus(1);
        eth1.setIfDescr("Ethernet1");
        nb.addInterface("10.1.5.2", eth1).setIsSnmpPrimary("P").setIsManaged("M");
        m_nodeDao.save(nb.getCurrentNode());

        m_nodeDao.flush();
        if (!m_snmpPoller.isInitialized()) {
            m_snmpPoller.init();
            m_snmpPoller.start();
        }
    }

    @Test
    public void testSnmpInterfacePoller() {
    }

    private void anticipateDown(OnmsSnmpInterface onmsSnmpInterface, String ipaddr) {
        EventBuilder eventBuilder = new EventBuilder(EventConstants.SNMP_INTERFACE_OPER_DOWN_EVENT_UEI, "OpenNMS.SnmpPoller.DefaultPollContext");
        eventBuilder.setTime(new Date());
        eventBuilder.setNodeid(onmsSnmpInterface.getNodeId());
        eventBuilder.setInterface(InetAddressUtils.addr(ipaddr));
        eventBuilder.setService("SNMP");
        Event event = eventBuilder.getEvent();
        m_eventMgr.getEventAnticipator().anticipateEvent(event);
    }
}
