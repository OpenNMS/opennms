/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BAGMANE_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BAGMANE_OSPF_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BAGMANE_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BAGMANE_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BANGALORE_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BANGALORE_OSPF_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BANGALORE_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BANGALORE_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_OSPF_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_42_OSPF_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_42_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_42_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_42_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_OSPF_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MYSORE_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MYSORE_OSPF_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MYSORE_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MYSORE_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW1_OSPF_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW1_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW2_OSPF_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW2_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SRX_100_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SRX_100_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SRX_100_SNMP_RESOURCE_B;
import java.util.List;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.OspfElement;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms10205bNetworkBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;

public class Nms10205bEnIT extends EnLinkdBuilderITCase {

	Nms10205bNetworkBuilder builder = new Nms10205bNetworkBuilder();

    /*
     * 
MUMBAI_10.205.56.5: (LLDP is not supported on this device_family=m320)
===================
root@Mumbai> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.14     ge-0/0/1.0             Full      192.168.9.1      128    31 ---->Bangalore
192.168.5.18     ge-0/0/2.0             Full      192.168.20.1     128    34 ---->Bagmane
192.168.5.22     ge-0/1/1.0             Full      192.168.22.1     128    38 ---->Mysore
192.168.5.10     ge-0/1/2.0             Full      192.168.7.1      128    35 ---->Delhi

DELHI_10.205.56.7:
==================
admin@Delhi> show lldp neighbors
Local Interface Chassis Id        Port info     System Name
ge-1/1/6        00:23:9c:02:3b:40  ge-0/0/6.0   Space-EX-SW1
ge-1/1/5        80:71:1f:c7:0f:c0  ge-1/0/1     Bagmane
admin@Delhi> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.1.6      ge-1/0/1.0             Full      192.168.9.1      128    31  ---->Bangalore
192.168.5.9      ge-1/0/2.0             Full      192.168.5.1      128    39  ---->Mumbai
172.16.7.2       ge-1/1/6.0             Full      10.205.56.1      128    33  ---->Space_ex_sw1


BANGALORE_10.205.56.9: (LLDP is not supported on this device_family=m7i)
======================
root@Bangalore> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.13     ge-0/0/0.0             Full      192.168.5.1      128    33  ---->Mumbai
192.168.1.5      ge-0/0/1.0             Full      192.168.7.1      128    32  ---->Delhi
172.16.9.2       ge-0/0/3.0             Full      10.205.56.2      128    34  ---->Space_ex_sw2
192.168.1.10     ge-0/1/0.0             Full      192.168.20.1     128    38  ---->Bagmane

Bagmane_10.205.56.20:
====================
admin@Bagmane> show lldp neighbors
Local Interface Chassis Id        Port info     System Name
ge-1/0/1        00:22:83:f1:67:c0  ge-1/1/5     Delhi
ge-1/0/3        00:26:88:6a:9a:80  ge-1/0/6.0   sw21
ge-1/0/2        2c:6b:f5:5d:c1:00  TO-BAMANE    J6350-2
admin@Bagmane> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.17     ge-1/0/0.0             Full      192.168.5.1      128    30 ----> Mumbai
172.16.20.2      ge-1/0/2.0             Full      10.205.56.42     128    31 ----> J6350_42
192.168.1.9      ge-1/0/4.0             Full      192.168.9.1      128    32 ----> Bangalore
192.168.1.14     ge-1/0/5.0             Full      192.168.22.1     128    33 ----> Mysore

Mysore_10.205.56.22:(LLDP is not supported on this device_family=m10i)
===================
admin@Mysore> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.21     ge-0/0/1.0             Full      192.168.5.1      128    32 ----> Mumbai
192.168.1.13     ge-0/1/1.0             Full      192.168.20.1     128    38 ----> bagmane

Space-EX-SW1_10.205.56.1:
=========================
root@Space-EX-SW1> show lldp neighbors
Local Interface    Parent Interface    Chassis Id          Port info          System Name
ge-0/0/0.0         -                   00:21:59:cf:4c:00   ge-0/0/0.0         Space-EX-SW2
ge-0/0/6.0         -                   00:22:83:f1:67:c0   ge-1/1/6           Delhi
ge-0/0/4.0         -                   00:26:88:6a:9a:80   ge-2/0/33.0        sw21
root@Space-EX-SW1> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
172.16.10.2      ge-0/0/0.0             Full      10.205.56.2      128    34 ----> Space_ex_sw2
172.16.7.1       ge-0/0/6.0             Full      192.168.7.1      128    32 ----> Delhi

Space-EX-SW2_10.205.56.2: 
=========================
root@Space-EX-SW2> show lldp neighbors
Local Interface    Parent Interface    Chassis Id          Port info     System Name
ge-0/0/0.0         -                   00:23:9c:02:3b:40   TO-EX-SW1    Space-EX-SW1
me0.0              -                   00:26:88:6a:9a:80   ge-0/0/15.0  sw21
root@Space-EX-SW2> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
172.16.10.1      ge-0/0/0.0             Full      10.205.56.1      128    35 ----> Space_ex_sw1
172.16.9.1       ge-0/0/3.0             Full      192.168.9.1      128    32 ----> Bangalore

*/    

	
    /*
     * 

MUMBAI_10.205.56.5: (LLDP is not supported on this device_family=m320)
===================

DELHI_10.205.56.7:
==================
admin@Delhi> show lldp neighbors
Local Interface Chassis Id        Port info     System Name
ge-1/1/6        00:23:9c:02:3b:40  ge-0/0/6.0   Space-EX-SW1
ge-1/1/5        80:71:1f:c7:0f:c0  ge-1/0/1     Bagmane

BANGALORE_10.205.56.9: (LLDP is not supported on this device_family=m7i)
======================

Bagmane_10.205.56.20:
====================
admin@Bagmane> show lldp neighbors
Local Interface Chassis Id        Port info     System Name
ge-1/0/1        00:22:83:f1:67:c0  ge-1/1/5     Delhi
ge-1/0/3        00:26:88:6a:9a:80  ge-1/0/6.0   sw21
ge-1/0/2        2c:6b:f5:5d:c1:00  TO-BAMANE    J6350-2

Mysore_10.205.56.22:(LLDP is not supported on this device_family=m10i)
===================

Space-EX-SW1_10.205.56.1:
=========================
root@Space-EX-SW1> show lldp neighbors
Local Interface    Parent Interface    Chassis Id          Port info          System Name
ge-0/0/0.0         -                   00:21:59:cf:4c:00   ge-0/0/0.0         Space-EX-SW2
ge-0/0/6.0         -                   00:22:83:f1:67:c0   ge-1/1/6           Delhi
ge-0/0/4.0         -                   00:26:88:6a:9a:80   ge-2/0/33.0        sw21

Space-EX-SW2_10.205.56.2: 
=========================
root@Space-EX-SW2> show lldp neighbors
Local Interface    Parent Interface    Chassis Id          Port info     System Name
ge-0/0/0.0         -                   00:23:9c:02:3b:40   TO-EX-SW1    Space-EX-SW1
me0.0              -                   00:26:88:6a:9a:80   ge-0/0/15.0  sw21

J6350-42_10.205.56.42:
=========================
does not support lldp rem table but
is linked to bagmane

SRX-100_10.205.56.23:
=========================
support LLDP 
it has a link to Mysore that does not support LLDP
 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource=MUMBAI_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource=DELHI_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource=BANGALORE_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=BAGMANE_IP, port=161, resource=BAGMANE_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=MYSORE_IP, port=161, resource=MYSORE_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource=SPACE_EX_SW1_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=SPACE_EX_SW2_IP, port=161, resource=SPACE_EX_SW2_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=J6350_42_IP, port=161, resource=J6350_42_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=SRX_100_IP, port=161, resource=SRX_100_SNMP_RESOURCE_B)
    })
    public void testNetwork10205bLldpLinks() {
        m_nodeDao.save(builder.getMumbai());
        m_nodeDao.save(builder.getDelhi());
        m_nodeDao.save(builder.getBangalore());
        m_nodeDao.save(builder.getBagmane());
        m_nodeDao.save(builder.getMysore());
        m_nodeDao.save(builder.getSpaceExSw1());
        m_nodeDao.save(builder.getSpaceExSw2());
        m_nodeDao.save(builder.getJ635042());
        m_nodeDao.save(builder.getSRX100());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());
        
        final OnmsNode mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME);
        final OnmsNode delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME);
        final OnmsNode bangalore = m_nodeDao.findByForeignId("linkd", BANGALORE_NAME);
        final OnmsNode bagmane = m_nodeDao.findByForeignId("linkd", BAGMANE_NAME);
        final OnmsNode mysore = m_nodeDao.findByForeignId("linkd", MYSORE_NAME);
        final OnmsNode spaceexsw1 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW1_NAME);
        final OnmsNode spaceexsw2 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW2_NAME);
        final OnmsNode j635042 = m_nodeDao.findByForeignId("linkd", J6350_42_NAME);
        final OnmsNode srx100 = m_nodeDao.findByForeignId("linkd", SRX_100_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(mumbai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delhi.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bangalore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bagmane.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mysore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635042.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(srx100.getId()));

        assertEquals(0,m_lldpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(mumbai.getId()));
        assertEquals(0,m_lldpLinkDao.findByNodeId(mumbai.getId()).size());

        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        List<LldpLink> delhilldpLinks = m_lldpLinkDao.findByNodeId(delhi.getId());
        printLldpTopology(delhilldpLinks);
        assertEquals(2,delhilldpLinks.size());

        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
        assertEquals(0,m_lldpLinkDao.findByNodeId(bangalore.getId()).size());

        assertTrue(m_linkd.runSingleSnmpCollection(bagmane.getId()));
        List<LldpLink> bagmanelldpLinks = m_lldpLinkDao.findByNodeId(bagmane.getId());
        printLldpTopology(bagmanelldpLinks);
        assertEquals(3,bagmanelldpLinks.size());

        assertTrue(m_linkd.runSingleSnmpCollection(mysore.getId()));
        assertEquals(0,m_lldpLinkDao.findByNodeId(mysore.getId()).size());

        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        List<LldpLink> spaceexsw1lldpLinks = m_lldpLinkDao.findByNodeId(spaceexsw1.getId());
        printLldpTopology(spaceexsw1lldpLinks);
        assertEquals(3,spaceexsw1lldpLinks.size());

        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
        List<LldpLink> spaceexsw2lldpLinks = m_lldpLinkDao.findByNodeId(spaceexsw2.getId());
        printLldpTopology(spaceexsw2lldpLinks);
        assertEquals(2,spaceexsw2lldpLinks.size());

        assertTrue(m_linkd.runSingleSnmpCollection(j635042.getId()));
        assertEquals(0,m_lldpLinkDao.findByNodeId(j635042.getId()).size());

        assertTrue(m_linkd.runSingleSnmpCollection(srx100.getId()));
        assertEquals(0,m_lldpLinkDao.findByNodeId(srx100.getId()).size());

        assertEquals(10,m_lldpLinkDao.countAll());
        assertEquals(6,m_lldpElementDao.countAll());
        for (final LldpElement node: m_lldpElementDao.findAll()) {
            printLldpElement(node);
        }

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater topologyUpdater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();

        assertNotNull(topology);
        printOnmsTopology(topology);

        for (OnmsTopologyEdge edge: topology.getEdges()) {
            switch (edge.getSource().getVertex().getLabel()) {
                case DELHI_NAME:
                    switch (edge.getTarget().getVertex().getLabel()) {
                        case BAGMANE_NAME:
                            assertEquals("ge-1/1/5", edge.getSource().getIfname());
                            assertEquals("28519 type LLDP_PORTID_SUBTYPE_LOCAL", edge.getSource().getAddr());
                            assertEquals(28519, edge.getSource().getIfindex().intValue());
                            assertEquals("ge-1/0/1", edge.getTarget().getIfname());
                            assertEquals("513 type LLDP_PORTID_SUBTYPE_LOCAL", edge.getTarget().getAddr());
                            assertEquals(513, edge.getTarget().getIfindex().intValue());
                            break;
                        case SPACE_EX_SW1_NAME:
                            assertEquals("ge-1/1/6", edge.getSource().getIfname());
                            assertEquals("28520 type LLDP_PORTID_SUBTYPE_LOCAL", edge.getSource().getAddr());
                            assertEquals(28520, edge.getSource().getIfindex().intValue());
                            assertEquals("ge-0/0/6.0", edge.getTarget().getIfname());
                            assertEquals("528 type LLDP_PORTID_SUBTYPE_LOCAL", edge.getTarget().getAddr());
                            assertEquals(528, edge.getTarget().getIfindex().intValue());
                            break;
                        default:
                            fail();
                    }
                    break;
                case SPACE_EX_SW1_NAME:
                    assertEquals(SPACE_EX_SW2_NAME,edge.getTarget().getVertex().getLabel());
                    assertEquals("ge-0/0/0.0", edge.getSource().getIfname());
                    assertEquals("1361 type LLDP_PORTID_SUBTYPE_LOCAL", edge.getSource().getAddr());
                    assertEquals(1361, edge.getSource().getIfindex().intValue());
                    assertEquals("ge-0/0/0.0", edge.getTarget().getIfname());
                    assertEquals("531 type LLDP_PORTID_SUBTYPE_LOCAL", edge.getTarget().getAddr());
                    assertEquals(531, edge.getTarget().getIfindex().intValue());
                    break;
                default:
                    fail();
            }
        }

    }
    

	
	/*
     * 
MUMBAI_10.205.56.5: 
===================
root@Mumbai> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.14     ge-0/0/1.0             Full      192.168.9.1      128    31 ---->Bangalore
192.168.5.18     ge-0/0/2.0             Full      192.168.20.1     128    34 ---->Bagmane
192.168.5.22     ge-0/1/1.0             Full      192.168.22.1     128    38 ---->Mysore
192.168.5.10     ge-0/1/2.0             Full      192.168.7.1      128    35 ---->Delhi

DELHI_10.205.56.7:
==================
admin@Delhi> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.1.6      ge-1/0/1.0             Full      192.168.9.1      128    31  ---->Bangalore
192.168.5.9      ge-1/0/2.0             Full      192.168.5.1      128    39  ---->Mumbai
172.16.7.2       ge-1/1/6.0             Full      10.205.56.1      128    33  ---->Space_ex_sw1

BANGALORE_10.205.56.9:
======================
root@Bangalore> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.13     ge-0/0/0.0             Full      192.168.5.1      128    33  ---->Mumbai
192.168.1.5      ge-0/0/1.0             Full      192.168.7.1      128    32  ---->Delhi
172.16.9.2       ge-0/0/3.0             Full      10.205.56.2      128    34  ---->Space_ex_sw2
192.168.1.10     ge-0/1/0.0             Full      192.168.20.1     128    38  ---->Bagmane

Bagmane_10.205.56.20:
====================
admin@Bagmane> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.17     ge-1/0/0.0             Full      192.168.5.1      128    30 ----> Mumbai
172.16.20.2      ge-1/0/2.0             Full      10.205.56.42     128    31 ----> J6350_42
192.168.1.9      ge-1/0/4.0             Full      192.168.9.1      128    32 ----> Bangalore
192.168.1.14     ge-1/0/5.0             Full      192.168.22.1     128    33 ----> Mysore

Mysore_10.205.56.22:
===================
admin@Mysore> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
192.168.5.21     ge-0/0/1.0             Full      192.168.5.1      128    32 ----> Mumbai
192.168.1.13     ge-0/1/1.0             Full      192.168.20.1     128    38 ----> bagmane

Space-EX-SW1_10.205.56.1:
=========================
root@Space-EX-SW1> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
172.16.10.2      ge-0/0/0.0             Full      10.205.56.2      128    34 ----> Space_ex_sw2
172.16.7.1       ge-0/0/6.0             Full      192.168.7.1      128    32 ----> Delhi

Space-EX-SW2_10.205.56.2: 
=========================
root@Space-EX-SW2> show ospf neighbor
Address          Interface              State     ID               Pri  Dead
172.16.10.1      ge-0/0/0.0             Full      10.205.56.1      128    35 ----> Space_ex_sw1
172.16.9.1       ge-0/0/3.0             Full      192.168.9.1      128    32 ----> Bangalore

*/
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MUMBAI_IP, port=161, resource=MUMBAI_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=DELHI_IP, port=161, resource=DELHI_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=BANGALORE_IP, port=161, resource=BANGALORE_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=BAGMANE_IP, port=161, resource=BAGMANE_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=MYSORE_IP, port=161, resource=MYSORE_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=SPACE_EX_SW1_IP, port=161, resource=SPACE_EX_SW1_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=SPACE_EX_SW2_IP, port=161, resource=SPACE_EX_SW2_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=J6350_42_IP, port=161, resource=J6350_42_SNMP_RESOURCE_B),
            @JUnitSnmpAgent(host=SRX_100_IP, port=161, resource=SRX_100_SNMP_RESOURCE_B)
    })
    public void testNetwork10205bOspfLinks() throws Exception {
        m_nodeDao.save(builder.getMumbai());
        m_nodeDao.save(builder.getDelhi());
        m_nodeDao.save(builder.getBangalore());
        m_nodeDao.save(builder.getBagmane());
        m_nodeDao.save(builder.getMysore());
        m_nodeDao.save(builder.getSpaceExSw1());
        m_nodeDao.save(builder.getSpaceExSw2());
        m_nodeDao.save(builder.getJ635042());
        m_nodeDao.save(builder.getSRX100());
        m_nodeDao.flush();

        final OnmsNode mumbai = m_nodeDao.findByForeignId("linkd", MUMBAI_NAME);
        final OnmsNode delhi = m_nodeDao.findByForeignId("linkd", DELHI_NAME);
        final OnmsNode bangalore = m_nodeDao.findByForeignId("linkd", BANGALORE_NAME);
        final OnmsNode bagmane = m_nodeDao.findByForeignId("linkd", BAGMANE_NAME);
        final OnmsNode mysore = m_nodeDao.findByForeignId("linkd", MYSORE_NAME);
        final OnmsNode spaceexsw1 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW1_NAME);
        final OnmsNode spaceexsw2 = m_nodeDao.findByForeignId("linkd", SPACE_EX_SW2_NAME);
        final OnmsNode j635042 = m_nodeDao.findByForeignId("linkd", J6350_42_NAME);
        final OnmsNode srx100 = m_nodeDao.findByForeignId("linkd", SRX_100_NAME);

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(true);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertFalse(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertTrue(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        assertTrue(m_linkd.scheduleNodeCollection(mumbai.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(delhi.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bangalore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(bagmane.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(mysore.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(spaceexsw2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(j635042.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(srx100.getId()));

        assertEquals(0,m_ospfLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(mumbai.getId()));
        OspfElement mumbaiospfelem = m_ospfElementDao.findByRouterId(MUMBAI_OSPF_ID);
        assertNotNull(mumbaiospfelem);
        printOspfElement(mumbaiospfelem);
        final List<OspfLink> mumbaiospflinks = m_ospfLinkDao.findByNodeId(mumbai.getId());
        printOspfTopology(mumbaiospflinks);
        assertEquals(4,mumbaiospflinks.size());
        Thread.sleep(1000);
        
        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        OspfElement delhiospfelem = m_ospfElementDao.findByRouterId(DELHI_OSPF_ID);
        assertNotNull(delhiospfelem);
        printOspfElement(delhiospfelem);
        final List<OspfLink> delhiospflinks = m_ospfLinkDao.findByNodeId(delhi.getId());
        printOspfTopology(delhiospflinks);
        assertEquals(3,delhiospflinks.size());
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
        OspfElement bangaloreospfelem = m_ospfElementDao.findByRouterId(BANGALORE_OSPF_ID);
        assertNotNull(bangaloreospfelem);
        printOspfElement(bangaloreospfelem);
        final List<OspfLink> bangaloreospflinks = m_ospfLinkDao.findByNodeId(bangalore.getId());
        printOspfTopology(bangaloreospflinks);
        assertEquals(4,bangaloreospflinks.size());
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(bagmane.getId()));
        OspfElement bagmaneospfelem = m_ospfElementDao.findByRouterId(BAGMANE_OSPF_ID);
        assertNotNull(bagmaneospfelem);
        printOspfElement(bagmaneospfelem);
        final List<OspfLink> bagmaneospflinks = m_ospfLinkDao.findByNodeId(bagmane.getId());
        printOspfTopology(bagmaneospflinks);
        assertEquals(4,bagmaneospflinks.size());
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(mysore.getId()));
        OspfElement mysoreospfelem = m_ospfElementDao.findByRouterId(MYSORE_OSPF_ID);
        assertNotNull(mysoreospfelem);
        printOspfElement(mysoreospfelem);
        final List<OspfLink> mysoreosplinks = m_ospfLinkDao.findByNodeId(mysore.getId());
        printOspfTopology(mysoreosplinks);
        assertEquals(2,mysoreosplinks.size());
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        OspfElement spaceexsw1ospfelem = m_ospfElementDao.findByRouterId(SPACE_EX_SW1_OSPF_ID);
        assertNotNull(spaceexsw1ospfelem);
        printOspfElement(spaceexsw1ospfelem);
        final List<OspfLink> spaceexsw1ospflinks = m_ospfLinkDao.findByNodeId(spaceexsw1.getId());
        printOspfTopology(spaceexsw1ospflinks);
        assertEquals(2,spaceexsw1ospflinks.size());
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
        OspfElement spaceexsw2ospfelem = m_ospfElementDao.findByRouterId(SPACE_EX_SW2_OSPF_ID);
        assertNotNull(spaceexsw2ospfelem);
        printOspfElement(spaceexsw2ospfelem);
        final List<OspfLink> spaceexsw2ospflinks = m_ospfLinkDao.findByNodeId(spaceexsw2.getId());
        printOspfTopology(spaceexsw2ospflinks);
        assertEquals(2,spaceexsw2ospflinks.size());
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(j635042.getId()));
        OspfElement j635042ospfelem = m_ospfElementDao.findByRouterId(J6350_42_OSPF_ID);
        assertNotNull(j635042ospfelem);
        printOspfElement(j635042ospfelem);
        final List<OspfLink> j635042ospflinks = m_ospfLinkDao.findByNodeId(j635042.getId());
        printOspfTopology(j635042ospflinks);
        assertEquals(1,j635042ospflinks.size());
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(srx100.getId()));
        assertNull(m_ospfElementDao.findByNodeId(srx100.getId()));
        assertEquals(0,m_ospfLinkDao.findByNodeId(srx100.getId()).size());

        assertEquals(8,m_ospfElementDao.countAll());
        assertEquals(22,m_ospfLinkDao.countAll());

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.OSPF);
        m_linkd.runTopologyUpdater(ProtocolSupported.OSPF);

        OspfOnmsTopologyUpdater topologyUpdater = m_linkd.getOspfTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();
        assertEquals(8,topology.getVertices().size());
        assertNotNull(topology);
        printOnmsTopology(topology);
        assertEquals(8,topology.getVertices().size());
        assertEquals(11,topology.getEdges().size());
        for (OnmsTopologyEdge edge: topology.getEdges()) {
            switch (edge.getSource().getVertex().getLabel()) {
                case MUMBAI_NAME:
                    switch (edge.getTarget().getVertex().getLabel()) {
                        case MYSORE_NAME:
                            assertEquals("ge-0/1/1.0", edge.getSource().getIfname());
                            assertEquals("192.168.5.21", edge.getSource().getAddr());
                            assertEquals(978, edge.getSource().getIfindex().intValue());
                            assertEquals("ge-0/0/1.0", edge.getTarget().getIfname());
                            assertEquals("192.168.5.22", edge.getTarget().getAddr());
                            assertEquals(508, edge.getTarget().getIfindex().intValue());
                            break;
                        case BAGMANE_NAME:
                            assertEquals("ge-0/0/2.0", edge.getSource().getIfname());
                            assertEquals("192.168.5.17", edge.getSource().getAddr());
                            assertEquals(977, edge.getSource().getIfindex().intValue());
                            assertEquals("ge-1/0/0.0", edge.getTarget().getIfname());
                            assertEquals("192.168.5.18", edge.getTarget().getAddr());
                            assertEquals(534, edge.getTarget().getIfindex().intValue());
                            break;
                        case DELHI_NAME:
                            assertEquals("ge-0/1/2.0", edge.getSource().getIfname());
                            assertEquals("192.168.5.9", edge.getSource().getAddr());
                            assertEquals(519, edge.getSource().getIfindex().intValue());
                            assertEquals("ge-1/0/2.0", edge.getTarget().getIfname());
                            assertEquals("192.168.5.10", edge.getTarget().getAddr());
                            assertEquals(28503, edge.getTarget().getIfindex().intValue());
                            break;
                        case BANGALORE_NAME:
                            assertEquals("ge-0/0/1.0", edge.getSource().getIfname());
                            assertEquals("192.168.5.13", edge.getSource().getAddr());
                            assertEquals(507, edge.getSource().getIfindex().intValue());
                            assertEquals("ge-0/0/0.0", edge.getTarget().getIfname());
                            assertEquals("192.168.5.14", edge.getTarget().getAddr());
                            assertEquals(2401, edge.getTarget().getIfindex().intValue());
                            break;
                        default:
                            fail();
                            break;
                    }
                    break;
                case BANGALORE_NAME:
                    if (edge.getTarget().getVertex().getLabel().equals(BAGMANE_NAME)) {
                        assertEquals("ge-0/1/0.0",edge.getSource().getIfname());
                        assertEquals("192.168.1.9",edge.getSource().getAddr());
                        assertEquals(2396,edge.getSource().getIfindex().intValue());
                        assertEquals("ge-1/0/4.0",edge.getTarget().getIfname());
                        assertEquals("192.168.1.10",edge.getTarget().getAddr());
                        assertEquals(1732,edge.getTarget().getIfindex().intValue());
                    } else  if (edge.getTarget().getVertex().getLabel().equals(SPACE_EX_SW2_NAME)) {
                        assertEquals("ge-0/0/3.0",edge.getSource().getIfname());
                        assertEquals("172.16.9.1",edge.getSource().getAddr());
                        assertEquals(2398,edge.getSource().getIfindex().intValue());
                        assertEquals("ge-0/0/3.0",edge.getTarget().getIfname());
                        assertEquals("172.16.9.2",edge.getTarget().getAddr());
                        assertEquals(551,edge.getTarget().getIfindex().intValue());
                    } else {
                        fail();
                    }
                    break;
                case SPACE_EX_SW1_NAME:
                    if (edge.getTarget().getVertex().getLabel().equals(SPACE_EX_SW2_NAME)) {
                        assertEquals("ge-0/0/0.0",edge.getSource().getIfname());
                        assertEquals("172.16.10.1",edge.getSource().getAddr());
                        assertEquals(1361,edge.getSource().getIfindex().intValue());
                        assertEquals("ge-0/0/0.0",edge.getTarget().getIfname());
                        assertEquals("172.16.10.2",edge.getTarget().getAddr());
                        assertEquals(531,edge.getTarget().getIfindex().intValue());
                    } else {
                        fail();
                    }
                    break;
                case BAGMANE_NAME:
                    if (edge.getTarget().getVertex().getLabel().equals(MYSORE_NAME)) {
                        assertEquals("ge-1/0/5.0",edge.getSource().getIfname());
                        assertEquals("192.168.1.13",edge.getSource().getAddr());
                        assertEquals(654,edge.getSource().getIfindex().intValue());
                        assertEquals("ge-0/1/1.0",edge.getTarget().getIfname());
                        assertEquals("192.168.1.14",edge.getTarget().getAddr());
                        assertEquals(520,edge.getTarget().getIfindex().intValue());
                    } else  if (edge.getTarget().getVertex().getLabel().equals(J6350_42_NAME)) {
                        assertEquals("ge-1/0/2.0",edge.getSource().getIfname());
                        assertEquals("172.16.20.1",edge.getSource().getAddr());
                        assertEquals(540,edge.getSource().getIfindex().intValue());
                        assertEquals("ge-0/0/2.0",edge.getTarget().getIfname());
                        assertEquals("172.16.20.2",edge.getTarget().getAddr());
                        assertEquals(549,edge.getTarget().getIfindex().intValue());
                    } else {
                        fail();
                    }
                    break;
                case DELHI_NAME:
                    if (edge.getTarget().getVertex().getLabel().equals(SPACE_EX_SW1_NAME)) {
                        assertEquals("ge-1/1/6.0",edge.getSource().getIfname());
                        assertEquals("172.16.7.1",edge.getSource().getAddr());
                        assertEquals(17619,edge.getSource().getIfindex().intValue());
                        assertEquals("ge-0/0/6.0",edge.getTarget().getIfname());
                        assertEquals("172.16.7.2",edge.getTarget().getAddr());
                        assertEquals(528,edge.getTarget().getIfindex().intValue());
                    } else  if (edge.getTarget().getVertex().getLabel().equals(BANGALORE_NAME)) {
                        assertEquals("ge-1/0/1.0",edge.getSource().getIfname());
                        assertEquals("192.168.1.5",edge.getSource().getAddr());
                        assertEquals(3674,edge.getSource().getIfindex().intValue());
                        assertEquals("ge-0/0/1.0",edge.getTarget().getIfname());
                        assertEquals("192.168.1.6",edge.getTarget().getAddr());
                        assertEquals(2397,edge.getTarget().getIfindex().intValue());
                    } else {
                        fail();
                    }
                    break;
                default:
                    fail();
            }
        }


    }
}
