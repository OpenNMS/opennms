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

package org.opennms.netmgt.linkd;

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
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH1_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH1_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH2_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH2_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH2_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH3_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH3_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH3_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH4_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH4_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH4_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH5_IP;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH5_NAME;
import static org.opennms.netmgt.nb.TestNetworkBuilder.SWITCH5_SNMP_RESOURCE;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.DataLinkInterface.DiscoveryProtocol;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms17216NetworkBuilder;

public class Nms17216Test extends LinkdTestBuilder {

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
     * Here we add cdp discovery and all test lab devices
     * To the previuos links discovered by lldp
     * should be added the followings discovered with cdp:
     * switch3 Fa0/23 Fa0/24 ---> switch5 Fa0/1 Fa0/9
     * router1 Fa0/0 ----> switch1 Gi0/1
     * router2 Serial0/0/0 ----> router1 Serial0/0/0
     * router3 Serial0/0/1 ----> router2 Serial0/0/1
     * router4 GigabitEthernet0/1 ----> router3   GigabitEthernet0/0
     * switch4 FastEthernet0/1    ----> router3   GigabitEthernet0/1
     * 
     * here are the corresponding ifindex:
     * switch1 Gi0/1 -->  10101
     * 
     * switch3 Fa0/23 -->  10023
     * switch3 Fa0/24 -->  10024
     *
     * switch5 Fa0/1 -->  10001
     * switch5 Fa0/13 -->  10013
     * 
     * router1 Fa0/0 -->  7
     * router1 Serial0/0/0 --> 13
     * router1 Serial0/0/1 --> 14
     * 
     * router2 Serial0/0/0 --> 12
     * router2 Serial0/0/1 --> 13
     * 
     * router3 Serial0/0/1 --> 13
     * router3 GigabitEthernet0/0 --> 8
     * router3 GigabitEthernet0/1 --> 9
     * 
     * router4 GigabitEthernet0/1  --> 3
     * 
     * switch4 FastEthernet0/1 --> 10001
     * 
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource=SWITCH2_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource=SWITCH3_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource=SWITCH4_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource=SWITCH5_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=ROUTER1_IP, port=161, resource=ROUTER1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=ROUTER2_IP, port=161, resource=ROUTER2_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=ROUTER3_IP, port=161, resource=ROUTER3_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=ROUTER4_IP, port=161, resource=ROUTER4_SNMP_RESOURCE)
    })
    public void testNetwork17216Links() throws Exception {
        
        m_nodeDao.save(builder.getSwitch1());
        m_nodeDao.save(builder.getSwitch2());
        m_nodeDao.save(builder.getSwitch3());
        m_nodeDao.save(builder.getSwitch4());
        m_nodeDao.save(builder.getSwitch5());
        m_nodeDao.save(builder.getRouter1());
        m_nodeDao.save(builder.getRouter2());
        m_nodeDao.save(builder.getRouter3());
        m_nodeDao.save(builder.getRouter4());

        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setUseBridgeDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setEnableVlanDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIsisDiscovery(false);

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);
        final OnmsNode switch4 = m_nodeDao.findByForeignId("linkd", SWITCH4_NAME);
        final OnmsNode switch5 = m_nodeDao.findByForeignId("linkd", SWITCH5_NAME);
        final OnmsNode router1 = m_nodeDao.findByForeignId("linkd", ROUTER1_NAME);
        final OnmsNode router2 = m_nodeDao.findByForeignId("linkd", ROUTER2_NAME);
        final OnmsNode router3 = m_nodeDao.findByForeignId("linkd", ROUTER3_NAME);
        final OnmsNode router4 = m_nodeDao.findByForeignId("linkd", ROUTER4_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch3.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch4.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch5.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router3.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router4.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch4.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch5.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router3.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router4.getId()));
       
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        final Collection<LinkableNode> nodes = m_linkd.getLinkableNodesOnPackage("example1");

        assertEquals(9, nodes.size());
        
        for (LinkableNode node: nodes) {
            switch(node.getNodeId()) {
                case 1: assertEquals(5, node.getCdpInterfaces().size());
                assertEquals(SWITCH1_NAME, node.getCdpDeviceId());
                break;
                case 2: assertEquals(6, node.getCdpInterfaces().size());
                assertEquals(SWITCH2_NAME, node.getCdpDeviceId());
                break;
                case 3: assertEquals(4, node.getCdpInterfaces().size());
                assertEquals(SWITCH3_NAME, node.getCdpDeviceId());
                break;
                case 4: assertEquals(1, node.getCdpInterfaces().size());
                assertEquals(SWITCH4_NAME, node.getCdpDeviceId());
                break;
                case 5: assertEquals(2, node.getCdpInterfaces().size());
                assertEquals(SWITCH5_NAME, node.getCdpDeviceId());
                break;
                case 6: assertEquals(2, node.getCdpInterfaces().size());
                assertEquals(ROUTER1_NAME, node.getCdpDeviceId());
                break;
                case 7: assertEquals(2, node.getCdpInterfaces().size());
                assertEquals(ROUTER2_NAME, node.getCdpDeviceId());
                break;
                case 8: assertEquals(3, node.getCdpInterfaces().size());
                assertEquals(ROUTER3_NAME, node.getCdpDeviceId());
                break;
                case 9: assertEquals(1, node.getCdpInterfaces().size());
                assertEquals(ROUTER4_NAME, node.getCdpDeviceId());
                break;
                default: assertEquals(-1, node.getNodeId());
                break;
            }
        }        
        
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(19,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> datalinkinterfaces = m_dataLinkInterfaceDao.findAll();

        int start=getStartPoint(datalinkinterfaces);

        for (final DataLinkInterface datalinkinterface: datalinkinterfaces) {
            Integer linkid = datalinkinterface.getId();
            if ( linkid == start) {
                // switch1 gi0/9 -> switch2 gi0/1 --lldp --cdp
                checkLink(switch2, switch1, 10101, 10109, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+11) {
                checkLink(switch2, switch1, 10101, 10109, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+1 ) {
                // switch1 gi0/10 -> switch2 gi0/2 --lldp --cdp
                checkLink(switch2, switch1, 10102, 10110, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+12 ) {
                // switch1 gi0/10 -> switch2 gi0/2 --lldp --cdp
                checkLink(switch2, switch1, 10102, 10110, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+2) {
                // switch1 gi0/11 -> switch2 gi0/3 --lldp --cdp
                checkLink(switch2, switch1, 10103, 10111, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+13) {
                // switch1 gi0/11 -> switch2 gi0/3 --lldp --cdp
                checkLink(switch2, switch1, 10103, 10111, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+3) {
                // switch1 gi0/12 -> switch2 gi0/4 --lldp --cdp
                checkLink(switch2, switch1, 10104, 10112, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+14) {
                // switch1 gi0/12 -> switch2 gi0/4 --lldp --cdp
                checkLink(switch2, switch1, 10104, 10112, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+4) {
                // switch2 gi0/19 -> switch3 Fa0/19 --lldp --cdp
                checkLink(switch3, switch2, 10019, 10119, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+15) {
                // switch2 gi0/19 -> switch3 Fa0/19 --lldp --cdp
                checkLink(switch3, switch2, 10019, 10119, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+5) {
                // switch2 gi0/20 -> switch3 Fa0/20 --lldp --cdp
                checkLink(switch3, switch2, 10020, 10120, datalinkinterface);
                assertEquals(DiscoveryProtocol.lldp, datalinkinterface.getProtocol());
            } else if (linkid == start+16) {
                // switch2 gi0/20 -> switch3 Fa0/20 --lldp --cdp
                checkLink(switch3, switch2, 10020, 10120, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+6) {
                checkLink(router4, router3, 3, 8, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+7) {
                checkLink(router2, router1, 12, 13, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+8) {
                checkLink(router3, router2, 13, 13, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+9) {
                //switch4 FastEthernet0/1    ----> router3   GigabitEthernet0/1
                checkLink(router3, switch4, 9, 10001, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+10) {
                // switch1 gi0/1 -> router1 Fa0/20 --cdp
                checkLink(router1, switch1, 7, 10101, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+17) {
                // switch3 Fa0/1 -> switch5 Fa0/23 --cdp
                checkLink(switch5, switch3, 10001, 10023, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else if (linkid == start+18) {
                // switch3 gi0/1 -> switch5 Fa0/20 --cdp
                checkLink(switch5, switch3, 10013, 10024, datalinkinterface);
                assertEquals(DiscoveryProtocol.cdp, datalinkinterface.getProtocol());
            } else {
                // error
                checkLink(switch1,switch1,-1,-1,datalinkinterface);
            }      
        }
    }

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

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setUseBridgeDiscovery(false);
        example1.setUseCdpDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setEnableVlanDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIsisDiscovery(false);

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch1.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch2.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(switch3.getId()));
 
        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
               
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(6,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> links = m_dataLinkInterfaceDao.findAll();

        int startid = getStartPoint(links);
        for (final DataLinkInterface link: links) {
//            printLink(datalinkinterface);
            Integer linkid = link.getId();
            if ( linkid == startid) {
                // switch1 gi0/9 -> switch2 gi0/1 --lldp
                checkLink(switch2, switch1, 10101, 10109, link);
            } else if (linkid == startid +1 ) {
                // switch1 gi0/10 -> switch2 gi0/2 --lldp
                checkLink(switch2, switch1, 10102, 10110, link);
            } else if (linkid == startid+2) {
                // switch1 gi0/11 -> switch2 gi0/3 --lldp
                checkLink(switch2, switch1, 10103, 10111, link);
            } else if (linkid == startid+3) {
                // switch1 gi0/12 -> switch2 gi0/4 --lldp
                checkLink(switch2, switch1, 10104, 10112, link);
            } else if (linkid == startid+4) {
                // switch2 gi0/19 -> switch3 Fa0/19 --lldp
                checkLink(switch3, switch2, 10019, 10119, link);
            } else if (linkid == startid+5) {
                // switch2 gi0/20 -> switch3 Fa0/20 --lldp
                checkLink(switch3, switch2, 10020, 10120, link);
            } else {
                // error
                checkLink(switch1,switch1,-1,-1,link);
            }   
        }
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource=SWITCH4_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=ROUTER3_IP, port=161, resource=ROUTER3_SNMP_RESOURCE)
    })
    public void testNetwork17216Switch4Router4CdpLinks() throws Exception {
        
        m_nodeDao.save(builder.getSwitch4());
        m_nodeDao.save(builder.getRouter3());

        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        assertEquals(false, example1.hasForceIpRouteDiscoveryOnEthernet());
        example1.setUseLldpDiscovery(false);
        example1.setUseBridgeDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setUseCdpDiscovery(true);
        example1.setEnableVlanDiscovery(false);
        example1.setSaveRouteTable(false);
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveStpNodeTable(false);
        example1.setUseIsisDiscovery(false);

        
        final OnmsNode switch4 = m_nodeDao.findByForeignId("linkd", SWITCH4_NAME);
        final OnmsNode router3 = m_nodeDao.findByForeignId("linkd", ROUTER3_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(switch4.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(router3.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(switch4.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(router3.getId()));
       
        assertEquals(0,m_dataLinkInterfaceDao.countAll());


        
        final Collection<LinkableNode> nodes = m_linkd.getLinkableNodesOnPackage("example1");

        assertEquals(2, nodes.size());
        
        for (LinkableNode node: nodes) {
            assertEquals(1, node.getCdpInterfaces().size());
        }
        
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(1,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> datalinkinterfaces = m_dataLinkInterfaceDao.findAll();
                
        for (final DataLinkInterface datalinkinterface: datalinkinterfaces) {

                checkLink(router3, switch4, 9, 10001, datalinkinterface);
        }
    }

}
