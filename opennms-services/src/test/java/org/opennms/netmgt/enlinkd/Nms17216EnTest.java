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
/*
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER1_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER1_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER2_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER2_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER2_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER3_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER3_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER3_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER4_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER4_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.ROUTER4_SNMP_RESOURCE;
*/
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH1_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH1_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH2_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH2_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH2_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH3_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH3_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH3_SNMP_RESOURCE;

/*
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH4_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH4_LLDP_CHASSISID;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH4_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH4_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH5_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH5_LLDP_CHASSISID;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH5_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH5_SNMP_RESOURCE;
*/
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms17216NetworkBuilder;

public class Nms17216EnTest extends EnLinkdTestBuilder {
        
	Nms17216NetworkBuilder builder = new Nms17216NetworkBuilder();    
    /*
     * These are the links among the following nodes discovered using 
     * only the lldp protocol
     * switch1 Gi0/9 Gi0/10 Gi0/11 Gi0/12 ----> switch2 Gi0/1 Gi0/2 Gi0/3 Gi0/4
     * switch2 Gi0/19 Gi0/20              ----> switch3 Fa0/19 Fa0/20
     * 
     * here are the corresponding ifindex:
     * switch1 Gi0/9 --> 10109
     * switch1 Gi0/10 --> 10110
     * switch1 Gi0/11 --> 10111
     * switch1 Gi0/12 --> 10112
     * 
     * switch2 Gi0/1 --> 10101
     * switch2 Gi0/2 --> 10102
     * switch2 Gi0/3 --> 10103
     * switch2 Gi0/4 --> 10104
     * switch2 Gi0/19 --> 10119
     * switch2 Gi0/20 --> 10120
     * 
     * switch3 Fa0/19 -->  10019
     * switch3 Fa0/20 -->  10020
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource=SWITCH2_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource=SWITCH3_SNMP_RESOURCE)
    })
    public void testNetwork17216LldpLinks() throws Exception {
        m_nodeDao.save(builder.getSwitch1());
        m_nodeDao.save(builder.getSwitch2());
        m_nodeDao.save(builder.getSwitch3());
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

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch3.getId()));
 
        assertEquals(0,m_lldpLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        final List<LldpLink> topologyA = m_lldpLinkDao.findAll();
        printLldpTopology(topologyA);
        assertEquals(4,topologyA.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getLldpElement() != null)
        		printLldpElement(node.getLldpElement());
        }
        
        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        final List<LldpLink> topologyB = m_lldpLinkDao.findAll();
        printLldpTopology(topologyB);
        assertEquals(10,topologyB.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getLldpElement() != null)
        		printLldpElement(node.getLldpElement());
        }
       
        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
        final List<LldpLink> topologyC = m_lldpLinkDao.findAll();
        printLldpTopology(topologyC);
        assertEquals(12,topologyC.size());
        for (final OnmsNode node: m_nodeDao.findAll()) {
        	if (node.getLldpElement() != null)
        		printLldpElement(node.getLldpElement());
        }

    }
}
