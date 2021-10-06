/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.CdpLink.CiscoNetworkProtocolType;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms13593NetworkBuilder;
import org.opennms.netmgt.nb.Nms8000NetworkBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.*;

public class Nms13593EnIT extends EnLinkdBuilderITCase {
        
	Nms13593NetworkBuilder builder = new Nms13593NetworkBuilder();
    /*

     */

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host=ZHBGO1Zsr001_IP, port=161, resource=ZHBGO1Zsr001_RESOURCE),
            @JUnitSnmpAgent(host=ZHBGO1Zsr002_IP, port=161, resource=ZHBGO1Zsr002_RESOURCE)
    })
    public void testLldpLinks() throws Exception {
        m_nodeDao.save(builder.getZHBGO1Zsr001());
        m_nodeDao.save(builder.getZHBGO1Zsr002());

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

        final OnmsNode zsr001 = m_nodeDao.findByForeignId("linkd", ZHBGO1Zsr001_NAME);
        final OnmsNode zsr002 = m_nodeDao.findByForeignId("linkd", ZHBGO1Zsr002_NAME);

        assertTrue(m_linkd.scheduleNodeCollection(zsr001.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(zsr002.getId()));


        assertTrue(m_linkd.runSingleSnmpCollection(zsr001.getId()));
        assertEquals(1, m_lldpElementDao.countAll());
        assertEquals(3, m_lldpLinkDao.countAll());
        
        assertTrue(m_linkd.runSingleSnmpCollection(zsr002.getId()));
        assertEquals(2, m_lldpElementDao.countAll());
        assertEquals(7, m_lldpLinkDao.countAll());
       


        for (final LldpElement node: m_lldpElementDao.findAll()) {
            printLldpElement(node);
        }
        
        for (LldpLink link: m_lldpLinkDao.findAll()) {
            printLldpLink(link);
        }

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater updater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = updater.getTopology();
        Assert.assertNotNull(topology);
        assertEquals(2,topology.getVertices().size());
        assertEquals(2,topology.getEdges().size());

        
    }

}
