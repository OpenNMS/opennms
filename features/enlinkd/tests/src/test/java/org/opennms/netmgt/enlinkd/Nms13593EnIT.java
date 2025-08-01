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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms13593NetworkBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

import static org.opennms.netmgt.nb.Nms13593NetworkBuilder.ZHBGO1Zsr001_NAME;
import static org.opennms.netmgt.nb.Nms13593NetworkBuilder.ZHBGO1Zsr001_IP;
import static org.opennms.netmgt.nb.Nms13593NetworkBuilder.ZHBGO1Zsr001_RESOURCE;

import static org.opennms.netmgt.nb.Nms13593NetworkBuilder.ZHBGO1Zsr002_NAME;
import static org.opennms.netmgt.nb.Nms13593NetworkBuilder.ZHBGO1Zsr002_IP;
import static org.opennms.netmgt.nb.Nms13593NetworkBuilder.ZHBGO1Zsr002_RESOURCE;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
public class Nms13593EnIT extends EnLinkdBuilderITCase {
        
	Nms13593NetworkBuilder builder = new Nms13593NetworkBuilder();
    /*
A:ZHBGO1Zsr001# show system lldp neighbor
Link Layer Discovery Protocol (LLDP) System Information

===============================================================================
NB = nearest-bridge   NTPMR = nearest-non-tpmr   NC = nearest-customer
===============================================================================
Lcl Port      Scope Remote Chassis ID  Index  Remote Port     Remote Sys Name
-------------------------------------------------------------------------------
3/2/c1/1      NB    50:E0:EF:00:06:00  1      1/1/c1/1, 100-* esat-1
3/2/c3/1      NB    50:E0:EF:00:06:00  2      1/1/c3/1, 100-* esat-1
3/2/c5/1      NB    24:21:24:DA:F6:3F  3      3/2/c5/1        ZHBGO1Zsr002
3/2/c6/1      NB    24:21:24:DA:F6:3F  4      3/2/c6/1        ZHBGO1Zsr002
===============================================================================
* indicates that the corresponding row element may have been truncated.
Number of neighbors : 4

two LLDP links must be found
ZHBGO1Zsr001 (3/2/c5/1) -> ZHBGO1Zsr002 (3/2/c5/1)
ZHBGO1Zsr001 (3/2/c6/1) -> ZHBGO1Zsr002 (3/2/c6/1)

     */

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ZHBGO1Zsr001_IP, port=161, resource=ZHBGO1Zsr001_RESOURCE),
            @JUnitSnmpAgent(host=ZHBGO1Zsr002_IP, port=161, resource=ZHBGO1Zsr002_RESOURCE)
    })
    public void testLldpLinks() {
        m_nodeDao.save(builder.getZHBGO1Zsr001());
        m_nodeDao.save(builder.getZHBGO1Zsr002());

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

        final OnmsNode zsr001 = m_nodeDao.findByForeignId("linkd", ZHBGO1Zsr001_NAME);
        final OnmsNode zsr002 = m_nodeDao.findByForeignId("linkd", ZHBGO1Zsr002_NAME);

        m_linkd.reload();

        assertTrue(m_linkd.runSingleSnmpCollection(zsr001.getId()));
        assertEquals(1, m_lldpElementDao.countAll());
        assertEquals(3, m_lldpLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(zsr002.getId()));
        assertEquals(2, m_lldpElementDao.countAll());
        assertEquals(7, m_lldpLinkDao.countAll());
       


        int ei = 0;
        int ej = 0;
        for (final LldpElement node: m_lldpElementDao.findAll()) {
            printLldpElement(node);
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS,node.getLldpChassisIdSubType());
            switch (node.getLldpSysname()) {
                case "ZHBGO1Zsr001":
                    assertEquals(zsr001.getId().intValue(),node.getNode().getId().intValue());
                    assertEquals("242124ece23f",node.getLldpChassisId());
                    ei++;
                    break;
                case "ZHBGO1Zsr002":
                    assertEquals(zsr002.getId().intValue(),node.getNode().getId().intValue());
                    assertEquals("242124daf63f",node.getLldpChassisId());
                    ej++;
                    break;
                default:
                    fail();
                    break;
            }
        }
        assertEquals(1,ei);
        assertEquals(1,ej);

        int l11=0;
        int l13=0;
        int l14=0;
        int l21=0;
        int l24=0;
        int l25=0;
        int l26=0;

        for (LldpLink link: m_lldpLinkDao.findAll()) {
            printLldpLink(link);
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, link.getLldpPortIdSubType());
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
            if (link.getNode().getId().intValue() == zsr001.getId().intValue()) {
                switch (link.getLldpPortIfindex()) {
                    case 104906753:
                        assertEquals(104906753,link.getLldpPortIfindex().intValue());
                        assertEquals("104906753",link.getLldpPortId());
                        assertEquals("50e0ef000600",link.getLldpRemChassisId());
                        assertEquals("esat-1",link.getLldpRemSysname());
                        assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, link.getLldpRemPortIdSubType());
                        assertEquals("35700737",link.getLldpRemPortId());
                        l11++;
                        break;
                    case 105037825:
                        assertEquals(105037825,link.getLldpPortIfindex().intValue());
                        assertEquals("105037825",link.getLldpPortId());
                        assertEquals("242124daf63f",link.getLldpRemChassisId());
                        assertEquals("ZHBGO1Zsr002",link.getLldpRemSysname());
                        assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpRemPortIdSubType());
                        assertEquals("3/2/c5/1",link.getLldpRemPortId());
                        l13++;
                        break;
                    case 105070593:
                        assertEquals(105070593,link.getLldpPortIfindex().intValue());
                        assertEquals("105070593",link.getLldpPortId());
                        assertEquals("242124daf63f",link.getLldpRemChassisId());
                        assertEquals("ZHBGO1Zsr002",link.getLldpRemSysname());
                        assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpRemPortIdSubType());
                        assertEquals("3/2/c6/1",link.getLldpRemPortId());
                        l14++;
                        break;
                    default:
                        fail();
                        break;
                }
            } else if (link.getNode().getId().intValue() == zsr002.getId().intValue()) {
                switch (link.getLldpPortIfindex()) {
                    case 104906753:
                        assertEquals(104906753,link.getLldpPortIfindex().intValue());
                        assertEquals("104906753",link.getLldpPortId());
                        assertEquals("50e0ef005000",link.getLldpRemChassisId());
                        assertEquals("esat-1",link.getLldpRemSysname());
                        assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, link.getLldpRemPortIdSubType());
                        assertEquals("35700737",link.getLldpRemPortId());
                        l21++;
                        break;
                    case 105037825:
                        assertEquals(105037825,link.getLldpPortIfindex().intValue());
                        assertEquals("105037825",link.getLldpPortId());
                        assertEquals("242124ece23f",link.getLldpRemChassisId());
                        assertEquals("ZHBGO1Zsr001",link.getLldpRemSysname());
                        assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpRemPortIdSubType());
                        assertEquals("3/2/c5/1",link.getLldpRemPortId());
                        l24++;
                        break;
                    case 105070593:
                        assertEquals(105070593,link.getLldpPortIfindex().intValue());
                        assertEquals("105070593",link.getLldpPortId());
                        assertEquals("242124ece23f",link.getLldpRemChassisId());
                        assertEquals("ZHBGO1Zsr001",link.getLldpRemSysname());
                        assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, link.getLldpRemPortIdSubType());
                        assertEquals("3/2/c6/1",link.getLldpRemPortId());
                        l25++;
                        break;
                    case 1140918299:
                        assertEquals(1140918299,link.getLldpPortIfindex().intValue());
                        assertEquals("1140918299",link.getLldpPortId());
                        assertEquals("e48184acbf34",link.getLldpRemChassisId());
                        assertEquals("sq342g4",link.getLldpRemSysname());
                        assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, link.getLldpRemPortIdSubType());
                        assertEquals("1610901763",link.getLldpRemPortId());
                        l26++;
                        break;
                    default:
                        fail();
                        break;
                }
            } else {
                fail();
            }
        }

        assertEquals(1,l11);
        assertEquals(1,l13);
        assertEquals(1,l14);
        assertEquals(1,l21);
        assertEquals(1,l24);
        assertEquals(1,l25);
        assertEquals(1,l26);

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater updater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = updater.getTopology();
        Assert.assertNotNull(topology);
        assertEquals(2,topology.getVertices().size());
        assertEquals(2,topology.getEdges().size());
        int i=0;
        int j=0;
        for (OnmsTopologyVertex v :topology.getVertices()) {
            switch (v.getLabel()) {
                case "ZHBGO1Zsr001":
                    i++;
                    break;
                case "ZHBGO1Zsr002":
                    j++;
                    break;
                default:
                    fail();
            }

        }
        assertEquals(1,i);
        assertEquals(1,j);

        for (OnmsTopologyEdge e : topology.getEdges()) {
            System.err.println(e.getSource().getToolTipText());
            System.err.println(e.getTarget().getToolTipText());

            assertEquals("ZHBGO1Zsr001", e.getSource().getVertex().getLabel());
            assertEquals("ZHBGO1Zsr002", e.getTarget().getVertex().getLabel());
            assertEquals(e.getSource().getIfname(), e.getTarget().getIfname());
            assertEquals(e.getSource().getIfindex().intValue(),e.getTarget().getIfindex().intValue());
            switch (e.getSource().getIfindex()) {
                case 105037825:
                    assertEquals("3/2/c5/1", e.getSource().getIfname());
                    assertEquals("3/2/c5/1", e.getTarget().getIfname());
                    break;
                case 105070593:
                    assertEquals("3/2/c6/1", e.getSource().getIfname());
                    assertEquals("3/2/c6/1", e.getTarget().getIfname());
                    break;
                default:
                    fail();
                    break;
            }
        }


    }

}
