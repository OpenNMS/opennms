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

package org.opennms.netmgt.linkd.snmp;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.linkd.nb.Nms10205bNetworkBuilder;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.provisiond.enableDiscovery=false")
public class Nms10205bOspfTest extends Nms10205bNetworkBuilder implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = MUMBAI_IP, port = 161, resource = "classpath:linkd/nms10205b/" + MUMBAI_NAME + "_" + MUMBAI_IP + ".txt")
    })
    public void testNetwork10205bMumbayOspfGeneralGroupCollection() throws Exception {

        String name = "ospfGeneralGroup";
        OspfGeneralGroup m_ospfGeneralGroup = new OspfGeneralGroup(InetAddressUtils.addr(MUMBAI_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfGeneralGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(MUMBAI_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        assertEquals(MUMBAI_OSPF_ID, m_ospfGeneralGroup.getOspfRouterId());
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = SRX_100_IP, port = 161, resource = "classpath:linkd/nms10205b/" + "SRX-100_" + SRX_100_IP + ".txt")
    })
    public void testNetwork10205bSrx100OspfGeneralGroupCollection() throws Exception {

        String name = "ospfGeneralGroup";
        OspfGeneralGroup m_ospfGeneralGroup = new OspfGeneralGroup(InetAddressUtils.addr(SRX_100_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfGeneralGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SRX_100_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        assertEquals(SRX_100_OSPF_ID, m_ospfGeneralGroup.getOspfRouterId());
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = MUMBAI_IP, port = 161, resource = "classpath:linkd/nms10205b/" + MUMBAI_NAME + "_" + MUMBAI_IP + ".txt")
    })
    public void testNetwork10205bMumbayOspfIfTableCollection() throws Exception {

        String name = "ospfIfTable";
        OspfIfTable m_ospfIfTable = new OspfIfTable(InetAddressUtils.addr(MUMBAI_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfIfTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(MUMBAI_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        final Collection<OspfIfTableEntry> ospfifTableCollection = m_ospfIfTable.getEntries();
        assertEquals(6, ospfifTableCollection.size());
        for (final OspfIfTableEntry entry : ospfifTableCollection) {
            assertEquals(0, entry.getOspfAddressLessIf().intValue());
            InetAddress ospfIpAddress = entry.getOspfIpAddress();
            assertEquals(true, MUMBAI_IP_IF_MAP.containsKey(ospfIpAddress));
        }
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = SRX_100_IP, port = 161, resource = "classpath:linkd/nms10205b/" + "SRX-100_" + SRX_100_IP + ".txt")
    })
    public void testNetwork10205bSrx100OspfIfTableCollection() throws Exception {

        String name = "ospfIfTable";
        OspfIfTable m_ospfIfTable = new OspfIfTable(InetAddressUtils.addr(SRX_100_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfIfTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SRX_100_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        final Collection<OspfIfTableEntry> ospfifTableCollection = m_ospfIfTable.getEntries();
        assertEquals(0, ospfifTableCollection.size());
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = MUMBAI_IP, port = 161, resource = "classpath:linkd/nms10205b/" + MUMBAI_NAME + "_" + MUMBAI_IP + ".txt")
    })
    public void testNetwork10205bMumbayOspfNbrTableCollection() throws Exception {

        String name = "ospfNbrTable";
        OspfNbrTable m_ospfNbrTable = new OspfNbrTable(InetAddressUtils.addr(MUMBAI_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfNbrTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(MUMBAI_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        final Collection<OspfNbrTableEntry> ospfNbrTableCollection = m_ospfNbrTable.getEntries();
        assertEquals(4, ospfNbrTableCollection.size());
        for (final OspfNbrTableEntry entry : ospfNbrTableCollection) {
            assertEquals(0, entry.getOspfNbrAddressLessIndex().intValue());
            assertEquals(OspfNbrTableEntry.OSPF_NBR_STATE_FULL, entry.getOspfNbrState());
            checkrow(entry);
        }
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = SRX_100_IP, port = 161, resource = "classpath:linkd/nms10205b/" + "SRX-100_" + SRX_100_IP + ".txt")
    })
    public void testNetwork10205bSrx100OspfNbrTableCollection() throws Exception {

        String name = "ospfNbrTable";
        OspfNbrTable m_ospfNbrTable = new OspfNbrTable(InetAddressUtils.addr(SRX_100_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_ospfNbrTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SRX_100_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        final Collection<OspfNbrTableEntry> ospfNbrTableCollection = m_ospfNbrTable.getEntries();
        assertEquals(0, ospfNbrTableCollection.size());
    }

    private void checkrow(OspfNbrTableEntry entry) {
        InetAddress ip = entry.getOspfNbrIpAddress();
        if (ip.getHostAddress().equals("192.168.5.10")) {
            assertEquals(DELHI_OSPF_ID, entry.getOspfNbrRouterId());
            assertEquals(true, DELHI_IP_IF_MAP.containsKey(ip));
        } else if (ip.getHostAddress().equals("192.168.5.14")) {
            assertEquals(BANGALORE_OSPF_ID, entry.getOspfNbrRouterId());
            assertEquals(true, BANGALORE_IP_IF_MAP.containsKey(ip));
        } else if (ip.getHostAddress().equals("192.168.5.18")) {
            assertEquals(BAGMANE_OSPF_ID, entry.getOspfNbrRouterId());
            assertEquals(true, BAGMANE_IP_IF_MAP.containsKey(ip));
        } else if (ip.getHostAddress().equals("192.168.5.22")) {
            assertEquals(MYSORE_OSPF_ID, entry.getOspfNbrRouterId());
            assertEquals(true, MYSORE_IP_IF_MAP.containsKey(ip));
        } else {
            assertEquals(true, false);
        }
    }
}
