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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.CdpElement.CdpGlobalDeviceIdFormat;
import org.opennms.netmgt.enlinkd.model.OspfElement.TruthValue;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms7467NetworkBuilder;

import static org.opennms.netmgt.nb.Nms7467NetworkBuilder.CISCO_WS_C2948_IP;
import static org.opennms.netmgt.nb.Nms7467NetworkBuilder.CISCO_WS_C2948_NAME;
import static org.opennms.netmgt.nb.Nms7467NetworkBuilder.CISCO_WS_C2948_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms7467NetworkBuilder.CISCO_WS_C2948_GLOBAL_DEVICEID;

public class Nms7467EnIT extends EnLinkdBuilderITCase {

	private final Nms7467NetworkBuilder builder = new Nms7467NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=CISCO_WS_C2948_IP, port=161, resource=CISCO_WS_C2948_SNMP_RESOURCE)
    })
    public void testCisco01Links() {
        
        m_nodeDao.save(builder.getCiscoWsC2948());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);

        assertFalse(m_linkdConfig.useIsisDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useLldpDiscovery());
        assertTrue(m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode cisco01 = m_nodeDao.findByForeignId("linkd", CISCO_WS_C2948_NAME);
        
        m_linkd.reload();

        assertTrue(m_linkd.runSingleSnmpCollection(cisco01.getId()));

        for (final CdpElement node: m_cdpElementDao.findAll()) {
            
            assertNotNull(node);
            printCdpElement(node);
            assertEquals(TruthValue.TRUE, node.getCdpGlobalRun());
            assertEquals(CISCO_WS_C2948_GLOBAL_DEVICEID,node.getCdpGlobalDeviceId());
            assertEquals(CdpGlobalDeviceIdFormat.other, node.getCdpGlobalDeviceIdFormat());
        }

        assertEquals(5, m_cdpLinkDao.countAll());
        for (CdpLink link: m_cdpLinkDao.findAll()) {
            assertNotNull(link);
            printCdpLink(link);
        }
    }
}
