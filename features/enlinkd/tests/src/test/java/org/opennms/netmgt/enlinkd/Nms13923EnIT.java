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
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms13923NetworkBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import static org.opennms.netmgt.nb.Nms13923NetworkBuilder.srv005_NAME;
import static org.opennms.netmgt.nb.Nms13923NetworkBuilder.srv005_IP;
import static org.opennms.netmgt.nb.Nms13923NetworkBuilder.srv005_RESOURCE;

public class Nms13923EnIT extends EnLinkdBuilderITCase {
        
	Nms13923NetworkBuilder builder = new Nms13923NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=srv005_IP, port=161, resource=srv005_RESOURCE)
    })
    public void testLldpSrv005Links() {
        m_nodeDao.save(builder.getSrv005());

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

        final OnmsNode srv005 = m_nodeDao.findByForeignId("linkd", srv005_NAME);

        m_linkd.reload();

        assertTrue(m_linkd.runSingleSnmpCollection(srv005.getId()));
        assertEquals(1, m_lldpElementDao.countAll());

        for (LldpLink link: m_lldpLinkDao.findAll()) {
            printLldpLink(link);
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, link.getLldpPortIdSubType());
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, link.getLldpRemChassisIdSubType());
        }
        assertEquals(49, m_lldpLinkDao.countAll());
        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater updater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = updater.getTopology();
        Assert.assertNotNull(topology);
        assertEquals(1,topology.getVertices().size());
        assertEquals(0,topology.getEdges().size());




    }



}
