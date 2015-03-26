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
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BAGMANE_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BAGMANE_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BAGMANE_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BANGALORE_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BANGALORE_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.BANGALORE_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.DELHI_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_42_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_42_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.J6350_42_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MUMBAI_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MYSORE_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MYSORE_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.MYSORE_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW1_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW1_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW1_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW2_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW2_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SPACE_EX_SW2_SNMP_RESOURCE_B;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SRX_100_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SRX_100_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SRX_100_SNMP_RESOURCE_B;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.nb.Nms10205bNetworkBuilder;

public class Nms10205bEnTest extends EnLinkdTestBuilder {

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
    public void testNetwork10205bLldpLinks() throws Exception {
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
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(!m_linkdConfig.useOspfDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());
        
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
        assertEquals(0,m_lldpLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        assertEquals(2,m_lldpLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
        assertEquals(2,m_lldpLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(bagmane.getId()));
        assertEquals(5,m_lldpLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(mysore.getId()));
        assertEquals(5,m_lldpLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        assertEquals(8,m_lldpLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
        assertEquals(10,m_lldpLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(j635042.getId()));
        assertEquals(10,m_lldpLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(srx100.getId()));
        assertEquals(10,m_lldpLinkDao.countAll());
     
        final List<LldpLink> topologyC = m_lldpLinkDao.findAll();
        printLldpTopology(topologyC);
        assertEquals(10,topologyC.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getLldpElement() != null)
        		printLldpElement(node.getLldpElement());
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

        assertTrue(!m_linkdConfig.useLldpDiscovery());
        assertTrue(!m_linkdConfig.useCdpDiscovery());
        assertTrue(m_linkdConfig.useOspfDiscovery());
        assertTrue(!m_linkdConfig.useBridgeDiscovery());
        assertTrue(!m_linkdConfig.useIsisDiscovery());

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
        final List<OspfLink> topologyA = m_ospfLinkDao.findAll();
        printOspfTopology(topologyA);
        assertEquals(4,topologyA.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getOspfElement() != null)
        		printOspfElement(node.getOspfElement());
        }
        
        Thread.sleep(1000);
        
        assertTrue(m_linkd.runSingleSnmpCollection(delhi.getId()));
        final List<OspfLink> topologyB = m_ospfLinkDao.findAll();
        printOspfTopology(topologyB);
        assertEquals(7,topologyB.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getOspfElement() != null)
        		printOspfElement(node.getOspfElement());
        }
        
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(bangalore.getId()));
        final List<OspfLink> topologyC = m_ospfLinkDao.findAll();
        printOspfTopology(topologyC);
        assertEquals(11,topologyC.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getOspfElement() != null)
        		printOspfElement(node.getOspfElement());
        }
        
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(bagmane.getId()));
        final List<OspfLink> topologyD = m_ospfLinkDao.findAll();
        printOspfTopology(topologyD);
        assertEquals(15,topologyD.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getOspfElement() != null)
        		printOspfElement(node.getOspfElement());
        }
        
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(mysore.getId()));
        final List<OspfLink> topologyE = m_ospfLinkDao.findAll();
        printOspfTopology(topologyE);
        assertEquals(17,topologyE.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getOspfElement() != null)
        		printOspfElement(node.getOspfElement());
        }
        
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw1.getId()));
        final List<OspfLink> topologyF = m_ospfLinkDao.findAll();
        printOspfTopology(topologyF);
        assertEquals(19,topologyF.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getOspfElement() != null)
        		printOspfElement(node.getOspfElement());
        }
        
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(spaceexsw2.getId()));
        final List<OspfLink> topologyG = m_ospfLinkDao.findAll();
        printOspfTopology(topologyG);
        assertEquals(21,topologyG.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getOspfElement() != null)
        		printOspfElement(node.getOspfElement());
        }
        
        Thread.sleep(1000);

        assertTrue(m_linkd.runSingleSnmpCollection(j635042.getId()));
        final List<OspfLink> topologyH = m_ospfLinkDao.findAll();
        printOspfTopology(topologyH);
        assertEquals(22,topologyH.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getOspfElement() != null)
        		printOspfElement(node.getOspfElement());
        }
        assertTrue(m_linkd.runSingleSnmpCollection(srx100.getId()));
        
        final List<OspfLink> topologyI = m_ospfLinkDao.findAll();
        printOspfTopology(topologyI);
        assertEquals(22,topologyI.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getOspfElement() != null)
        		printOspfElement(node.getOspfElement());
        }

    }
}
