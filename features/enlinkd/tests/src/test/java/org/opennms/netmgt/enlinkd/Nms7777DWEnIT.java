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
import static org.opennms.netmgt.nb.Nms7777DWNetworkBuilder.DW_IP;
import static org.opennms.netmgt.nb.Nms7777DWNetworkBuilder.DW_NAME;
import static org.opennms.netmgt.nb.Nms7777DWNetworkBuilder.DW_SNMP_RESOURCE;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms7777DWNetworkBuilder;

public class Nms7777DWEnIT extends EnLinkdBuilderITCase {

	Nms7777DWNetworkBuilder builder = new Nms7777DWNetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=DW_IP, port=161, resource=DW_SNMP_RESOURCE)
    })
    public void testLldpNoLinks() {
        
        m_nodeDao.save(builder.getDragonWaveRouter());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);

        assertFalse(m_linkdConfig.useIsisDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode dw = m_nodeDao.findByForeignId("linkd", DW_NAME);
        
        m_linkd.reload();
        assertTrue(m_linkd.runSingleSnmpCollection(dw.getId()));

        for (final LldpElement node: m_lldpElementDao.findAll()) {
                    printLldpElement(node);
        }

        assertEquals(0, m_lldpLinkDao.countAll());


        
        
    }
}
