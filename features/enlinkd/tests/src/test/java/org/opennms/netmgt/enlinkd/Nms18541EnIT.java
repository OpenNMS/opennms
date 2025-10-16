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

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms18541NetworkBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opennms.netmgt.nb.Nms18541NetworkBuilder.*;


public class Nms18541EnIT extends EnLinkdBuilderITCase {
    //NMS-18541 is the jira issue connected to this test
    //Cannot Walk LLDP Remote Link Table for MICROSENS G6 Industrial Switch PLM

    Nms18541NetworkBuilder builder = new Nms18541NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = MS01_IP, port = 161, resource = MS01_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS02_IP, port = 161, resource = MS02_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS03_IP, port = 161, resource = MS03_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS04_IP, port = 161, resource = MS04_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS08_IP, port = 161, resource = MS08_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS09_IP, port = 161, resource = MS09_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = QFX_IP, port = 161, resource = QFX_SNMP_RESOURCE)
    })
    public void networkAllTest() throws InterruptedException {
        m_nodeDao.save(builder.getMs01());
        m_nodeDao.save(builder.getMs02());
        m_nodeDao.save(builder.getMs03());
        m_nodeDao.save(builder.getMs04());
        m_nodeDao.save(builder.getMs08());
        m_nodeDao.save(builder.getMs09());
        m_nodeDao.save(builder.getQFX());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode ms01 = m_nodeDao.findByForeignId("linkd", MS01_NAME);
        final OnmsNode ms02 = m_nodeDao.findByForeignId("linkd", MS02_NAME);
        final OnmsNode ms03 = m_nodeDao.findByForeignId("linkd", MS03_NAME);
        final OnmsNode ms04 = m_nodeDao.findByForeignId("linkd", MS04_NAME);
        final OnmsNode ms08 = m_nodeDao.findByForeignId("linkd", MS08_NAME);
        final OnmsNode ms09 = m_nodeDao.findByForeignId("linkd", MS09_NAME);
        final OnmsNode qfx = m_nodeDao.findByForeignId("linkd", QFX_SYSNAME);

        assertNotNull(ms01);
        assertNotNull(ms02);
        assertNotNull(ms03);
        assertNotNull(ms04);
        assertNotNull(ms08);
        assertNotNull(ms09);
        assertNotNull(qfx);
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ms01.getId()));
        assertEquals(2,m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms02.getId()));
        assertEquals(4,m_lldpLinkDao.countAll());
        assertEquals(2,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms03.getId()));
        assertEquals(5,m_lldpLinkDao.countAll());
        assertEquals(3,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms04.getId()));
        assertEquals(9,m_lldpLinkDao.countAll());
        assertEquals(4,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms08.getId()));
        assertEquals(12,m_lldpLinkDao.countAll());
        assertEquals(5,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms09.getId()));
        assertEquals(19,m_lldpLinkDao.countAll());
        assertEquals(6,m_lldpElementDao.countAll());
        Thread.sleep(200);

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        Thread.sleep(200);
        OnmsTopology topologyA =m_linkd.getLldpTopologyUpdater().getTopology();
        assertNotNull(topologyA);
        printOnmsTopology(topologyA);
        assertEquals(6,topologyA.getVertices().size());
        assertEquals(2,topologyA.getEdges().size());

        assertTrue(m_linkd.runSingleSnmpCollection(qfx.getId()));
        assertEquals(34,m_lldpLinkDao.countAll());
        assertEquals(7,m_lldpElementDao.countAll());

        Thread.sleep(200);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        OnmsTopology topology = m_linkd.getLldpTopologyUpdater().getTopology();
        assertEquals(7,topology.getVertices().size());
        assertEquals(6,topology.getEdges().size());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = QFX_IP, port = 161, resource = QFX_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS01_IP, port = 161, resource = MS01_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS02_IP, port = 161, resource = MS02_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS03_IP, port = 161, resource = MS03_SNMP_RESOURCE)
    })
    public void topoQfxSw01Sw02Sw03Test() throws InterruptedException {
        m_nodeDao.save(builder.getQFX());
        m_nodeDao.save(builder.getMs01());
        m_nodeDao.save(builder.getMs02());
        m_nodeDao.save(builder.getMs03());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode qfx = m_nodeDao.findByForeignId("linkd", QFX_SYSNAME);
        final OnmsNode ms01 = m_nodeDao.findByForeignId("linkd", MS01_NAME);
        final OnmsNode ms02 = m_nodeDao.findByForeignId("linkd", MS02_NAME);
        final OnmsNode ms03 = m_nodeDao.findByForeignId("linkd", MS03_NAME);

        assertNotNull(qfx);
        assertNotNull(ms01);
        assertNotNull(ms02);
        assertNotNull(ms03);
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(qfx.getId()));
        assertEquals(15,m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms01.getId()));
        assertEquals(17,m_lldpLinkDao.countAll());
        assertEquals(2,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms02.getId()));
        assertEquals(19,m_lldpLinkDao.countAll());
        assertEquals(3,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms03.getId()));
        assertEquals(20,m_lldpLinkDao.countAll());
        assertEquals(4,m_lldpElementDao.countAll());
        Thread.sleep(200);


        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater topologyUpdater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();

        assertNotNull(topology);
        printOnmsTopology(topology);
        Thread.sleep(200);

        assertEquals(4,topology.getVertices().size());
        assertEquals(3,topology.getEdges().size());


    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = QFX_IP, port = 161, resource = QFX_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS01_IP, port = 161, resource = MS01_SNMP_RESOURCE)
    })
    public void topoQfxSw01Test() throws InterruptedException {
        m_nodeDao.save(builder.getQFX());
        m_nodeDao.save(builder.getMs01());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode qfx = m_nodeDao.findByForeignId("linkd", QFX_SYSNAME);
        final OnmsNode ms01 = m_nodeDao.findByForeignId("linkd", MS01_NAME);

        assertNotNull(qfx);
        assertNotNull(ms01);
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(qfx.getId()));
        assertEquals(15,m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms01.getId()));
        assertEquals(17,m_lldpLinkDao.countAll());
        assertEquals(2,m_lldpElementDao.countAll());
        Thread.sleep(200);

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater topologyUpdater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();

        assertNotNull(topology);
        printOnmsTopology(topology);

        assertEquals(2,topology.getVertices().size());
        assertEquals(0,topology.getEdges().size());

    }


    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = QFX_IP, port = 161, resource = QFX_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS02_IP, port = 161, resource = MS02_SNMP_RESOURCE)
    })
    public void topoQfxSw02Test() throws InterruptedException {
        m_nodeDao.save(builder.getQFX());
        m_nodeDao.save(builder.getMs02());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode qfx = m_nodeDao.findByForeignId("linkd", QFX_SYSNAME);
        final OnmsNode ms02 = m_nodeDao.findByForeignId("linkd", MS02_NAME);

        assertNotNull(qfx);
        assertNotNull(ms02);
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(qfx.getId()));
        assertEquals(15,m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms02.getId()));
        assertEquals(17,m_lldpLinkDao.countAll());
        assertEquals(2,m_lldpElementDao.countAll());
        Thread.sleep(200);

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater topologyUpdater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();

        assertNotNull(topology);
        printOnmsTopology(topology);

        assertEquals(2,topology.getVertices().size());
        assertEquals(1,topology.getEdges().size());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = QFX_IP, port = 161, resource = QFX_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS03_IP, port = 161, resource = MS03_SNMP_RESOURCE)
    })
    public void topoQfxSw03Test() throws InterruptedException {
        m_nodeDao.save(builder.getQFX());
        m_nodeDao.save(builder.getMs03());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode qfx = m_nodeDao.findByForeignId("linkd", QFX_SYSNAME);
        final OnmsNode ms03 = m_nodeDao.findByForeignId("linkd", MS03_NAME);

        assertNotNull(qfx);
        assertNotNull(ms03);
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(qfx.getId()));
        assertEquals(15,m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms03.getId()));
        assertEquals(16,m_lldpLinkDao.countAll());
        assertEquals(2,m_lldpElementDao.countAll());
        Thread.sleep(200);

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater topologyUpdater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();

        assertNotNull(topology);
        printOnmsTopology(topology);

        assertEquals(2,topology.getVertices().size());
        assertEquals(0,topology.getEdges().size());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = QFX_IP, port = 161, resource = QFX_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS04_IP, port = 161, resource = MS04_SNMP_RESOURCE)
    })
    public void topoQfxSw04Test() throws InterruptedException {
        m_nodeDao.save(builder.getQFX());
        m_nodeDao.save(builder.getMs04());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode qfx = m_nodeDao.findByForeignId("linkd", QFX_SYSNAME);
        final OnmsNode ms04 = m_nodeDao.findByForeignId("linkd", MS04_NAME);

        assertNotNull(qfx);
        assertNotNull(ms04);
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(qfx.getId()));
        assertEquals(15,m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms04.getId()));
        assertEquals(19,m_lldpLinkDao.countAll());
        assertEquals(2,m_lldpElementDao.countAll());
        Thread.sleep(200);

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater topologyUpdater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();

        assertNotNull(topology);
        printOnmsTopology(topology);

        assertEquals(2,topology.getVertices().size());
        assertEquals(1,topology.getEdges().size());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = QFX_IP, port = 161, resource = QFX_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS08_IP, port = 161, resource = MS08_SNMP_RESOURCE)
    })
    public void topoQfxSw08Test() throws InterruptedException {
        m_nodeDao.save(builder.getQFX());
        m_nodeDao.save(builder.getMs08());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode qfx = m_nodeDao.findByForeignId("linkd", QFX_SYSNAME);
        final OnmsNode ms08 = m_nodeDao.findByForeignId("linkd", MS08_NAME);

        assertNotNull(qfx);
        assertNotNull(ms08);
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(qfx.getId()));
        assertEquals(15,m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms08.getId()));
        assertEquals(18,m_lldpLinkDao.countAll());
        assertEquals(2,m_lldpElementDao.countAll());
        Thread.sleep(200);

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater topologyUpdater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();

        assertNotNull(topology);
        printOnmsTopology(topology);

        assertEquals(2,topology.getVertices().size());
        assertEquals(1,topology.getEdges().size());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = QFX_IP, port = 161, resource = QFX_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS09_IP, port = 161, resource = MS09_SNMP_RESOURCE)
    })
    public void topoQfxSw09Test() throws InterruptedException {
        m_nodeDao.save(builder.getQFX());
        m_nodeDao.save(builder.getMs09());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode qfx = m_nodeDao.findByForeignId("linkd", QFX_SYSNAME);
        final OnmsNode ms09 = m_nodeDao.findByForeignId("linkd", MS09_NAME);

        assertNotNull(qfx);
        assertNotNull(ms09);
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(qfx.getId()));
        assertEquals(15,m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms09.getId()));
        assertEquals(22,m_lldpLinkDao.countAll());
        assertEquals(2,m_lldpElementDao.countAll());
        Thread.sleep(200);

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater topologyUpdater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();

        assertNotNull(topology);
        printOnmsTopology(topology);

        assertEquals(2,topology.getVertices().size());
        assertEquals(1,topology.getEdges().size());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = QFX_IP, port = 161, resource = QFX_SNMP_RESOURCE)
    })
    public void qfxTest() {

        m_nodeDao.save(builder.getQFX());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode qfx = m_nodeDao.findByForeignId("linkd", QFX_SYSNAME);

        assertNotNull(qfx);
        assertNotNull(qfx.getId());
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(qfx.getId()));
        assertEquals(15, m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_lldpElementDao.findAll().forEach(e -> {
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, e.getLldpChassisIdSubType());
            assertEquals(QFX_LLDP_CHASSIS_ID, e.getLldpChassisId());
            assertEquals(QFX_SYSNAME, e.getLldpSysname());
        });

        m_lldpLinkDao.findAll().forEach(l -> {
            assertNotNull(l.getLldpRemPortIdSubType());
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpPortIdSubType());
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, l.getLldpRemChassisIdSubType());
            switch (l.getLldpRemLocalPortNum()) {
                case 567:
                    assertEquals(56, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals(MS08_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
                    assertEquals("2/5", l.getLldpRemPortId());
                    assertEquals("ge-0/0/15", l.getLldpPortId());
                    assertEquals("SCTT - VDS Minnovo Via Novara pros. Besozzi", l.getLldpPortDescr());
                    break;
                case 525:
                    assertEquals(74, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals(MS02_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
                    assertEquals("3/5", l.getLldpRemPortId());
                    assertEquals("ge-0/0/7", l.getLldpPortId());
                    assertEquals("to_ForzeArmate_Parco_Visconti", l.getLldpPortDescr());
                    break;
                case 570:
                    assertEquals(75, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals(MS09_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
                    assertEquals("3/6", l.getLldpRemPortId());
                    assertEquals("ge-0/0/16", l.getLldpPortId());
                    assertEquals("SCTT - VDS Minnovo Via Novara, 199 piano 1 - I0560 Commissariato PS San Siro", l.getLldpPortDescr());
                    break;
                case 556:
                    assertEquals(76, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals(MS04_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
                    assertEquals("2/5", l.getLldpRemPortId());
                    assertEquals("ge-0/0/13", l.getLldpPortId());
                    assertEquals("SCTT - VDS Minnovo Via Novara 387 ang. San Romanello", l.getLldpPortDescr());
                    break;
                case 519:
                    assertEquals(4, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, l.getLldpRemPortIdSubType());
                    assertEquals("002561268740", l.getLldpRemChassisId());
                    assertEquals("28", l.getLldpRemPortId());
                    assertEquals("ge-0/0/1", l.getLldpPortId());
                    assertEquals("E0406", l.getLldpPortDescr());
                    break;
                case 523:
                    assertEquals(6, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, l.getLldpRemPortIdSubType());
                    assertEquals("b439d6521240", l.getLldpRemChassisId());
                    assertEquals("28", l.getLldpRemPortId());
                    assertEquals("ge-0/0/5", l.getLldpPortId());
                    assertEquals("E0729", l.getLldpPortDescr());
                    break;
                case 524:
                    assertEquals(9, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, l.getLldpRemPortIdSubType());
                    assertEquals("001f28fdc940", l.getLldpRemChassisId());
                    assertEquals("27", l.getLldpRemPortId());
                    assertEquals("ge-0/0/6", l.getLldpPortId());
                    assertEquals("E0588", l.getLldpPortDescr());
                    break;
                case 528:
                    assertEquals(10, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("ecfcc6c83b80", l.getLldpRemChassisId());
                    assertEquals("ecfcc6c83b80", l.getLldpRemPortId());
                    assertEquals("ge-0/0/10", l.getLldpPortId());
                    assertEquals("to_via Arcangeli Pacifico", l.getLldpPortDescr());
                    break;
                case 531:
                    assertEquals(12, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, l.getLldpRemPortIdSubType());
                    assertEquals("28b829da6ce0", l.getLldpRemChassisId());
                    assertEquals("581", l.getLldpRemPortId());
                    assertEquals("et-0/0/48", l.getLldpPortId());
                    assertEquals("et-0/0/48", l.getLldpPortDescr());
                    break;
                case 527:
                    assertEquals(29, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("50e4e0ce3b8e", l.getLldpRemChassisId());
                    assertEquals("50e4e0ce3b8e", l.getLldpRemPortId());
                    assertEquals("ge-0/0/9", l.getLldpPortId());
                    assertEquals("to_Cascina_Bellaria90", l.getLldpPortDescr());
                    break;
                case 532:
                    assertEquals(30, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, l.getLldpRemPortIdSubType());
                    assertEquals("14b3a16cc846", l.getLldpRemChassisId());
                    assertEquals("581", l.getLldpRemPortId());
                    assertEquals("et-0/0/49", l.getLldpPortId());
                    assertEquals("et-0/0/49", l.getLldpPortDescr());
                    break;
                case 518:
                    assertEquals(53, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, l.getLldpRemPortIdSubType());
                    assertEquals("0026f18a6440", l.getLldpRemChassisId());
                    assertEquals("28", l.getLldpRemPortId());
                    assertEquals("ge-0/0/0", l.getLldpPortId());
                    assertEquals("E0095", l.getLldpPortDescr());
                    break;
                case 521:
                    assertEquals(71, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, l.getLldpRemPortIdSubType());
                    assertEquals("001b3f0851c0", l.getLldpRemChassisId());
                    assertEquals("26", l.getLldpRemPortId());
                    assertEquals("ge-0/0/3", l.getLldpPortId());
                    assertEquals("E0564", l.getLldpPortDescr());
                    break;
                case 520:
                    assertEquals(72, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, l.getLldpRemPortIdSubType());
                    assertEquals("b439d6520180", l.getLldpRemChassisId());
                    assertEquals("28", l.getLldpRemPortId());
                    assertEquals("ge-0/0/2", l.getLldpPortId());
                    assertEquals("E0482", l.getLldpPortDescr());
                    break;
                case 522:
                    assertEquals(77, l.getLldpRemIndex().intValue());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL, l.getLldpRemPortIdSubType());
                    assertEquals("0026f18ac480", l.getLldpRemChassisId());
                    assertEquals("28", l.getLldpRemPortId());
                    assertEquals("ge-0/0/4", l.getLldpPortId());
                    assertEquals("E0596", l.getLldpPortDescr());
                    break;
                default:
                    fail();
            }
        });

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = MS01_IP, port = 161, resource = MS01_SNMP_RESOURCE)
    })
    public void microsensSw01Test() {

        m_nodeDao.save(builder.getMs01());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode ms01 = m_nodeDao.findByForeignId("linkd", MS01_NAME);

        assertNotNull(ms01);
        assertNotNull(ms01.getId());
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ms01.getId()));
        assertEquals(2, m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_lldpElementDao.findAll().forEach(e -> {
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, e.getLldpChassisIdSubType());
            assertEquals(MS01_LLDP_CHASSIS_ID, e.getLldpChassisId());
            assertEquals(MS01_NAME, e.getLldpSysname());
        });

        m_lldpLinkDao.findAll().forEach(l -> {
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpPortIdSubType());
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, l.getLldpRemChassisIdSubType());
            assertEquals(0, l.getLldpRemIndex().intValue());
            switch (l.getLldpRemLocalPortNum()) {
                case 5:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals(MS02_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
                    assertEquals("2/5", l.getLldpRemPortId());
                    assertEquals("2/4", l.getLldpPortId());
                    break;
                case 11:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals(MS03_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
                    assertEquals("2/5", l.getLldpRemPortId());
                    assertEquals("3/4", l.getLldpPortId());
                    break;
                default:
                    fail();
            }
        });

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = MS02_IP, port = 161, resource = MS02_SNMP_RESOURCE)
    })
    public void microsensSw02Test() {

        m_nodeDao.save(builder.getMs02());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode ms02 = m_nodeDao.findByForeignId("linkd", MS02_NAME);

        assertNotNull(ms02);
        assertNotNull(ms02.getId());
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ms02.getId()));
        assertEquals(2, m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_lldpElementDao.findAll().forEach(e -> {
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, e.getLldpChassisIdSubType());
            assertEquals(MS02_LLDP_CHASSIS_ID, e.getLldpChassisId());
            assertEquals(MS02_NAME, e.getLldpSysname());
        });

        m_lldpLinkDao.findAll().forEach(l -> {
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpPortIdSubType());
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, l.getLldpRemChassisIdSubType());
            assertEquals(0, l.getLldpRemIndex().intValue());
            switch (l.getLldpRemLocalPortNum()) {
                case 5:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals(MS01_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
                    assertEquals("2/5", l.getLldpRemPortId());
                    assertEquals("2/4", l.getLldpPortId());
                    break;
                case 11:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals(QFX_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
                    assertEquals("ge-0/0/7", l.getLldpRemPortId());
                    assertEquals("3/4", l.getLldpPortId());
                    break;
                default:
                    fail();
            }
        });
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = MS03_IP, port = 161, resource = MS03_SNMP_RESOURCE)
    })
    public void microsensSw03Test() {

        m_nodeDao.save(builder.getMs03());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode ms03 = m_nodeDao.findByForeignId("linkd", MS03_NAME);

        assertNotNull(ms03);
        assertNotNull(ms03.getId());
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ms03.getId()));
        assertEquals(1, m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_lldpElementDao.findAll().forEach(e -> {
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, e.getLldpChassisIdSubType());
            assertEquals(MS03_LLDP_CHASSIS_ID, e.getLldpChassisId());
            assertEquals(MS03_NAME, e.getLldpSysname());
        });

        m_lldpLinkDao.findAll().forEach(l -> {
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpPortIdSubType());
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, l.getLldpRemChassisIdSubType());
            assertEquals(0, l.getLldpRemIndex().intValue());
            assertEquals(5, l.getLldpRemLocalPortNum().intValue());
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
            assertEquals(MS01_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
            assertEquals("3/5", l.getLldpRemPortId());
            assertEquals("2/4", l.getLldpPortId());
        });
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = MS04_IP, port = 161, resource = MS04_SNMP_RESOURCE)
    })
    public void microsensSw04Test() {

        m_nodeDao.save(builder.getMs04());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode ms04 = m_nodeDao.findByForeignId("linkd", MS04_NAME);

        assertNotNull(ms04);
        assertNotNull(ms04.getId());
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ms04.getId()));
        assertEquals(4, m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_lldpElementDao.findAll().forEach(e -> {
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, e.getLldpChassisIdSubType());
            assertEquals(MS04_LLDP_CHASSIS_ID, e.getLldpChassisId());
            assertEquals(MS04_NAME, e.getLldpSysname());
        });

        m_lldpLinkDao.findAll().forEach(l -> {
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpPortIdSubType());
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, l.getLldpRemChassisIdSubType());
            assertEquals(0, l.getLldpRemIndex().intValue());
            switch (l.getLldpRemLocalPortNum()) {
                case 205:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals(QFX_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
                    assertEquals("ge-0/0/13", l.getLldpRemPortId());
                    assertEquals("205", l.getLldpPortId());
                    assertEquals("Port 2/5", l.getLldpPortDescr());
                    break;
                case 301:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("e82725078343", l.getLldpRemChassisId());
                    assertEquals("e82725078343", l.getLldpRemPortId());
                    assertEquals("301", l.getLldpPortId());
                    assertEquals("Port 3/1", l.getLldpPortDescr());
                    break;
                case 302:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("b8a44fb2bb2b", l.getLldpRemChassisId());
                    assertEquals("b8a44fb2bb2b", l.getLldpRemPortId());
                    assertEquals("302", l.getLldpPortId());
                    assertEquals("Port 3/2", l.getLldpPortDescr());
                    break;
                case 303:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("e8272507969a", l.getLldpRemChassisId());
                    assertEquals("e8272507969a", l.getLldpRemPortId());
                    assertEquals("303", l.getLldpPortId());
                    assertEquals("Port 3/3", l.getLldpPortDescr());
                    break;
                default:
                    fail();
            }
        });
    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = MS08_IP, port = 161, resource = MS08_SNMP_RESOURCE)
    })
    public void microsensSw08Test() {

        m_nodeDao.save(builder.getMs08());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode ms08 = m_nodeDao.findByForeignId("linkd", MS08_NAME);

        assertNotNull(ms08);
        assertNotNull(ms08.getId());
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ms08.getId()));
        assertEquals(3, m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_lldpElementDao.findAll().forEach(e -> {
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, e.getLldpChassisIdSubType());
            assertEquals(MS08_LLDP_CHASSIS_ID, e.getLldpChassisId());
            assertEquals(MS08_NAME, e.getLldpSysname());
        });

        m_lldpLinkDao.findAll().forEach(l -> {
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpPortIdSubType());
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, l.getLldpRemChassisIdSubType());
            assertEquals(0, l.getLldpRemIndex().intValue());
            switch (l.getLldpRemLocalPortNum()) {
                case 205:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals(QFX_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
                    assertEquals("ge-0/0/15", l.getLldpRemPortId());
                    assertEquals("205", l.getLldpPortId());
                    assertEquals("Port 2/5", l.getLldpPortDescr());
                    break;
                case 301:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("e82725079663", l.getLldpRemChassisId());
                    assertEquals("e82725079663", l.getLldpRemPortId());
                    assertEquals("301", l.getLldpPortId());
                    assertEquals("Port 3/1", l.getLldpPortDescr());
                    break;
                case 302:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("e82725078327", l.getLldpRemChassisId());
                    assertEquals("e82725078327", l.getLldpRemPortId());
                    assertEquals("302", l.getLldpPortId());
                    assertEquals("Port 3/2", l.getLldpPortDescr());
                    break;
                default:
                    fail();
            }
        });

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = MS09_IP, port = 161, resource = MS09_SNMP_RESOURCE)
    })
    public void microsensSw09Test() {

        m_nodeDao.save(builder.getMs09());
        m_nodeDao.flush();

        m_linkdConfig.getConfiguration().setUseBridgeDiscovery(false);
        m_linkdConfig.getConfiguration().setUseOspfDiscovery(false);
        m_linkdConfig.getConfiguration().setUseIsisDiscovery(false);
        m_linkdConfig.getConfiguration().setUseCdpDiscovery(false);
        m_linkdConfig.getConfiguration().setUseLldpDiscovery(true);

        assertTrue(m_linkdConfig.useLldpDiscovery());
        assertFalse(m_linkdConfig.useCdpDiscovery());
        assertFalse(m_linkdConfig.useOspfDiscovery());
        assertFalse(m_linkdConfig.useBridgeDiscovery());
        assertFalse(m_linkdConfig.useIsisDiscovery());

        final OnmsNode ms09 = m_nodeDao.findByForeignId("linkd", MS09_NAME);

        assertNotNull(ms09);
        assertNotNull(ms09.getId());
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ms09.getId()));
        assertEquals(7, m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_lldpElementDao.findAll().forEach(e -> {
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, e.getLldpChassisIdSubType());
            assertEquals(MS09_LLDP_CHASSIS_ID, e.getLldpChassisId());
            assertEquals(MS09_NAME, e.getLldpSysname());
        });

        m_lldpLinkDao.findAll().forEach(l -> {
            assertNotNull(l.getLldpRemPortIdSubType());
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpPortIdSubType());
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, l.getLldpRemChassisIdSubType());
            assertEquals(0, l.getLldpRemIndex().intValue());
            switch (l.getLldpRemLocalPortNum()) {
                case 12:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals(QFX_LLDP_CHASSIS_ID, l.getLldpRemChassisId());
                    assertEquals("ge-0/0/16", l.getLldpRemPortId());
                    assertEquals("3/5", l.getLldpPortId());
                    assertEquals("Port 3/5", l.getLldpPortDescr());
                    break;
                case 1:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("accc8ef9c0a3", l.getLldpRemChassisId());
                    assertEquals("accc8ef9c0a3", l.getLldpRemPortId());
                    assertEquals("1/1", l.getLldpPortId());
                    assertEquals("Port 1/1", l.getLldpPortDescr());
                    break;
                case 2:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("accc8e53f5fb", l.getLldpRemChassisId());
                    assertEquals("accc8e53f5fb", l.getLldpRemPortId());
                    assertEquals("2/1", l.getLldpPortId());
                    assertEquals("Port 2/1", l.getLldpPortDescr());
                    break;
                case 3:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("accc8e536f3c", l.getLldpRemChassisId());
                    assertEquals("accc8e536f3c", l.getLldpRemPortId());
                    assertEquals("2/2", l.getLldpPortId());
                    assertEquals("Port 2/2", l.getLldpPortDescr());
                    break;
                case 7:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("accc8eaaeb7f", l.getLldpRemChassisId());
                    assertEquals("accc8eaaeb7f", l.getLldpRemPortId());
                    assertEquals("2/6", l.getLldpPortId());
                    assertEquals("Port 2/6", l.getLldpPortDescr());
                    break;
                case 8:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("accc8ef9c09e", l.getLldpRemChassisId());
                    assertEquals("accc8ef9c09e", l.getLldpRemPortId());
                    assertEquals("3/1", l.getLldpPortId());
                    assertEquals("Port 3/1", l.getLldpPortDescr());
                    break;
                case 9:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals("accc8e59d145", l.getLldpRemChassisId());
                    assertEquals("eth0", l.getLldpRemPortId());
                    assertEquals("3/2", l.getLldpPortId());
                    assertEquals("Port 3/2", l.getLldpPortDescr());
                    break;
                default:
                    fail();
            }
        });

    }

}
