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
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms13923NetworkBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;

import static org.junit.Assert.*;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.*;

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

        assertTrue(m_linkd.scheduleNodeCollection(srv005.getId()));


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
