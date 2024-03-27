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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.opennms.netmgt.nb.Nms0001NetworkBuilder.FROH_IP;
import static org.opennms.netmgt.nb.Nms0001NetworkBuilder.FROH_NAME;
import static org.opennms.netmgt.nb.Nms0001NetworkBuilder.FROH_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms0001NetworkBuilder.OEDIPUS_IP;
import static org.opennms.netmgt.nb.Nms0001NetworkBuilder.OEDIPUS_NAME;
import static org.opennms.netmgt.nb.Nms0001NetworkBuilder.OEDIPUS_SNMP_RESOURCE;
import static org.opennms.netmgt.nb.Nms0001NetworkBuilder.SIEGFRIE_IP;
import static org.opennms.netmgt.nb.Nms0001NetworkBuilder.SIEGFRIE_NAME;
import static org.opennms.netmgt.nb.Nms0001NetworkBuilder.SIEGFRIE_SNMP_RESOURCE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms0001NetworkBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;

public class Nms0001EnIT extends EnLinkdBuilderITCase {

	Nms0001NetworkBuilder builder = new Nms0001NetworkBuilder();

    /*
     *
     * These are the links among the following nodes discovered using
     * only the isis protocol
     *     froh:ae1.0(599):10.1.3.6/30
     *     froh:ae2.0(600):10.1.3.2/30
     *  oedipus:ae0.0(575):10.1.0.10/30
     *  oedipus:ae1.0(578):10.1.3.5/30
     * siegfrie:ae2.0(552):10.1.3.1/30
     * siegfrie:ae0.0(533):10.1.0.9/30
     *
     * siegfrie:0001 10.25.50.54:533    ---->  0001 10.25.50.62:00 1F 12 AC CB F0:0
     * siegfrie:0001 10.25.50.54:552    ---->  0001 10.08.85.00:00 21 59 0E 47 C2:0
     *
     *     froh:0001 10.08.85.00:599    ---->  0001 10.25.50.62:00 1F 12 AC CB F1:0
     *     froh:0001 10.08.85.00:600    ---->  0001 10.25.50.54:00 1F 12 AC C3 F2:0
     *
     *  oedipus:0001 10.25.50.62:575     ----> 0001 10.25.50.54:00 1F 12 AC C3 F0:0
     *  oedipus:0001 10.25.50.62:578     ----> 0001 10.08.85.00:00 21 59 0E 47 C1:0
     *
     * The problem is that the association with Address is into another mib
     *
     * froh-192.168.239.51-walk.txt:.1.3.6.1.2.1.138.1.6.1.1.4."599".1 = Hex-STRING: 00 1F 12 AC CB F1
     *
     * routing table for ip address                                      "ip route"  "mask" "level"      "next hop Snpa"
     * froh-192.168.239.51-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13."1.1.4"."10.1.0.4"   ."30"    .1    = Hex-STRING: 00 1F 12 AC CB F1
     * froh-192.168.239.51-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13."1.1.4"."10.1.0.8"   ."30"    .1    = Hex-STRING: 00 1F 12 AC CB F1
     * froh-192.168.239.51-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13."1.1.4"."10.255.0.62"."32     .1    = Hex-STRING: 00 1F 12 AC CB F1
     *
     *
     * oedipus-192.168.239.62-walk.txt:.1.2.840.10006.300.43.1.1.1.1.2.576 = Hex-STRING: 00 1F 12 AC CB F1
     * oedipus-192.168.239.62-walk.txt:.1.2.840.10006.300.43.1.1.1.1.2.578 = Hex-STRING: 00 1F 12 AC CB F1
     *
     *
     *  oedipus-192.168.239.62-walk.txt:.1.3.6.1.2.1.138.1.6.1.1.4."575".1 = Hex-STRING: 00 1F 12 AC C3 F0
     *
     *  oedipus-192.168.239.62-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13.1.1.4.10.1.0.0.30.1 = Hex-STRING: 00 1F 12 AC C3 F0
     *  oedipus-192.168.239.62-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13.1.1.4.10.1.3.0.30.1 = Hex-STRING: 00 1F 12 AC C3 F0
     *  oedipus-192.168.239.62-walk.txt:.1.3.6.1.2.1.138.1.8.1.1.13.1.1.4.10.255.0.54.32.1 = Hex-STRING: 00 1F 12 AC C3 F0
     *
     *  siegfrie-192.168.239.54-walk.txt:.1.2.840.10006.300.43.1.1.1.1.2.532 = Hex-STRING: 00 1F 12 AC C3 F0
     *  siegfrie-192.168.239.54-walk.txt:.1.2.840.10006.300.43.1.1.1.1.2.533 = Hex-STRING: 00 1F 12 AC C3 F0
     */


    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = FROH_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = OEDIPUS_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = SIEGFRIE_SNMP_RESOURCE)
    })
    public void testIsIsLinks() {
        
        m_nodeDao.save(builder.getFroh());
        m_nodeDao.save(builder.getOedipus());
        m_nodeDao.save(builder.getSiegFrie());
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
        
        final OnmsNode froh = m_nodeDao.findByForeignId("linkd", FROH_NAME);
        final OnmsNode oedipus = m_nodeDao.findByForeignId("linkd", OEDIPUS_NAME);
        final OnmsNode siegfrie = m_nodeDao.findByForeignId("linkd", SIEGFRIE_NAME);

        m_linkd.reload();

        assertTrue(m_linkd.runSingleSnmpCollection(froh.getId()));
        assertEquals(2, m_isisLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(oedipus.getId()));
        assertEquals(4, m_isisLinkDao.countAll());
        assertTrue(m_linkd.runSingleSnmpCollection(siegfrie.getId()));
        assertEquals(6, m_isisLinkDao.countAll());

        Map<Integer,IsIsElement> elementmap = new HashMap<>();
        for (IsIsElement node: m_isisElementDao.findAll()) {
        	assertNotNull(node);
        	System.err.println(node);
        	elementmap.put(node.getNode().getId(), node);
        }

        List<IsIsLink> isislinks = m_isisLinkDao.findAll();
        Set<Integer> parsed = new HashSet<>();
        int count = 0;
        for (IsIsLink sourceLink : isislinks) {
            if (parsed.contains(sourceLink.getId())) { 
                continue;
            }
            IsIsElement sourceElement = elementmap.get(sourceLink.getNode().getId());
            IsIsLink targetLink = null;
            for (IsIsLink link : isislinks) {
                if (sourceLink.getId().intValue() == link.getId().intValue()|| parsed.contains(link.getId()))
                    continue;
                IsIsElement targetElement = elementmap.get(link.getNode().getId());
                //Compare the remote data to the targetNode element data
                if (!sourceLink.getIsisISAdjNeighSysID().equals(targetElement.getIsisSysID())  
                        || !link.getIsisISAdjNeighSysID().equals(sourceElement.getIsisSysID())) { 
                    continue;
                }

                if (sourceLink.getIsisISAdjIndex().intValue() == 
                        link.getIsisISAdjIndex().intValue()  ) {
                    targetLink=link;
                    System.err.println(sourceLink + "<-\n->" + targetLink);
                    count++;
                    break;
                }
            }
            
            if (targetLink == null) {
                continue;
            }

            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
        }
        assertEquals(3,count);
    }

    @Test
   public void testLinkdNetworkTopologyUpdater() {

        m_nodeDao.save(builder.getFroh());
        m_nodeDao.save(builder.getOedipus());
        m_nodeDao.save(builder.getSiegFrie());
        m_nodeDao.flush();

        m_linkd.reload();

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.NETWORKROUTER);
        m_linkd.runTopologyUpdater(ProtocolSupported.NETWORKROUTER);

        NetworkRouterTopologyUpdater topologyUpdater = m_linkd.getNetworkRouterTopologyUpdater();

        assertNotNull(topologyUpdater);
        OnmsTopology topo = topologyUpdater.getTopology();
        printOnmsTopology(topo);
        assertThat(topo.getVertices(), hasSize(10));
        assertThat(topo.getEdges(), hasSize(12));

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = FROH_IP, port = 161, resource = FROH_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = OEDIPUS_IP, port = 161, resource = OEDIPUS_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = SIEGFRIE_IP, port = 161, resource = SIEGFRIE_SNMP_RESOURCE)
    })
    public void testIsIsLinksExec() throws InterruptedException {

        m_nodeDao.save(builder.getFroh());
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

        final OnmsNode froh = m_nodeDao.findByForeignId("linkd", FROH_NAME);

        m_linkd.reload();

        assertEquals(0, m_linkd.getStatus());
        assertEquals("START_PENDING", m_linkd.getStatusText());
        m_linkd.start();
        assertEquals(2, m_linkd.getStatus());
        assertEquals("RUNNING", m_linkd.getStatusText());

        assertTrue(m_linkd.execSingleSnmpCollection(froh.getId()));

        Thread.sleep(10000);
        m_isisLinkDao.flush();
        assertEquals(2, m_isisLinkDao.countAll());
    }
}
