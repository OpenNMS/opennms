/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FROH_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FROH_ISIS_SYS_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FROH_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.FROH_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.OEDIPUS_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.OEDIPUS_ISIS_SYS_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.OEDIPUS_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.OEDIPUS_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SIEGFRIE_IP;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SIEGFRIE_ISIS_SYS_ID;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SIEGFRIE_NAME;
import static org.opennms.netmgt.nb.NmsNetworkBuilder.SIEGFRIE_SNMP_RESOURCE;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.config.linkd.Package;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.opennms.netmgt.model.topology.LinkableNode;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms0001NetworkBuilder;

public class Nms0001Test extends LinkdTestBuilder {

	Nms0001NetworkBuilder builder = new Nms0001NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = FROH_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = OEDIPUS_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = SIEGFRIE_SNMP_RESOURCE)
    })
    public void testIsIsLinks() throws Exception {
        
        m_nodeDao.save(builder.getFroh());
        m_nodeDao.save(builder.getOedipus());
        m_nodeDao.save(builder.getSiegFrie());
        m_nodeDao.flush();

        Package example1 = m_linkdConfig.getPackage("example1");
        example1.setUseBridgeDiscovery(false);
        example1.setUseIpRouteDiscovery(false);
        example1.setEnableVlanDiscovery(false);
        example1.setUseOspfDiscovery(false);
        example1.setUseLldpDiscovery(false);
        
        example1.setSaveStpInterfaceTable(false);
        example1.setSaveRouteTable(false);
        example1.setSaveStpNodeTable(false);

        final OnmsNode froh = m_nodeDao.findByForeignId("linkd", FROH_NAME);
        final OnmsNode oedipus = m_nodeDao.findByForeignId("linkd", OEDIPUS_NAME);
        final OnmsNode siegfrie = m_nodeDao.findByForeignId("linkd", SIEGFRIE_NAME);
        
        assertTrue(m_linkd.scheduleNodeCollection(froh.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(oedipus.getId()));
        assertTrue(m_linkd.scheduleNodeCollection(siegfrie.getId()));

        assertTrue(m_linkd.runSingleSnmpCollection(froh.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(oedipus.getId()));
        assertTrue(m_linkd.runSingleSnmpCollection(siegfrie.getId()));
       
        assertEquals(0,m_dataLinkInterfaceDao.countAll());
        
        final Collection<LinkableNode> nodes = m_linkd.getLinkableNodesOnPackage("example1");

        assertEquals(3, nodes.size());
        
        for (LinkableNode node: nodes) {
            assertEquals(2, node.getIsisInterfaces().size());
            switch(node.getNodeId()) {
                case 1: assertEquals(FROH_ISIS_SYS_ID, node.getIsisSysId());
                break;
                case 2: assertEquals(OEDIPUS_ISIS_SYS_ID, node.getIsisSysId());
                break;
                case 3: assertEquals(SIEGFRIE_ISIS_SYS_ID, node.getIsisSysId());
                break;
                default: assertEquals(-1, node.getNodeId());
                break;
            }
        }        
        
        assertTrue(m_linkd.runSingleLinkDiscovery("example1"));

        assertEquals(3,m_dataLinkInterfaceDao.countAll());
        final List<DataLinkInterface> datalinkinterfaces = m_dataLinkInterfaceDao.findAll();

        int start=getStartPoint(datalinkinterfaces);

        /*
         * 
         * These are the links among the following nodes discovered using 
         * only the isis protocol
         * froh:ae1.0(599):10.1.3.6/30       <-->    oedipus:ae1.0(578):10.1.3.5/30
         * froh:ae2.0(600):10.1.3.2/30       <-->    siegfrie:ae2.0(552):10.1.3.1/30
         * oedipus:ae0.0(575):10.1.0.10/30   <-->    siegfrie:ae0.0(533):10.1.0.9/30
         * 
         */
        for (final DataLinkInterface datalinkinterface: datalinkinterfaces) {
            
            Integer linkid = datalinkinterface.getId();
            if ( linkid == start) {
                checkLink(froh, oedipus, 599, 578, datalinkinterface);
            } else if (linkid == start+1 ) {
                checkLink(froh, siegfrie, 600, 552, datalinkinterface);
            } else if (linkid == start+2) {
                checkLink(oedipus, siegfrie, 575, 533, datalinkinterface);
            } else {
                // error
                checkLink(froh,froh,-1,-1,datalinkinterface);
            } 
            
        }
        
        DataLinkInterface iface = m_dataLinkInterfaceDao.findByNodeIdAndIfIndex(froh.getId(), Integer.valueOf(599)).iterator().next();
        iface.setNodeParentId(oedipus.getId());
        iface.setParentIfIndex(578);
        iface.setStatus(StatusType.ACTIVE);
        iface.setLastPollTime(new Date());
        m_dataLinkInterfaceDao.saveOrUpdate(iface);
        
        assertEquals(3, m_dataLinkInterfaceDao.countAll());
    }
}
