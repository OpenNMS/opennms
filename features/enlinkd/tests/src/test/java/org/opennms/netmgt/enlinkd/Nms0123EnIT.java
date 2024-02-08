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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0111_IP;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0111_NAME;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0111_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0112_IP;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0112_NAME;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0112_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0113_IP;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0113_NAME;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0113_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0114_IP;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0114_NAME;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0114_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0121_IP;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0121_NAME;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0121_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0123_IP;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0123_NAME;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0123_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0201_IP;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0201_NAME;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0201_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0202_IP;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0202_NAME;
import static org.opennms.netmgt.nb.Nms0123NetworkBuilder.ITPN0202_SNMP_RESOURCE;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms0123NetworkBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;

public class Nms0123EnIT extends EnLinkdBuilderITCase {

    Nms0123NetworkBuilder builder = new Nms0123NetworkBuilder();

    /*
     *  itpn0111 -- itpn0112 --. itpn0113 -- itpn0202 -- itpn0123
     *     |                                     |
     *     itpn0201 -------------------------itpn0201
     *     |
     *   itpn0114 -- itpn0121
     *
     */
    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = ITPN0111_IP, port = 161, resource = ITPN0111_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = ITPN0112_IP, port = 161, resource = ITPN0112_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = ITPN0113_IP, port = 161, resource = ITPN0113_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = ITPN0114_IP, port = 161, resource = ITPN0114_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = ITPN0121_IP, port = 161, resource = ITPN0121_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = ITPN0123_IP, port = 161, resource = ITPN0123_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = ITPN0201_IP, port = 161, resource = ITPN0201_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = ITPN0202_IP, port = 161, resource = ITPN0202_SNMP_RESOURCE)
    })
    public void testItpnLldp() {
        
        m_nodeDao.save(builder.getItpn0111());
        m_nodeDao.save(builder.getItpn0112());
        m_nodeDao.save(builder.getItpn0113());
        m_nodeDao.save(builder.getItpn0114());
        m_nodeDao.save(builder.getItpn0121());
        m_nodeDao.save(builder.getItpn0123());
        m_nodeDao.save(builder.getItpn0201());
        m_nodeDao.save(builder.getItpn0202());
        m_nodeDao.flush();

        assertEquals(8,m_nodeDao.countAll());

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

        final OnmsNode itpn0111 = m_nodeDao.findByForeignId("linkd", ITPN0111_NAME);
        final OnmsNode itpn0112 = m_nodeDao.findByForeignId("linkd", ITPN0112_NAME);
        final OnmsNode itpn0113 = m_nodeDao.findByForeignId("linkd", ITPN0113_NAME);
        final OnmsNode itpn0114 = m_nodeDao.findByForeignId("linkd", ITPN0114_NAME);
        final OnmsNode itpn0121 = m_nodeDao.findByForeignId("linkd", ITPN0121_NAME);
        final OnmsNode itpn0123 = m_nodeDao.findByForeignId("linkd", ITPN0123_NAME);
        final OnmsNode itpn0201 = m_nodeDao.findByForeignId("linkd", ITPN0201_NAME);
        final OnmsNode itpn0202 = m_nodeDao.findByForeignId("linkd", ITPN0202_NAME);

        assertEquals(8,m_nodeDao.countAll());
        m_nodeDao.findAll().forEach(System.err::println);
        System.err.println(m_linkd.getQueryManager().getSnmpNode(itpn0202.getId()));

        m_linkd.reload();
        assertEquals(0,m_lldpLinkDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(itpn0111.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(itpn0112.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(itpn0113.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(itpn0114.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(itpn0121.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(itpn0123.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(itpn0201.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(itpn0202.getId()));

        assertEquals(8,m_lldpElementDao.countAll());
        assertEquals(23,m_lldpLinkDao.countAll());

        for (final LldpElement node: m_lldpElementDao.findAll()) {
        		printLldpElement(node);
        }
        printLldpTopology(m_lldpLinkDao.findAll());

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater topologyUpdater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();

        assertNotNull(topology);
        printOnmsTopology(topology);
        assertEquals(8,topology.getVertices().size());
        assertEquals(8,topology.getEdges().size());
    }

}
