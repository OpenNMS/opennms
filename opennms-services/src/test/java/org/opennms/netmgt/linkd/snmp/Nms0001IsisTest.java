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
import java.util.Iterator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.linkd.nb.Nms0001NetworkBuilder;
import org.opennms.netmgt.linkd.snmp.IsIsSystemObjectGroup.IsisAdminState;
import org.opennms.netmgt.linkd.snmp.IsisISAdjTableEntry.IsisISAdjNeighSysType;
import org.opennms.netmgt.linkd.snmp.IsisISAdjTableEntry.IsisISAdjState;
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
public class Nms0001IsisTest extends Nms0001NetworkBuilder implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = "classpath:linkd/nms0001/" + FROH_NAME + "-"+FROH_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = "classpath:linkd/nms0001/" + OEDIPUS_NAME + "-"+OEDIPUS_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = "classpath:linkd/nms0001/" + SIEGFRIE_NAME + "-"+SIEGFRIE_IP + "-walk.txt")
    })
    public void testIsisSysObjGroupCollection() throws Exception {

        String name = "isisSystemObjectGroup";

        // froh
        IsIsSystemObjectGroup m_isisSystemObjectGroup = new IsIsSystemObjectGroup(InetAddressUtils.addr(FROH_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_isisSystemObjectGroup};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(FROH_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }

        assertEquals(IsisAdminState.ON, m_isisSystemObjectGroup.getIsisSysAdminState());
        assertEquals(FROH_ISIS_SYS_ID, m_isisSystemObjectGroup.getIsisSysId());
        
        // oedipus
        m_isisSystemObjectGroup = new IsIsSystemObjectGroup(InetAddressUtils.addr(OEDIPUS_IP));
        tracker = new CollectionTracker[]{m_isisSystemObjectGroup};
        snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(OEDIPUS_IP));
        walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(IsisAdminState.ON, m_isisSystemObjectGroup.getIsisSysAdminState());
        assertEquals(OEDIPUS_ISIS_SYS_ID, m_isisSystemObjectGroup.getIsisSysId());

        // siegfrie
        m_isisSystemObjectGroup = new IsIsSystemObjectGroup(InetAddressUtils.addr(SIEGFRIE_IP));
        tracker = new CollectionTracker[]{m_isisSystemObjectGroup};
        snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(SIEGFRIE_IP));
        walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {

        }
        
        assertEquals(IsisAdminState.ON, m_isisSystemObjectGroup.getIsisSysAdminState());
        assertEquals(SIEGFRIE_ISIS_SYS_ID, m_isisSystemObjectGroup.getIsisSysId());

    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = "classpath:linkd/nms0001/" + FROH_NAME + "-"+FROH_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = "classpath:linkd/nms0001/" + OEDIPUS_NAME + "-"+OEDIPUS_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = "classpath:linkd/nms0001/" + SIEGFRIE_NAME + "-"+SIEGFRIE_IP + "-walk.txt")
    })
    public void testIsisISAdjTableCollection() throws Exception {

        String name = "isisISAdjTable";
        IsisISAdjTable m_isisISAdjTable = new IsisISAdjTable(InetAddressUtils.addr(FROH_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_isisISAdjTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(FROH_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }

        Collection<IsisISAdjTableEntry> isisISAdjTableEntryCollection = m_isisISAdjTable.getEntries();
        assertEquals(2, isisISAdjTableEntryCollection.size());
        Iterator<IsisISAdjTableEntry> iter = isisISAdjTableEntryCollection.iterator();
        IsisISAdjTableEntry entry1 = iter.next();
        assertEquals(599, entry1.getIsisCircIndex().intValue());
        assertEquals(1, entry1.getIsisISAdjIndex().intValue());
        assertEquals(IsisISAdjState.UP, entry1.getIsIsAdjStatus());
        assertEquals("001f12accbf1", entry1.getIsIsAdjNeighSnpaAddress());
        assertEquals(IsisISAdjNeighSysType.l1_IntermediateSystem, entry1.getIsisISAdjNeighSysType());
        assertEquals(OEDIPUS_ISIS_SYS_ID, entry1.getIsIsAdjNeighSysId());
        assertEquals(0, entry1.getIsisAdjNbrExtendedCircID().intValue());
        
        IsisISAdjTableEntry entry2 = iter.next();
        assertEquals(600, entry2.getIsisCircIndex().intValue());
        assertEquals(1, entry2.getIsisISAdjIndex().intValue());
        assertEquals(IsisISAdjState.UP, entry2.getIsIsAdjStatus());
        assertEquals("001f12acc3f2", entry2.getIsIsAdjNeighSnpaAddress());
        assertEquals(IsisISAdjNeighSysType.l1_IntermediateSystem, entry2.getIsisISAdjNeighSysType());
        assertEquals(SIEGFRIE_ISIS_SYS_ID, entry2.getIsIsAdjNeighSysId());
        assertEquals(0, entry2.getIsisAdjNbrExtendedCircID().intValue());

        
    }

    @Test
    @JUnitSnmpAgents(value = {
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = "classpath:linkd/nms0001/" + FROH_NAME + "-"+FROH_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = "classpath:linkd/nms0001/" + OEDIPUS_NAME + "-"+OEDIPUS_IP + "-walk.txt"),
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = "classpath:linkd/nms0001/" + SIEGFRIE_NAME + "-"+SIEGFRIE_IP + "-walk.txt")
    })
    public void testIsisCircTableCollection() throws Exception {

        String name = "isisCircTable";
        IsisCircTable m_isisCircTable = new IsisCircTable(InetAddressUtils.addr(FROH_IP));
        CollectionTracker[] tracker = new CollectionTracker[0];
        tracker = new CollectionTracker[]{m_isisCircTable};
        SnmpAgentConfig snmpAgent = SnmpPeerFactory.getInstance().getAgentConfig(InetAddressUtils.addr(FROH_IP));
        SnmpWalker walker = SnmpUtils.createWalker(snmpAgent, name, tracker);
        walker.start();

        try {
            walker.waitFor();
        } catch (final InterruptedException e) {
            assertEquals(false, true);
        }

        Collection<IsisCircTableEntry> isisCircTableEntryCollection = m_isisCircTable.getEntries();
        assertEquals(3, isisCircTableEntryCollection.size());
        Iterator<IsisCircTableEntry> iter = isisCircTableEntryCollection.iterator();
        IsisCircTableEntry entry1 = iter.next();
        assertEquals(16, entry1.getIsisCircIndex().intValue());
        assertEquals(16, entry1.getIsisCircIfIndex().intValue());
        
        IsisCircTableEntry entry2 = iter.next();
        assertEquals(599, entry2.getIsisCircIndex().intValue());
        assertEquals(599, entry2.getIsisCircIfIndex().intValue());

        IsisCircTableEntry entry3 = iter.next();
        assertEquals(600, entry3.getIsisCircIndex().intValue());
        assertEquals(600, entry3.getIsisCircIfIndex().intValue());
        
    }

}
