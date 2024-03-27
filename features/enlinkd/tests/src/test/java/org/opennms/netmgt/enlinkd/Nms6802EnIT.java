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
import static org.junit.Assert.fail;
import static org.opennms.netmgt.nb.Nms6802NetworkBuilder.CISCOISIS_IP;
import static org.opennms.netmgt.nb.Nms6802NetworkBuilder.CISCOISIS_NAME;
import static org.opennms.netmgt.nb.Nms6802NetworkBuilder.CISCOISIS_ISIS_SYS_ID;
import static org.opennms.netmgt.nb.Nms6802NetworkBuilder.CISCOISIS_SNMP_RESOURCE;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.enlinkd.model.IsIsLink.IsisISAdjNeighSysType;
import org.opennms.netmgt.enlinkd.model.IsIsLink.IsisISAdjState;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms6802NetworkBuilder;

public class Nms6802EnIT extends EnLinkdBuilderITCase {

	Nms6802NetworkBuilder builder = new Nms6802NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = CISCOISIS_IP, port = 161, resource = CISCOISIS_SNMP_RESOURCE),
    })
    public void testIsIsLinks() {
        
        m_nodeDao.save(builder.getCiscoIosXrRouter());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        
        assertTrue(m_linkdConfig.useIsisDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        
        final OnmsNode ciscoiosxr = m_nodeDao.findByForeignId("linkd", CISCOISIS_NAME);
        
        m_linkd.reload();
        assertTrue(m_linkd.runSingleSnmpCollection(ciscoiosxr.getId()));
        assertEquals(4, m_isisLinkDao.countAll());

        for (IsIsElement node: m_isisElementDao.findAll()) {
        	assertNotNull(node);
        	System.err.println(node);
        	assertEquals(CISCOISIS_ISIS_SYS_ID, node.getIsisSysID());
        	assertEquals(IsisAdminState.on,node.getIsisSysAdminState());
        }
        
        for (IsIsLink link: m_isisLinkDao.findAll()) {
            System.err.println(link);
            assertEquals(IsisAdminState.on, link.getIsisCircAdminState());
            assertEquals(IsisISAdjState.up,link.getIsisISAdjState());
            assertEquals("000000000000", link.getIsisISAdjNeighSNPAAddress());
            assertEquals(IsisISAdjNeighSysType.l2IntermediateSystem, link.getIsisISAdjNeighSysType());
            switch (link.getIsisCircIndex()) {
            case 19:
                assertEquals(5, link.getIsisISAdjIndex().intValue());
                assertEquals(19, link.getIsisCircIfIndex().intValue());
                assertEquals("093176092059", link.getIsisISAdjNeighSysID());
                assertEquals(234881856,link.getIsisISAdjNbrExtendedCircID().intValue());
                break;
            case 20:
                assertEquals(5, link.getIsisISAdjIndex().intValue());
                assertEquals(20, link.getIsisCircIfIndex().intValue());
                assertEquals("093176092059", link.getIsisISAdjNeighSysID());
                assertEquals(234881920,link.getIsisISAdjNbrExtendedCircID().intValue());
                break;
            case 27:
                assertEquals(3, link.getIsisISAdjIndex().intValue());
                assertEquals(27, link.getIsisCircIfIndex().intValue());
                assertEquals("093176090003", link.getIsisISAdjNeighSysID());
                assertEquals(33554880,link.getIsisISAdjNbrExtendedCircID().intValue());
                break;
            case 28:
                assertEquals(3, link.getIsisISAdjIndex().intValue());
                assertEquals(28, link.getIsisCircIfIndex().intValue());
                assertEquals("093176090003", link.getIsisISAdjNeighSysID());
                assertEquals(33554944,link.getIsisISAdjNbrExtendedCircID().intValue());
                break;
            default:
                fail();
                break;
            }
        }
    }
}
