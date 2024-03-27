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
import org.opennms.netmgt.nb.Nms13637NetworkBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyEdge;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.MKTROUTER1_NAME;
import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.MKTROUTER1_IP;
import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.MKTROUTER1_RESOURCE;
import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.MKTROUTER1_ETHER1_MAC;

import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.MKTROUTER2_NAME;
import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.MKTROUTER2_IP;
import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.MKTROUTER2_RESOURCE;
import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.MKTROUTER2_ETHER1_MAC;

import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.CISCO_SW01_NAME;
import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.CISCO_SW01_IP;
import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.CISCO_SW01_RESOURCE;
import static org.opennms.netmgt.nb.Nms13637NetworkBuilder.CISCO_SW01_LLDP_ID;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;

public class Nms13637EnIT extends EnLinkdBuilderITCase {
        
	Nms13637NetworkBuilder builder = new Nms13637NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=MKTROUTER1_IP, port=161, resource=MKTROUTER1_RESOURCE),
            @JUnitSnmpAgent(host=MKTROUTER2_IP, port=161, resource=MKTROUTER2_RESOURCE),
            @JUnitSnmpAgent(host= CISCO_SW01_IP, port=161, resource= CISCO_SW01_RESOURCE)
    })

    public void testLldpLinks() {
        m_nodeDao.save(builder.getCiscoHomeSw());
        m_nodeDao.save(builder.getRouter1());
        m_nodeDao.save(builder.getRouter2());

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

        final OnmsNode ciscohomesw = m_nodeDao.findByForeignId("linkd", CISCO_SW01_NAME);
        final OnmsNode router1 = m_nodeDao.findByForeignId("linkd", MKTROUTER1_NAME);
        final OnmsNode router2 = m_nodeDao.findByForeignId("linkd", MKTROUTER2_NAME);

        m_linkd.reload();

        assertTrue(m_linkd.runSingleSnmpCollection(router1.getId()));
        assertEquals(1, m_lldpElementDao.countAll());
        assertEquals(5, m_lldpLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(router2.getId()));
        assertEquals(2, m_lldpElementDao.countAll());
        assertEquals(10, m_lldpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ciscohomesw.getId()));
        assertEquals(3, m_lldpElementDao.countAll());
        assertEquals(19, m_lldpLinkDao.countAll());


        int ei = 0;
        int ej = 0;
        int ek = 0;
        for (final LldpElement node: m_lldpElementDao.findAll()) {
            printLldpElement(node);
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, node.getLldpChassisIdSubType());
            switch (node.getLldpSysname()) {
                case "router-1":
                    assertEquals(router1.getId().intValue(), node.getNode().getId().intValue());
                    assertEquals(MKTROUTER1_ETHER1_MAC, node.getLldpChassisId());
                    ei++;
                    break;
                case "router-2":
                    assertEquals(router2.getId().intValue(), node.getNode().getId().intValue());
                    assertEquals(MKTROUTER2_ETHER1_MAC, node.getLldpChassisId());
                    ej++;
                    break;
                case "sw01-office":
                    assertEquals(ciscohomesw.getId().intValue(), node.getNode().getId().intValue());
                    assertEquals(CISCO_SW01_LLDP_ID, node.getLldpChassisId());
                    ek++;
                    break;
                default:
                    fail();
                    break;
            }
        }
        assertEquals(1, ei);
        assertEquals(1, ej);
        assertEquals(1, ek);


        for (LldpLink link : m_lldpLinkDao.findAll()) {
            printLldpLink(link);
            Assert.assertNotNull(link.getLldpRemPortDescr());
            if (link.getNode().getId().intValue() == router1.getId().intValue()) {
                Assert.assertEquals("", link.getLldpRemPortDescr());
            } else  if (link.getNode().getId().intValue() == router2.getId().intValue()) {
                Assert.assertEquals("", link.getLldpRemPortDescr());
            } else {
                Assert.assertEquals(ciscohomesw.getId().intValue(), link.getNode().getId().intValue());
                switch (link.getLldpRemIndex()) {
                    case 9:
                    case 73:
                    case 74:
                        Assert.assertEquals("", link.getLldpRemPortDescr());
                        break;
                    case 10:
                    case 55:
                    case 56:
                    case 58:
                    case 59:
                    case 66:
                        Assert.assertNotEquals("", link.getLldpRemPortDescr());
                        break;
                    default:
                        fail();
                }
            }
        }

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater updater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = updater.getTopology();
        Assert.assertNotNull(topology);
        assertEquals(3, topology.getVertices().size());
        int i = 0;
        int j = 0;
        int k = 0;
        for (OnmsTopologyVertex v : topology.getVertices()) {
            switch (v.getLabel()) {
                case "router-1":
                    i++;
                    break;
                case "router-2":
                    j++;
                    break;
                case "sw01-office":
                    k++;
                    break;
                default:
                    fail();
            }

        }
        assertEquals(1, i);
        assertEquals(1, j);
        assertEquals(1, k);

        assertEquals(3,topology.getEdges().size());
        for (OnmsTopologyEdge e : topology.getEdges()) {
            System.err.println("-------------Edge-------------------");
            System.err.println(e.getSource().getVertex().getLabel() + ":" + e.getSource().getIfname() + "<->" + e.getTarget().getIfname() + ":" + e.getTarget().getVertex().getLabel());
            System.err.println(e.getSource().getToolTipText());
            System.err.println(e.getTarget().getToolTipText());
            assertEquals("ether1", e.getSource().getIfname());
            assertNotNull(e.getTarget().getIfname());
        }
    }
}
