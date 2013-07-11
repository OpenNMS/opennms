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
public class Nms10205bLldpTest extends Nms10205bNetworkBuilder implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = J6350_42_IP, port = 161, resource = "classpath:linkd/nms10205b/" + "J6350-42_" + J6350_42_IP + ".txt")
    })
    public void testNetwork10205bJ63542LldpLocalBaseCollection() throws Exception {

        String name = "lldpLocGroup";
        LldpLocalGroup m_lLldpLocalGroup = new LldpLocalGroup(InetAddressUtils.addr(J6350_42_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_lLldpLocalGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(J6350_42_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        assertEquals(4, m_lLldpLocalGroup.getLldpLocChassisidSubType().intValue());
        assertEquals(J6350_42_LLDP_CHASSISID, m_lLldpLocalGroup.getLldpLocChassisid());
        assertEquals(J6350_42_NAME, m_lLldpLocalGroup.getLldpLocSysname());
    }


    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = J6350_42_IP, port = 161, resource = "classpath:linkd/nms10205b/" + "J6350-42_" + J6350_42_IP + ".txt")
    })
    public void testNetwork10205bJ63542LldpRemTableCollection() throws Exception {

        String name = "lldpRemTable";
        LldpRemTable m_lldpRemTable = new LldpRemTable(InetAddressUtils.addr(J6350_42_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_lldpRemTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(J6350_42_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }

        final Collection<LldpRemTableEntry> lldpTableEntryCollection = m_lldpRemTable.getEntries();
        assertEquals(0, lldpTableEntryCollection.size());
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = J6350_42_IP, port = 161, resource = "classpath:linkd/nms10205b/" + "J6350-42_" + J6350_42_IP + ".txt")
    })
    public void testNetwork10205bJ63542LldpLocTableCollection() throws Exception {

        String name = "lldpLocTable";
        LldpLocTable m_lldpLocTable = new LldpLocTable(InetAddressUtils.addr(J6350_42_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_lldpLocTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(J6350_42_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }

        final Collection<LldpLocTableEntry> lldpTableEntryCollection = m_lldpLocTable.getEntries();
        assertEquals(4, lldpTableEntryCollection.size());
        for (final LldpLocTableEntry entry : lldpTableEntryCollection) {
            assertEquals(7, entry.getLldpLocPortIdSubtype().intValue());
        }

    }
}
