/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER1_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER1_IP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER1_IP_IF_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER1_NAME;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER2_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER2_IP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER2_IP_IF_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER2_NAME;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER2_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER3_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER3_IP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER3_IP_IF_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER3_NAME;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER3_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER4_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER4_IP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER4_IP_IF_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER4_NAME;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.ROUTER4_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH1_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH1_IF_IFNAME_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH1_IP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH1_IP_IF_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH1_LLDP_CHASSISID;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH1_NAME;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH1_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH2_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH2_IF_IFNAME_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH2_IP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH2_LLDP_CHASSISID;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH2_NAME;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH2_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH3_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH3_IF_IFNAME_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH3_IP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH3_LLDP_CHASSISID;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH3_NAME;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH3_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH4_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH4_IP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH4_LLDP_CHASSISID;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH4_NAME;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH4_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH5_IF_IFDESCR_MAP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH5_IP;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH5_LLDP_CHASSISID;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH5_NAME;
import static org.opennms.netmgt.nb.Nms17216NetworkBuilder.SWITCH5_SNMP_RESOURCE;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.OspfElement.TruthValue;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms17216NetworkBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage.TopologyMessageStatus;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.opennms.netmgt.topologies.service.impl.OnmsTopologyLogger;

public class Nms17216EnIT extends EnLinkdBuilderITCase {
        
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
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource=SWITCH3_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH4_IP, port=161, resource=SWITCH4_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH5_IP, port=161, resource=SWITCH5_SNMP_RESOURCE)
    })
    public void testNetwork17216LldpLinks() throws Exception {
        m_nodeDao.save(builder.getSwitch1());
        m_nodeDao.save(builder.getSwitch2());
        m_nodeDao.save(builder.getSwitch3());
        m_nodeDao.save(builder.getSwitch4());
        m_nodeDao.save(builder.getSwitch5());
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

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);
        final OnmsNode switch4 = m_nodeDao.findByForeignId("linkd", SWITCH4_NAME);
        final OnmsNode switch5 = m_nodeDao.findByForeignId("linkd", SWITCH5_NAME);

        m_linkd.reload();
 
        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertEquals(4, m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        
        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertEquals(10,m_lldpLinkDao.countAll());
        assertEquals(2,m_lldpElementDao.countAll());

        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
        assertEquals(12,m_lldpLinkDao.countAll());
        assertEquals(3,m_lldpElementDao.countAll());

        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch4.getId()));
        assertEquals(12,m_lldpLinkDao.countAll());
        assertEquals(4,m_lldpElementDao.countAll());

        Thread.sleep(1000);
        assertTrue(m_linkd.runSingleSnmpCollection(switch5.getId()));
        assertEquals(12,m_lldpLinkDao.countAll());
        assertEquals(5,m_lldpElementDao.countAll());

        for (final LldpElement node: m_lldpElementDao.findAll()) {
            assertNotNull(node);
            printLldpElement(node);
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, node.getLldpChassisIdSubType());
            if (node.getNode().getId().intValue() == switch1.getId().intValue()) {
                assertEquals(SWITCH1_LLDP_CHASSISID, node.getLldpChassisId());
                assertEquals(SWITCH1_NAME, node.getLldpSysname());
            } else if (node.getNode().getId().intValue() == switch2.getId().intValue()) {
                assertEquals(SWITCH2_LLDP_CHASSISID, node.getLldpChassisId());
                assertEquals(SWITCH2_NAME, node.getLldpSysname());                
            } else if (node.getNode().getId().intValue() == switch3.getId().intValue()) {
                assertEquals(SWITCH3_LLDP_CHASSISID, node.getLldpChassisId());
                assertEquals(SWITCH3_NAME, node.getLldpSysname());
            } else if (node.getNode().getId().intValue() == switch4.getId().intValue()) {
                assertEquals(SWITCH4_LLDP_CHASSISID, node.getLldpChassisId());
                assertEquals(SWITCH4_NAME, node.getLldpSysname());
            } else if (node.getNode().getId().intValue() == switch5.getId().intValue()) {
                assertEquals(SWITCH5_LLDP_CHASSISID, node.getLldpChassisId());
                assertEquals(SWITCH5_NAME, node.getLldpSysname());
            } else {
                fail();
            }
        }
        
        for (LldpLink link: m_lldpLinkDao.findAll()) {
            printLldpLink(link);
            assertEquals(LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpPortIdSubType());
            assertEquals(LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpRemPortIdSubType());
            assertNotNull(link.getLldpPortIfindex());
            OnmsNode node = m_nodeDao.get(link.getNode().getId());
            switch (node.getLabel()) {
                case SWITCH1_NAME:
                    assertEquals(SWITCH2_LLDP_CHASSISID, link.getLldpRemChassisId());
                    assertEquals(SWITCH2_NAME,link.getLldpRemSysname());
                    switch (link.getLldpRemIndex()) {
                        case 4:
                            assertEquals(10109,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH1_IF_IFNAME_MAP.get(10109), link.getLldpPortId());
                            assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10109), link.getLldpPortDescr());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10101), link.getLldpRemPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10101), link.getLldpRemPortDescr());
                            break;
                        case 3:
                            assertEquals(10110,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH1_IF_IFNAME_MAP.get(10110), link.getLldpPortId());
                            assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10110), link.getLldpPortDescr());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10102), link.getLldpRemPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10102), link.getLldpRemPortDescr());
                            break;
                        case 1:
                            assertEquals(10111,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH1_IF_IFNAME_MAP.get(10111), link.getLldpPortId());
                            assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10111), link.getLldpPortDescr());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10103), link.getLldpRemPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10103), link.getLldpRemPortDescr());
                            break;
                        case 2:
                            assertEquals(10112,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH1_IF_IFNAME_MAP.get(10112), link.getLldpPortId());
                            assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10112), link.getLldpPortDescr());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10104), link.getLldpRemPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10104), link.getLldpRemPortDescr());
                            break;
                        default:
                            fail();
                            break;
                    }
                    break;
                case SWITCH2_NAME:
                    switch (link.getLldpRemIndex()) {
                        case 4:
                            assertEquals(SWITCH1_LLDP_CHASSISID, link.getLldpRemChassisId());
                            assertEquals(SWITCH1_NAME,link.getLldpRemSysname());
                            assertEquals(10101,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10101), link.getLldpPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10101), link.getLldpPortDescr());
                            assertEquals(SWITCH1_IF_IFNAME_MAP.get(10109), link.getLldpRemPortId());
                            assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10109), link.getLldpRemPortDescr());
                            break;
                        case 3:
                            assertEquals(SWITCH1_LLDP_CHASSISID, link.getLldpRemChassisId());
                            assertEquals(SWITCH1_NAME,link.getLldpRemSysname());
                            assertEquals(10102,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10102), link.getLldpPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10102), link.getLldpPortDescr());
                            assertEquals(SWITCH1_IF_IFNAME_MAP.get(10110), link.getLldpRemPortId());
                            assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10110), link.getLldpRemPortDescr());
                            break;
                        case 5:
                            assertEquals(SWITCH1_LLDP_CHASSISID, link.getLldpRemChassisId());
                            assertEquals(SWITCH1_NAME,link.getLldpRemSysname());
                            assertEquals(10103,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10103), link.getLldpPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10103), link.getLldpPortDescr());
                            assertEquals(SWITCH1_IF_IFNAME_MAP.get(10111), link.getLldpRemPortId());
                            assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10111), link.getLldpRemPortDescr());
                            break;
                        case 6:
                            assertEquals(SWITCH1_LLDP_CHASSISID, link.getLldpRemChassisId());
                            assertEquals(SWITCH1_NAME,link.getLldpRemSysname());
                            assertEquals(10104,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10104), link.getLldpPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10104), link.getLldpPortDescr());
                            assertEquals(SWITCH1_IF_IFNAME_MAP.get(10112), link.getLldpRemPortId());
                            assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10112), link.getLldpRemPortDescr());
                            break;
                        case 1:
                            assertEquals(SWITCH3_LLDP_CHASSISID, link.getLldpRemChassisId());
                            assertEquals(SWITCH3_NAME,link.getLldpRemSysname());
                            assertEquals(10119,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10119), link.getLldpPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10119), link.getLldpPortDescr());
                            assertEquals(SWITCH3_IF_IFNAME_MAP.get(10019), link.getLldpRemPortId());
                            assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10019), link.getLldpRemPortDescr());
                            break;
                        case 2:
                            assertEquals(SWITCH3_LLDP_CHASSISID, link.getLldpRemChassisId());
                            assertEquals(SWITCH3_NAME,link.getLldpRemSysname());
                            assertEquals(10120,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10120), link.getLldpPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10120), link.getLldpPortDescr());
                            assertEquals(SWITCH3_IF_IFNAME_MAP.get(10020), link.getLldpRemPortId());
                            assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10020), link.getLldpRemPortDescr());
                            break;
                        default:
                            fail();
                            break;
                    }
                    break;
                case SWITCH3_NAME:
                    assertEquals(SWITCH2_LLDP_CHASSISID, link.getLldpRemChassisId());
                    assertEquals(SWITCH2_NAME,link.getLldpRemSysname());
                    switch (link.getLldpRemIndex()) {
                        case 1:
                            assertEquals(10019,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH3_IF_IFNAME_MAP.get(10019), link.getLldpPortId());
                            assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10019), link.getLldpPortDescr());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10119), link.getLldpRemPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10119), link.getLldpRemPortDescr());
                            break;
                        case 2:
                            assertEquals(10020,link.getLldpPortIfindex().intValue());
                            assertEquals(SWITCH3_IF_IFNAME_MAP.get(10020), link.getLldpPortId());
                            assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10020), link.getLldpPortDescr());
                            assertEquals(SWITCH2_IF_IFNAME_MAP.get(10120), link.getLldpRemPortId());
                            assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10120), link.getLldpRemPortDescr());
                            break;
                        default:
                            fail();
                            break;
                    }
                    break;
                default:
                    fail();
            }
        }

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater topologyUpdater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();

        assertNotNull(topology);
        printOnmsTopology(topology);
        assertEquals(5,topology.getVertices().size());
        assertEquals(6,topology.getEdges().size());

        for (OnmsTopologyEdge edge: topology.getEdges()) {
            switch (edge.getSource().getVertex().getLabel()) {
                case SWITCH2_NAME:
                    assertEquals(SWITCH3_NAME, edge.getTarget().getVertex().getLabel());
                    switch (edge.getTarget().getIfindex()) {
                        case 10019:
                            assertEquals(10119, edge.getSource().getIfindex().intValue());
                            assertEquals("Gi0/19", edge.getSource().getIfname());
                            assertEquals("Gi0/19 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getSource().getAddr());
                            assertEquals("Fa0/19", edge.getTarget().getIfname());
                            assertEquals("Fa0/19 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getTarget().getAddr());
                            break;
                        case 10020:
                            assertEquals(10120, edge.getSource().getIfindex().intValue());
                            assertEquals("Gi0/20", edge.getSource().getIfname());
                            assertEquals("Gi0/20 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getSource().getAddr());
                            assertEquals("Fa0/20", edge.getTarget().getIfname());
                            assertEquals("Fa0/20 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getTarget().getAddr());
                            break;
                        default:
                            fail();
                    }
                    break;
                case SWITCH1_NAME:
                    assertEquals(SWITCH2_NAME, edge.getTarget().getVertex().getLabel());
                    switch (edge.getTarget().getIfindex()) {
                        case 10103:
                            assertEquals("Gi0/11", edge.getSource().getIfname());
                            assertEquals("Gi0/11 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getSource().getAddr());
                            assertEquals(10111, edge.getSource().getIfindex().intValue());
                            assertEquals("Gi0/3", edge.getTarget().getIfname());
                            assertEquals("Gi0/3 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getTarget().getAddr());
                            break;
                        case 10101:
                            assertEquals("Gi0/9", edge.getSource().getIfname());
                            assertEquals("Gi0/9 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getSource().getAddr());
                            assertEquals(10109, edge.getSource().getIfindex().intValue());
                            assertEquals("Gi0/1", edge.getTarget().getIfname());
                            assertEquals("Gi0/1 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getTarget().getAddr());
                            break;
                        case 10102:
                            assertEquals("Gi0/10", edge.getSource().getIfname());
                            assertEquals("Gi0/10 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getSource().getAddr());
                            assertEquals(10110, edge.getSource().getIfindex().intValue());
                            assertEquals("Gi0/2", edge.getTarget().getIfname());
                            assertEquals("Gi0/2 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getTarget().getAddr());
                            break;
                        case 10104:
                            assertEquals("Gi0/12", edge.getSource().getIfname());
                            assertEquals("Gi0/12 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getSource().getAddr());
                            assertEquals(10112, edge.getSource().getIfindex().intValue());
                            assertEquals("Gi0/4", edge.getTarget().getIfname());
                            assertEquals("Gi0/4 type LLDP_PORTID_SUBTYPE_INTERFACENAME", edge.getTarget().getAddr());
                            break;
                        default:
                            fail();
                    }
                    break;
                default:
                    fail();
            }
        }

    }
    
    /* 
     * switch1 GigabitEthernet 0/9 0/10 0/11 0/12 ---> switch2 GigabitEthernet 0/1 0/2 0/3 0/4
     * switch1 GigabitEthernet0/1                 ---> router1 FastEthernet0/0
     * 
     * switch2 GigabitEthernet 0/1 0/2 0/3 0/4    ---> switch1 GigabitEthernet 0/9 0/10 0/11 0/12 
     * switch2 GigabitEthernet 0/19 Gi0/20        ---> switch3 FastEthernet 0/19 0/20
     *  
     * switch3 FastEthernet 0/19 0/20             ---> switch2 GigabitEthernet 0/19 0/20
     * switch3 FastEthernet 0/23 0/24             ---> switch5 FastEthernet 0/1 0/13
     *
     * switch4 FastEthernet0/1                    ---> router3 GigabitEthernet0/1
     * 
     * switch5 FastEthernet 0/1 0/13              ---> switch3 FastEthernet 0/23 0/24
     * 
     * router1 FastEthernet0/0                    ---> switch1 GigabitEthernet0/1
     * router1 Serial0/0/0                        ---> router2 Serial0/0/0
     *  
     * router2 Serial0/0/0                        ---> router1 Serial0/0/0
     * router2 Serial0/0/1                        ---> router3 Serial0/0/1
     * 
     * router3 GigabitEthernet0/0                 ---> router4 GigabitEthernet0/1
     * router3 GigabitEthernet0/1                 ---> switch4 FastEthernet0/1 
     * router3 Serial0/0/1                        ---> router2 Serial0/0/1
     * 
     * router4 GigabitEthernet0/1                 ---> router3   GigabitEthernet0/0
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
    public void testNetwork17216CdpLinks() {
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

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(true);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertFalse(m_linkdConfig.useLldpDiscovery());
        assertTrue(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);
        final OnmsNode switch4 = m_nodeDao.findByForeignId("linkd", SWITCH4_NAME);
        final OnmsNode switch5 = m_nodeDao.findByForeignId("linkd", SWITCH5_NAME);
        final OnmsNode router1 = m_nodeDao.findByForeignId("linkd", ROUTER1_NAME);
        final OnmsNode router2 = m_nodeDao.findByForeignId("linkd", ROUTER2_NAME);
        final OnmsNode router3 = m_nodeDao.findByForeignId("linkd", ROUTER3_NAME);
        final OnmsNode router4 = m_nodeDao.findByForeignId("linkd", ROUTER4_NAME);

        m_linkd.reload();

        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertEquals(5, m_cdpLinkDao.countAll());
        assertEquals(1, m_cdpElementDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertEquals(11, m_cdpLinkDao.countAll());
        assertEquals(2, m_cdpElementDao.countAll());
       
        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
        assertEquals(15, m_cdpLinkDao.countAll());
        assertEquals(3, m_cdpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(switch4.getId()));
        assertEquals(16, m_cdpLinkDao.countAll());
        assertEquals(4, m_cdpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(switch5.getId()));
        assertEquals(18, m_cdpLinkDao.countAll());
        assertEquals(5, m_cdpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(router1.getId()));
        assertEquals(20, m_cdpLinkDao.countAll());
        assertEquals(6, m_cdpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(router2.getId()));
        assertEquals(22, m_cdpLinkDao.countAll());
        assertEquals(7, m_cdpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(router3.getId()));
        assertEquals(25, m_cdpLinkDao.countAll());
        assertEquals(8, m_cdpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(router4.getId()));
        assertEquals(26, m_cdpLinkDao.countAll());
        assertEquals(9, m_cdpElementDao.countAll());

        for (final CdpElement node: m_cdpElementDao.findAll()) {
            assertNotNull(node);
            printCdpElement(node);
            assertEquals(TruthValue.TRUE, node.getCdpGlobalRun());
            if (node.getNode().getId().intValue() == switch1.getId().intValue()) {
                assertEquals(SWITCH1_NAME,node.getCdpGlobalDeviceId());
            } else if (node.getNode().getId().intValue() == switch2.getId().intValue()) {
                assertEquals(SWITCH2_NAME,node.getCdpGlobalDeviceId());
            } else if (node.getNode().getId().intValue() == switch3.getId().intValue()) {
                assertEquals(SWITCH3_NAME,node.getCdpGlobalDeviceId());
            } else if (node.getNode().getId().intValue() == switch4.getId().intValue()) {
                assertEquals(SWITCH4_NAME,node.getCdpGlobalDeviceId());
            } else if (node.getNode().getId().intValue() == switch5.getId().intValue()) {
                assertEquals(SWITCH5_NAME,node.getCdpGlobalDeviceId());
            } else if (node.getNode().getId().intValue() == router1.getId().intValue()) {
                assertEquals(ROUTER1_NAME,node.getCdpGlobalDeviceId());
            } else if (node.getNode().getId().intValue() == router2.getId().intValue()) {
                assertEquals(ROUTER2_NAME,node.getCdpGlobalDeviceId());
            } else if (node.getNode().getId().intValue() == router3.getId().intValue()) {
                assertEquals(ROUTER3_NAME,node.getCdpGlobalDeviceId());
            } else if (node.getNode().getId().intValue() == router4.getId().intValue()) {
                assertEquals(ROUTER4_NAME,node.getCdpGlobalDeviceId());
            } else {
                fail();
            }
        }
        
        for (CdpLink link: m_cdpLinkDao.findAll()) {
            printCdpLink(link);
            assertEquals(CiscoNetworkProtocolType.ip, link.getCdpCacheAddressType());
            if        (link.getNode().getId().intValue() == switch1.getId().intValue()) {
                if (link.getCdpCacheIfIndex() == 10101 && link.getCdpCacheDeviceIndex() == 1 ) {
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10101), link.getCdpInterfaceName());
                    assertEquals(ROUTER1_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, 2800 Software (C2800NM-ADVENTERPRISEK9-M), Version 12.4(24)T1, RELEASE SOFTWARE (fc3) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2009 by Cisco Systems, Inc. Compiled Fri 19-Jun-09 15:13 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(ROUTER1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("Cisco 2811",link.getCdpCacheDevicePlatform());
                    assertEquals(ROUTER1_IF_IFDESCR_MAP.get(7), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex() == 10109 && link.getCdpCacheDeviceIndex() == 5 ) {
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10109), link.getCdpInterfaceName());
                    assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10101), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex() == 10110 && link.getCdpCacheDeviceIndex() == 2 ) {
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10110), link.getCdpInterfaceName());
                    assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10102), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex() == 10111 && link.getCdpCacheDeviceIndex() == 3 ) {
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10111), link.getCdpInterfaceName());
                    assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10103), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex() == 10112 && link.getCdpCacheDeviceIndex() == 4 ) {
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10112), link.getCdpInterfaceName());
                    assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10104), link.getCdpCacheDevicePort());
                } else {
                    fail();
                }
            } else if (link.getNode().getId().intValue() == switch2.getId().intValue()) {
                if (link.getCdpCacheIfIndex() == 10101 && link.getCdpCacheDeviceIndex() == 3 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10101), link.getCdpInterfaceName());
                    assertEquals(SWITCH1_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C3560 Software (C3560-IPSERVICESK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:19 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C3560G-24PS",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10109), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex() == 10102 && link.getCdpCacheDeviceIndex() == 4 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10102), link.getCdpInterfaceName());
                    assertEquals(SWITCH1_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C3560 Software (C3560-IPSERVICESK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:19 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C3560G-24PS",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10110), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex() == 10103 && link.getCdpCacheDeviceIndex() == 5 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10103), link.getCdpInterfaceName());
                    assertEquals(SWITCH1_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C3560 Software (C3560-IPSERVICESK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:19 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C3560G-24PS",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10111), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex() == 10104 && link.getCdpCacheDeviceIndex() == 6 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10104), link.getCdpInterfaceName());
                    assertEquals(SWITCH1_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C3560 Software (C3560-IPSERVICESK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:19 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C3560G-24PS",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10112), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex() == 10119 && link.getCdpCacheDeviceIndex() == 1 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10119), link.getCdpInterfaceName());
                    assertEquals(SWITCH3_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10019), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex() == 10120 && link.getCdpCacheDeviceIndex() == 2 ) {
                    assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10120), link.getCdpInterfaceName());
                    assertEquals(SWITCH3_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10020), link.getCdpCacheDevicePort());
                } else {
                    fail();
                }
           } else if (link.getNode().getId().intValue() == switch3.getId().intValue()) {
               if (link.getCdpCacheIfIndex() == 10019 && link.getCdpCacheDeviceIndex() == 3 ) {
                   assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10019), link.getCdpInterfaceName());
                   assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                   assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                   assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                   assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10119), link.getCdpCacheDevicePort());
               } else if (link.getCdpCacheIfIndex() == 10020 && link.getCdpCacheDeviceIndex() == 4 ) {
                   assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10020), link.getCdpInterfaceName());
                   assertEquals(SWITCH2_IP,link.getCdpCacheAddress());
                   assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:53 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(SWITCH2_NAME, link.getCdpCacheDeviceId());
                   assertEquals("cisco WS-C2960G-24TC-L",link.getCdpCacheDevicePlatform());
                   assertEquals(SWITCH2_IF_IFDESCR_MAP.get(10120), link.getCdpCacheDevicePort());
               } else if (link.getCdpCacheIfIndex() == 10023 && link.getCdpCacheDeviceIndex() == 1 ) {
                   assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10023), link.getCdpInterfaceName());
                   assertEquals(SWITCH5_IP,link.getCdpCacheAddress());
                   assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(SWITCH5_NAME, link.getCdpCacheDeviceId());
                   assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                   assertEquals(SWITCH5_IF_IFDESCR_MAP.get(10001), link.getCdpCacheDevicePort());
               } else if (link.getCdpCacheIfIndex() == 10024 && link.getCdpCacheDeviceIndex() == 2 ) {
                   assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10024), link.getCdpInterfaceName());
                   assertEquals(SWITCH5_IP,link.getCdpCacheAddress());
                   assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(SWITCH5_NAME, link.getCdpCacheDeviceId());
                   assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                   assertEquals(SWITCH5_IF_IFDESCR_MAP.get(10013), link.getCdpCacheDevicePort());
               } else {
                   fail();
               }
            } else if (link.getNode().getId().intValue() == switch4.getId().intValue()) {
                if (link.getCdpCacheIfIndex() == 10001 && link.getCdpCacheDeviceIndex() == 1 ) {
                    assertEquals(SWITCH4_IF_IFDESCR_MAP.get(10001), link.getCdpInterfaceName());
                    assertEquals(ROUTER3_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(ROUTER3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                    assertEquals(ROUTER3_IF_IFDESCR_MAP.get(9), link.getCdpCacheDevicePort());
                } else {
                    fail();
                }
            } else if (link.getNode().getId().intValue() == switch5.getId().intValue()) {
                if (link.getCdpCacheIfIndex() == 10001 && link.getCdpCacheDeviceIndex() == 1 ) {
                    assertEquals(SWITCH5_IF_IFDESCR_MAP.get(10001), link.getCdpInterfaceName());
                    assertEquals(SWITCH3_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10023), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex() == 10013 && link.getCdpCacheDeviceIndex() == 2 ) {
                    assertEquals(SWITCH5_IF_IFDESCR_MAP.get(10013), link.getCdpInterfaceName());
                    assertEquals(SWITCH3_IP,link.getCdpCacheAddress());
                    assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH3_IF_IFDESCR_MAP.get(10024), link.getCdpCacheDevicePort());
                } else {
                    fail();
                }
            } else if (link.getNode().getId().intValue() == router1.getId().intValue()) {
                if (link.getCdpCacheIfIndex() == 7 && link.getCdpCacheDeviceIndex() == 2 ) {
                    assertEquals(ROUTER1_IF_IFDESCR_MAP.get(7), link.getCdpInterfaceName());
                    assertEquals(10101,SWITCH1_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                    assertEquals("Cisco IOS Software, C3560 Software (C3560-IPSERVICESK9-M), Version 12.2(58)SE1, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2011 by Cisco Systems, Inc. Compiled Thu 05-May-11 02:19 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(SWITCH1_NAME, link.getCdpCacheDeviceId());
                    assertEquals("cisco WS-C3560G-24PS",link.getCdpCacheDevicePlatform());
                    assertEquals(SWITCH1_IF_IFDESCR_MAP.get(10101), link.getCdpCacheDevicePort());
                 } else if (link.getCdpCacheIfIndex() == 13 && link.getCdpCacheDeviceIndex() == 1 ) {
                     assertEquals(ROUTER1_IF_IFDESCR_MAP.get(13), link.getCdpInterfaceName());
                     assertEquals(12,ROUTER2_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                     assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                     assertEquals(ROUTER2_NAME, link.getCdpCacheDeviceId());
                     assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                     assertEquals(ROUTER2_IF_IFDESCR_MAP.get(12), link.getCdpCacheDevicePort());
                     
                 } else {
                    fail();
                 }
            } else if (link.getNode().getId().intValue() == router2.getId().intValue()) {
                if (link.getCdpCacheIfIndex() == 12 && link.getCdpCacheDeviceIndex() == 2 ) {
                     assertEquals(ROUTER2_IF_IFDESCR_MAP.get(12), link.getCdpInterfaceName());
                     assertEquals(13,ROUTER1_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                     assertEquals("Cisco IOS Software, 2800 Software (C2800NM-ADVENTERPRISEK9-M), Version 12.4(24)T1, RELEASE SOFTWARE (fc3) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2009 by Cisco Systems, Inc. Compiled Fri 19-Jun-09 15:13 by prod_rel_team",link.getCdpCacheVersion());
                     assertEquals(ROUTER1_NAME, link.getCdpCacheDeviceId());
                     assertEquals("Cisco 2811",link.getCdpCacheDevicePlatform());
                     assertEquals(ROUTER1_IF_IFDESCR_MAP.get(13), link.getCdpCacheDevicePort());
                } else if (link.getCdpCacheIfIndex() == 13 && link.getCdpCacheDeviceIndex() == 1 ) {
                    assertEquals(ROUTER2_IF_IFDESCR_MAP.get(13), link.getCdpInterfaceName());
                    assertEquals(13,ROUTER3_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                    assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(ROUTER3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                    assertEquals(ROUTER3_IF_IFDESCR_MAP.get(13), link.getCdpCacheDevicePort());
                 } else {
                    fail();
                 }
            } else if (link.getNode().getId().intValue() == router3.getId().intValue()) {
                if (link.getCdpCacheIfIndex() == 8 && link.getCdpCacheDeviceIndex() == 2 ) {
                    assertEquals(ROUTER3_IF_IFDESCR_MAP.get(8), link.getCdpInterfaceName());
                    assertEquals(3,ROUTER4_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                    assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(ROUTER4_NAME, link.getCdpCacheDeviceId());
                    assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                    assertEquals(ROUTER4_IF_IFDESCR_MAP.get(3), link.getCdpCacheDevicePort());
               } else if (link.getCdpCacheIfIndex() == 9 && link.getCdpCacheDeviceIndex() == 3 ) {
                   assertEquals(ROUTER3_IF_IFDESCR_MAP.get(9), link.getCdpInterfaceName());
                   assertEquals(SWITCH4_IP,link.getCdpCacheAddress());
                   assertEquals("Cisco IOS Software, C2960 Software (C2960-LANBASEK9-M), Version 12.2(50)SE5, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2010 by Cisco Systems, Inc. Compiled Tue 28-Sep-10 13:44 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(SWITCH4_NAME, link.getCdpCacheDeviceId());
                   assertEquals("cisco WS-C2960-24TT-L",link.getCdpCacheDevicePlatform());
                   assertEquals(SWITCH4_IF_IFDESCR_MAP.get(10001), link.getCdpCacheDevicePort());
               } else if (link.getCdpCacheIfIndex() == 13 && link.getCdpCacheDeviceIndex() == 1 ) {
                   assertEquals(ROUTER3_IF_IFDESCR_MAP.get(13), link.getCdpInterfaceName());
                   assertEquals(13,ROUTER2_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                   assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                   assertEquals(ROUTER2_NAME, link.getCdpCacheDeviceId());
                   assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                   assertEquals(ROUTER2_IF_IFDESCR_MAP.get(13), link.getCdpCacheDevicePort());
                } else {
                    fail();
                }
            } else if (link.getNode().getId().intValue() == router4.getId().intValue()) {
                if (link.getCdpCacheIfIndex() == 3 && link.getCdpCacheDeviceIndex() == 1 ) {
                    assertEquals(ROUTER4_IF_IFDESCR_MAP.get(3), link.getCdpInterfaceName());
                    assertEquals(8,ROUTER3_IP_IF_MAP.get(InetAddressUtils.addr(link.getCdpCacheAddress())).intValue());
                    assertEquals("Cisco IOS Software, C2900 Software (C2900-UNIVERSALK9-M), Version 15.1(4)M4, RELEASE SOFTWARE (fc1) Technical Support: http://www.cisco.com/techsupport Copyright (c) 1986-2012 by Cisco Systems, Inc. Compiled Tue 20-Mar-12 18:57 by prod_rel_team",link.getCdpCacheVersion());
                    assertEquals(ROUTER3_NAME, link.getCdpCacheDeviceId());
                    assertEquals("Cisco CISCO2911/K9",link.getCdpCacheDevicePlatform());
                    assertEquals(ROUTER3_IF_IFDESCR_MAP.get(8), link.getCdpCacheDevicePort());
                 } else {
                    fail();
                 }
            } else {
                fail();
            }
        }
    }

    /* 
     * only two node topology
     * switch1 GigabitEthernet 0/9 0/10 0/11 0/12 <---> switch2 GigabitEthernet 0/1 0/2 0/3 0/4
     *
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource=SWITCH2_SNMP_RESOURCE)

    })
    public void testNetwork17216CdpTopology() {
        m_nodeDao.save(builder.getSwitch1());
        m_nodeDao.save(builder.getSwitch2());
        
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(true);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);

        assertFalse(m_linkdConfig.useLldpDiscovery());
        assertTrue(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        //update configuration to support only CDP updates
        //need to reload daemon
        m_linkd.reload();
        assertEquals(4, getSupportedProtocolsAsProtocolSupported().size());
        assertTrue(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.NODES));
        assertTrue(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.NETWORKROUTER));
        assertFalse(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.BRIDGE));
        assertTrue(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.CDP));
        assertFalse(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.ISIS));
        assertFalse(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.LLDP));
        assertFalse(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.OSPF));
        assertTrue(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.USERDEFINED));

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);

        m_linkd.reload();

        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertEquals(5, m_cdpLinkDao.countAll());
        assertTrue(m_cdpTopologyService.hasUpdates());
        
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertEquals(11, m_cdpLinkDao.countAll());
        assertTrue(m_cdpTopologyService.hasUpdates());
        
        m_cdpTopologyService.updatesAvailable();
        assertEquals(2, m_nodeTopologyService.findAllSnmpNode().size());
        assertEquals(2, m_nodeTopologyService.findAllNode().size());
        assertEquals(6, m_nodeTopologyService.findAllIp().size());
        
        m_nodeTopologyService.findAllNode().forEach(node -> {
            System.err.println(node);
            assertNotNull(node.getId());
            assertNotNull(node.getLabel());
        });

        m_nodeTopologyService.findAllIp().forEach(ip -> {
            System.err.println(ip);
            assertNotNull(ip.getId());
            assertNotNull(ip.getIpAddress());
            assertNotNull(ip.getIsSnmpPrimary());
            assertTrue(ip.isManaged());
        });

        CdpOnmsTopologyUpdater cdptopology = m_linkd.getCdpTopologyUpdater();
        assertNotNull(cdptopology);
        //Test buildTopology method, just 
        // build the topology but no updates
        OnmsTopology topology = cdptopology.buildTopology();
        assertEquals(2, topology.getVertices().size());
        assertEquals(4, topology.getEdges().size());
        
        //Testing updates
        OnmsTopologyLogger tl = createAndSubscribe(
                  ProtocolSupported.CDP.name());
        assertEquals("CDP:Consumer:Logger", tl.getName());
        //No updates not yet runned 
        assertEquals(0, tl.getQueue().size());        
        assertTrue(m_cdpTopologyService.hasUpdates());
        System.err.println("--------Printing new start----------");
        m_linkd.runTopologyUpdater(ProtocolSupported.CDP);
        System.err.println("--------Printing new end----------");
        assertFalse(m_cdpTopologyService.hasUpdates());
        assertFalse(m_cdpTopologyService.parseUpdates());
        assertEquals(6, tl.getQueue().size());
        for (OnmsTopologyMessage m: tl.getQueue()) {
            assertEquals(TopologyUpdater.create(ProtocolSupported.CDP), m.getProtocol());
            assertEquals(TopologyMessageStatus.UPDATE, m.getMessagestatus());
            if (m.getMessagebody() instanceof OnmsTopologyVertex) {
                OnmsTopologyVertex vertex = (OnmsTopologyVertex) m.getMessagebody();
                assertNotNull(vertex.getId());
                assertNotNull(vertex.getNodeid());
                assertNotNull(vertex.getLabel());
                assertNotNull(vertex.getAddress());
                assertNotNull(vertex.getIconKey());
                assertNotNull(vertex.getToolTipText());
            } else if (m.getMessagebody() instanceof OnmsTopologyEdge ) {
                OnmsTopologyEdge edge = (OnmsTopologyEdge) m.getMessagebody();
                assertNotNull(edge.getId());
                assertNotNull(edge.getSource().getVertex());
                assertNotNull(edge.getTarget().getVertex());
            } else {
                fail();
            }
        }
        System.err.println("--------no updates start----------");
        m_linkd.runTopologyUpdater(ProtocolSupported.CDP);
        System.err.println("--------no updates end----------");
        m_cdpTopologyService.updatesAvailable();
        assertTrue(m_cdpTopologyService.parseUpdates());
        assertFalse(m_cdpTopologyService.hasUpdates());
        assertEquals(6, tl.getQueue().size());
    }
    
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=SWITCH1_IP, port=161, resource=SWITCH1_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH2_IP, port=161, resource=SWITCH2_SNMP_RESOURCE),
            @JUnitSnmpAgent(host=SWITCH3_IP, port=161, resource=SWITCH3_SNMP_RESOURCE)
    })
    public void testNetwork17216LldpTopology() {
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
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());
        
        // reload daemon and support only: LLDP updates
        m_linkd.reload();
        assertTrue(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.NODES));
        assertTrue(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.NETWORKROUTER));
        assertEquals(4, getSupportedProtocolsAsProtocolSupported().size());
        assertFalse(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.BRIDGE));
        assertFalse(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.CDP));
        assertFalse(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.ISIS));
        assertTrue(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.LLDP));
        assertFalse(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.OSPF));
        assertTrue(getSupportedProtocolsAsProtocolSupported().contains(ProtocolSupported.USERDEFINED));

        final OnmsNode switch1 = m_nodeDao.findByForeignId("linkd", SWITCH1_NAME);
        final OnmsNode switch2 = m_nodeDao.findByForeignId("linkd", SWITCH2_NAME);
        final OnmsNode switch3 = m_nodeDao.findByForeignId("linkd", SWITCH3_NAME);

        m_linkd.reload();

        assertTrue(m_linkd.runSingleSnmpCollection(switch1.getId()));
        assertEquals(4, m_lldpLinkDao.countAll());
        assertTrue(m_lldpTopologyService.hasUpdates());
        
        assertTrue(m_linkd.runSingleSnmpCollection(switch2.getId()));
        assertEquals(10, m_lldpLinkDao.countAll());
        assertTrue(m_lldpTopologyService.hasUpdates());
                
        LldpOnmsTopologyUpdater lldptopology = m_linkd.getLldpTopologyUpdater();
        assertNotNull(lldptopology);
        OnmsTopology topology = lldptopology.buildTopology();
        assertEquals(2, topology.getVertices().size());
        assertEquals(4, topology.getEdges().size());
        
        OnmsTopologyLogger tl = createAndSubscribe(
                  ProtocolSupported.LLDP.name());
        assertEquals("LLDP:Consumer:Logger", tl.getName());
                
        System.err.println("--------Printing new start----------");
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);
        System.err.println("--------Printing new end----------");
        assertEquals(6, tl.getQueue().size());

        OnmsTopology lldptopo2 = m_topologyDao.getTopology(ProtocolSupported.LLDP.name());
        assertEquals(2, lldptopo2.getVertices().size());
        assertEquals(4, lldptopo2.getEdges().size());

        assertTrue(m_linkd.runSingleSnmpCollection(switch3.getId()));
        assertEquals(3, m_lldpElementDao.countAll());
        assertEquals(12, m_lldpLinkDao.countAll());
        assertTrue(m_lldpTopologyService.hasUpdates());
        System.err.println("-------- updates start----------");
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);
        System.err.println("-------- updates end----------");
        assertEquals(9, tl.getQueue().size());
        int vertices = 0;
        int edges = 0;
        for (OnmsTopologyMessage m: tl.getQueue()) {
            assertEquals(TopologyUpdater.create(ProtocolSupported.LLDP), m.getProtocol());
            assertEquals(TopologyMessageStatus.UPDATE, m.getMessagestatus());
            if (m.getMessagebody() instanceof OnmsTopologyVertex) {
                OnmsTopologyVertex vertex = (OnmsTopologyVertex) m.getMessagebody();
                assertNotNull(vertex.getId());
                assertNotNull(vertex.getNodeid());
                assertNotNull(vertex.getLabel());
                assertNotNull(vertex.getAddress());
                assertNotNull(vertex.getIconKey());
                assertNotNull(vertex.getToolTipText());
                vertices++;
            } else if (m.getMessagebody() instanceof OnmsTopologyEdge ) {
                OnmsTopologyEdge edge = (OnmsTopologyEdge) m.getMessagebody();
                assertNotNull(edge.getId());
                assertNotNull(edge.getSource().getVertex());
                assertNotNull(edge.getTarget().getVertex());
                edges++;
            } else {
                fail();
            }
        }
        assertEquals(3, vertices);
        assertEquals(6, edges);

        OnmsTopology lldptopo3 = m_topologyDao.getTopology(ProtocolSupported.LLDP.name());
        assertEquals(3, lldptopo3.getVertices().size());
        assertEquals(6, lldptopo3.getEdges().size());
    }


}
