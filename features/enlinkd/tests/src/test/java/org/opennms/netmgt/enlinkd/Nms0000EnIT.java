package org.opennms.netmgt.enlinkd;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.nb.Nms0000NetworkBuilder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.opennms.netmgt.nb.Nms0000NetworkBuilder.*;


public class Nms0000EnIT extends EnLinkdBuilderITCase {

    Nms0000NetworkBuilder builder = new Nms0000NetworkBuilder();

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = MS01_IP, port = 161, resource = MS01_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS02_IP, port = 161, resource = MS02_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS03_IP, port = 161, resource = MS03_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS04_IP, port = 161, resource = MS04_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS05_IP, port = 161, resource = MS05_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS06_IP, port = 161, resource = MS06_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS07_IP, port = 161, resource = MS07_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS08_IP, port = 161, resource = MS08_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS09_IP, port = 161, resource = MS09_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS10_IP, port = 161, resource = MS10_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS11_IP, port = 161, resource = MS11_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS12_IP, port = 161, resource = MS12_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS14_IP, port = 161, resource = MS14_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS15_IP, port = 161, resource = MS15_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS16_IP, port = 161, resource = MS16_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS17_IP, port = 161, resource = MS17_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS18_IP, port = 161, resource = MS18_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS19_IP, port = 161, resource = MS19_SNMP_RESOURCE)
    })
    public void networkAllTest() throws InterruptedException {
        m_nodeDao.save(builder.getMs01());
        m_nodeDao.save(builder.getMs02());
        m_nodeDao.save(builder.getMs03());
        m_nodeDao.save(builder.getMs04());
        m_nodeDao.save(builder.getMs05());
        m_nodeDao.save(builder.getMs06());
        m_nodeDao.save(builder.getMs07());
        m_nodeDao.save(builder.getMs08());
        m_nodeDao.save(builder.getMs09());
        m_nodeDao.save(builder.getMs10());
        m_nodeDao.save(builder.getMs11());
        m_nodeDao.save(builder.getMs12());
        m_nodeDao.save(builder.getMs14());
        m_nodeDao.save(builder.getMs15());
        m_nodeDao.save(builder.getMs16());
        m_nodeDao.save(builder.getMs17());
        m_nodeDao.save(builder.getMs18());
        m_nodeDao.save(builder.getMs19());
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
        final OnmsNode ms05 = m_nodeDao.findByForeignId("linkd", MS05_NAME);
        final OnmsNode ms06 = m_nodeDao.findByForeignId("linkd", MS06_NAME);
        final OnmsNode ms07 = m_nodeDao.findByForeignId("linkd", MS07_NAME);
        final OnmsNode ms08 = m_nodeDao.findByForeignId("linkd", MS08_NAME);
        final OnmsNode ms09 = m_nodeDao.findByForeignId("linkd", MS09_NAME);
        final OnmsNode ms10 = m_nodeDao.findByForeignId("linkd", MS10_NAME);
        final OnmsNode ms11 = m_nodeDao.findByForeignId("linkd", MS11_NAME);
        final OnmsNode ms12 = m_nodeDao.findByForeignId("linkd", MS12_NAME);
        final OnmsNode ms14 = m_nodeDao.findByForeignId("linkd", MS14_NAME);
        final OnmsNode ms15 = m_nodeDao.findByForeignId("linkd", MS15_NAME);
        final OnmsNode ms16 = m_nodeDao.findByForeignId("linkd", MS16_NAME);
        final OnmsNode ms17 = m_nodeDao.findByForeignId("linkd", MS17_NAME);
        final OnmsNode ms18 = m_nodeDao.findByForeignId("linkd", MS18_NAME);
        final OnmsNode ms19 = m_nodeDao.findByForeignId("linkd", MS19_NAME);

        assertNotNull(ms01);
        assertNotNull(ms02);
        assertNotNull(ms03);
        assertNotNull(ms04);
        assertNotNull(ms05);
        assertNotNull(ms06);
        assertNotNull(ms07);
        assertNotNull(ms08);
        assertNotNull(ms09);
        assertNotNull(ms10);
        assertNotNull(ms11);
        assertNotNull(ms12);
        assertNotNull(ms14);
        assertNotNull(ms15);
        assertNotNull(ms16);
        assertNotNull(ms17);
        assertNotNull(ms18);
        assertNotNull(ms19);
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ms01.getId()));
        assertEquals(4,m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms02.getId()));
        assertEquals(8,m_lldpLinkDao.countAll());
        assertEquals(2,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms03.getId()));
        assertEquals(11,m_lldpLinkDao.countAll());
        assertEquals(3,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms04.getId()));
        assertEquals(15,m_lldpLinkDao.countAll());
        assertEquals(4,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms05.getId()));
        assertEquals(19,m_lldpLinkDao.countAll());
        assertEquals(5,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms06.getId()));
        assertEquals(23,m_lldpLinkDao.countAll());
        assertEquals(6,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms07.getId()));
        assertEquals(26,m_lldpLinkDao.countAll());
        assertEquals(7,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms08.getId()));
        assertEquals(31,m_lldpLinkDao.countAll());
        assertEquals(8,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms09.getId()));
        assertEquals(36,m_lldpLinkDao.countAll());
        assertEquals(9,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms10.getId()));
        assertEquals(41,m_lldpLinkDao.countAll());
        assertEquals(10,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms11.getId()));
        assertEquals(45,m_lldpLinkDao.countAll());
        assertEquals(11,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms12.getId()));
        assertEquals(51,m_lldpLinkDao.countAll());
        assertEquals(12,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms14.getId()));
        assertEquals(55,m_lldpLinkDao.countAll());
        assertEquals(13,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms15.getId()));
        assertEquals(56,m_lldpLinkDao.countAll());
        assertEquals(14,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms16.getId()));
        assertEquals(61,m_lldpLinkDao.countAll());
        assertEquals(15,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms17.getId()));
        assertEquals(65,m_lldpLinkDao.countAll());
        assertEquals(16,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms18.getId()));
        assertEquals(70,m_lldpLinkDao.countAll());
        assertEquals(17,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms19.getId()));
        assertEquals(73,m_lldpLinkDao.countAll());
        assertEquals(18,m_lldpElementDao.countAll());

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_linkd.forceTopologyUpdaterRun(ProtocolSupported.LLDP);
        m_linkd.runTopologyUpdater(ProtocolSupported.LLDP);

        LldpOnmsTopologyUpdater topologyUpdater = m_linkd.getLldpTopologyUpdater();

        OnmsTopology topology = topologyUpdater.getTopology();

        assertNotNull(topology);
        printOnmsTopology(topology);

        assertEquals(18,topology.getVertices().size());
        assertEquals(17,topology.getEdges().size());

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = MS07_IP, port = 161, resource = MS07_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS08_IP, port = 161, resource = MS08_SNMP_RESOURCE)
    })
    public void networkTwoConnectedNodeTest() throws InterruptedException {
        m_nodeDao.save(builder.getMs07());
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

        final OnmsNode ms07 = m_nodeDao.findByForeignId("linkd", MS07_NAME);
        final OnmsNode ms08 = m_nodeDao.findByForeignId("linkd", MS08_NAME);

        assertNotNull(ms07);
        assertNotNull(ms08);
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ms07.getId()));
        assertEquals(3,m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms08.getId()));
        assertEquals(8,m_lldpLinkDao.countAll());
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
            @JUnitSnmpAgent(host = MS08_IP, port = 161, resource = MS08_SNMP_RESOURCE),
            @JUnitSnmpAgent(host = MS10_IP, port = 161, resource = MS10_SNMP_RESOURCE)
    })
    public void networkThreeConnectedNodeTest() throws InterruptedException {
        m_nodeDao.save(builder.getMs08());
        m_nodeDao.save(builder.getMs09());
        m_nodeDao.save(builder.getMs10());
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
        final OnmsNode ms09 = m_nodeDao.findByForeignId("linkd", MS09_NAME);
        final OnmsNode ms10 = m_nodeDao.findByForeignId("linkd", MS10_NAME);

        assertNotNull(ms08);
        assertNotNull(ms09);
        assertNotNull(ms10);
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ms08.getId()));
        assertEquals(5,m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());
        Thread.sleep(200);
        assertTrue(m_linkd.runSingleSnmpCollection(ms10.getId()));
        assertEquals(10,m_lldpLinkDao.countAll());
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
            @JUnitSnmpAgent(host = MS08_IP, port = 161, resource = MS08_SNMP_RESOURCE)
    })
    public void microsenseTest() {

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
        assertEquals(5, m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_lldpElementDao.findAll().forEach(e -> {
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, e.getLldpChassisIdSubType());
            assertEquals("0060a70a7f6c", e.getLldpChassisId());
            assertEquals("SW_A1_BDA_08_M", e.getLldpSysname());
        });

        m_lldpLinkDao.findAll().forEach(l -> {
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpPortIdSubType());
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, l.getLldpRemChassisIdSubType());
            assertEquals(0, l.getLldpRemIndex().intValue());
            switch (l.getLldpRemLocalPortNum()) {
                case 5:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals("0060a70a7f26", l.getLldpRemChassisId());
                    assertEquals("2/5", l.getLldpRemPortId());
                    assertEquals("2/4", l.getLldpPortId());
                    break;
                case 7:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("accc8eef78f9", l.getLldpRemChassisId());
                    assertEquals("accc8eef78f9", l.getLldpRemPortId());
                    assertEquals("2/6", l.getLldpPortId());
                    break;
                case 8:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("accc8eef78e4", l.getLldpRemChassisId());
                    assertEquals("accc8eef78e4", l.getLldpRemPortId());
                    assertEquals("3/1", l.getLldpPortId());
                    break;
                case 9:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS, l.getLldpRemPortIdSubType());
                    assertEquals("b8a44f502ddd", l.getLldpRemChassisId());
                    assertEquals("b8a44f502ddd", l.getLldpRemPortId());
                    assertEquals("3/2", l.getLldpPortId());
                    break;
                case 11:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME, l.getLldpRemPortIdSubType());
                    assertEquals("0060a70a7f10", l.getLldpRemChassisId());
                    assertEquals("3/5", l.getLldpRemPortId());
                    assertEquals("3/4", l.getLldpPortId());
                    break;
                default:
                    fail();
            }
        });

    }

    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = MS16_IP, port = 161, resource = MS16_SNMP_RESOURCE)
    })
    public void ms16Test() {

        m_nodeDao.save(builder.getMs16());
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

        final OnmsNode ms16 = m_nodeDao.findByForeignId("linkd", MS16_NAME);

        assertNotNull(ms16);
        assertNotNull(ms16.getId());
        m_linkd.reload();

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(ms16.getId()));
        assertEquals(5, m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

    }


    @Test
    @JUnitSnmpAgents(value={
            @JUnitSnmpAgent(host = PLANET_IP, port = 161, resource = PLANET_SNMP_RESOURCE)
    })
    public void planetTest() {

        m_nodeDao.save(builder.getPlanet());
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

        final OnmsNode planet = m_nodeDao.findByForeignId("linkd", PLANET_NAME);

        assertEquals(0,m_lldpLinkDao.countAll());
        assertEquals(0,m_lldpElementDao.countAll());

        assertTrue(m_linkd.runSingleSnmpCollection(planet.getId()));
        assertEquals(3, m_lldpLinkDao.countAll());
        assertEquals(1,m_lldpElementDao.countAll());

        m_lldpElementDao.findAll().forEach(EnLinkdTestHelper::printLldpElement);
        m_lldpLinkDao.findAll().forEach(EnLinkdTestHelper::printLldpLink);

        m_lldpElementDao.findAll().forEach(l -> {
            assertEquals("a8f7e06c3bd8",l.getLldpChassisId());
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, l.getLldpChassisIdSubType());
            assertEquals("V177", l.getLldpSysname());
        });

        m_lldpLinkDao.findAll().forEach(l -> {
            assertEquals(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS, l.getLldpRemChassisIdSubType());
            assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL,l.getLldpPortIdSubType());
            System.err.println("-------->lldplocalportnum" +l.getLldpRemLocalPortNum());
            switch (l.getLldpRemLocalPortNum()) {
                case 6:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS,l.getLldpRemPortIdSubType());
                    assertEquals("0004566f821e",l.getLldpRemPortId());
                    break;
                case 10:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME,l.getLldpRemPortIdSubType());
                    assertEquals("2/5",l.getLldpRemPortId());
                    break;
                case 9:
                    assertEquals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL,l.getLldpRemPortIdSubType());
                    assertEquals("9",l.getLldpRemPortId());
                    break;
                default:
                    fail();

            }
        });

    }
}
