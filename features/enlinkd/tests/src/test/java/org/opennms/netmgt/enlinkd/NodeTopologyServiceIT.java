/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.SubNetwork;
import org.opennms.netmgt.nb.Nms0001NetworkBuilder;
import org.opennms.netmgt.nb.Nms0002NetworkBuilder;
import org.opennms.netmgt.nb.Nms003NetworkBuilder;
import org.opennms.netmgt.nb.Nms007NetworkBuilder;
import org.opennms.netmgt.nb.Nms0123NetworkBuilder;
import org.opennms.netmgt.nb.Nms101NetworkBuilder;
import org.opennms.netmgt.nb.Nms10205aNetworkBuilder;
import org.opennms.netmgt.nb.Nms10205bNetworkBuilder;
import org.opennms.netmgt.nb.Nms102NetworkBuilder;
import org.opennms.netmgt.nb.Nms1055NetworkBuilder;
import org.opennms.netmgt.nb.Nms13593NetworkBuilder;
import org.opennms.netmgt.nb.Nms13637NetworkBuilder;
import org.opennms.netmgt.nb.Nms13923NetworkBuilder;
import org.opennms.netmgt.nb.Nms17216NetworkBuilder;
import org.opennms.netmgt.nb.Nms4005NetworkBuilder;
import org.opennms.netmgt.nb.Nms4930NetworkBuilder;
import org.opennms.netmgt.nb.Nms6802NetworkBuilder;
import org.opennms.netmgt.nb.Nms7467NetworkBuilder;
import org.opennms.netmgt.nb.Nms7563NetworkBuilder;
import org.opennms.netmgt.nb.Nms7777DWNetworkBuilder;
import org.opennms.netmgt.nb.Nms7918NetworkBuilder;
import org.opennms.netmgt.nb.Nms8000NetworkBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeTopologyServiceIT extends EnLinkdBuilderITCase {

    private final static Logger LOG = LoggerFactory.getLogger(NodeTopologyServiceIT.class);

    @Test
    public void nms0001SubnetworksTest() {
        final Nms0001NetworkBuilder builder = new Nms0001NetworkBuilder();
        m_nodeDao.save(builder.getFroh());
        m_nodeDao.save(builder.getOedipus());
        m_nodeDao.save(builder.getSiegFrie());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(3));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(50));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        System.err.println("---All Subnetwork");
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(36));

        final Set<SubNetwork> ptpsubnets = m_nodeTopologyService.findAllPointToPointSubNetwork();
        System.err.println("---All PointToPointSubnetwork");
        ptpsubnets.forEach(System.err::println);
        assertThat(ptpsubnets, hasSize(21));

        final Set<SubNetwork> ptpLegalsubnets = m_nodeTopologyService.findAllLegalPointToPointSubNetwork();
        System.err.println("---All Legal PointToPointSubnetwork");
        ptpLegalsubnets.forEach(System.err::println);
        assertThat(ptpLegalsubnets, hasSize(3));

        final Set<SubNetwork> lpbsubnets = m_nodeTopologyService.findAllLoopbacks();
        System.err.println("---All Loopbacks ");
        lpbsubnets.forEach(System.err::println);
        assertThat(lpbsubnets,hasSize(6));

        final Set<SubNetwork> lpblegalsubnets = m_nodeTopologyService.findAllLegalLoopbacks();
        System.err.println("---All Legal Loopbacks ");
        lpblegalsubnets.forEach(System.err::println);
        assertThat(lpblegalsubnets,hasSize(5));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        System.err.println("---All Legal Subnetwork");
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(4));

        System.err.println("---Priority Map ");
        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertThat(priorityMap.keySet(),hasSize(3));
        priorityMap.values().forEach(v -> assertEquals(0,v.intValue()));
    }

    @Test
    public void nms0002SubnetworksTest() {
        final Nms0002NetworkBuilder builder = new Nms0002NetworkBuilder();
        m_nodeDao.save(builder.getRluck001());
        m_nodeDao.save(builder.getSluck001());
        m_nodeDao.save(builder.getRPict001());
        m_nodeDao.save(builder.getRNewt103());
        m_nodeDao.save(builder.getRDeEssnBrue());
        m_nodeDao.save(builder.getSDeEssnBrue081());
        m_nodeDao.save(builder.getSDeEssnBrue121());
        m_nodeDao.save(builder.getSDeEssnBrue142());
        m_nodeDao.save(builder.getSDeEssnBrue165());
        m_nodeDao.save(builder.getRSeMalmNobe013());
        m_nodeDao.save(builder.getSSeMalmNobe561());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(11));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        assertThat(ips, hasSize(148));
        ips.forEach(System.err::println);

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        System.err.println("---All Subnetwork " + subnets.size());
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(115));

        final Set<SubNetwork> ptpsubnets = m_nodeTopologyService.findAllPointToPointSubNetwork();
        System.err.println("---All PointToPointSubnetwork " + ptpsubnets.size());
        ptpsubnets.forEach(System.err::println);
        assertThat(ptpsubnets, hasSize(7));

        final Set<SubNetwork> ptpLegalsubnets = m_nodeTopologyService.findAllLegalPointToPointSubNetwork();
        System.err.println("---All Legal PointToPointSubnetwork " + ptpLegalsubnets.size());
        ptpLegalsubnets.forEach(System.err::println);
        assertThat(ptpLegalsubnets, hasSize(0));

        final Set<SubNetwork> lpbsubnets = m_nodeTopologyService.findAllLoopbacks();
        System.err.println("---All Loopbacks " + lpbsubnets.size());
        lpbsubnets.forEach(System.err::println);
        assertThat(lpbsubnets,hasSize(12));

        final Set<SubNetwork> lpblegalsubnets = m_nodeTopologyService.findAllLegalLoopbacks();
        System.err.println("---All Legal Loopbacks " + lpblegalsubnets.size());
        lpblegalsubnets.forEach(System.err::println);
        assertThat(lpblegalsubnets,hasSize(12));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        System.err.println("---All Legal Subnetwork " + legalsubnets.size());
        legalsubnets.forEach(System.err::println);
        int n = 0;
        assertThat(legalsubnets, hasSize(3));
        for (SubNetwork subnet: legalsubnets) {
            n = n + subnet.getNodeIds().size();
        }
        assertEquals(9,n);

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        assertEquals(9,priorityMap.size());
        LOG.info("{}", priorityMap);
    }

    @Test
    public void nms003SubnetworkTests() {
        final Nms003NetworkBuilder builder = new Nms003NetworkBuilder();
        m_nodeDao.save(builder.getSwitch1());
        m_nodeDao.save(builder.getSwitch2());
        m_nodeDao.save(builder.getSwitch3());
        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(3));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        assertThat(ips, hasSize(7));
        ips.forEach(System.err::println);

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllLegalSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(1));
        assertThat(subnets.iterator().next().getNodeIds(), hasSize(3));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        assertEquals(3,priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, v.intValue()));
        LOG.info("{}", priorityMap);
    }

    @Test
    public void nms007SubnetworkTest() {
        final Nms007NetworkBuilder builder = new Nms007NetworkBuilder();
        m_nodeDao.save(builder.getFireFly170());
        m_nodeDao.save(builder.getFireFly171());
        m_nodeDao.save(builder.getFireFly172());
        m_nodeDao.save(builder.getFireFly173());
        m_nodeDao.save(builder.getFireFly174());
        m_nodeDao.save(builder.getFireFly175());
        m_nodeDao.save(builder.getFireFly176());
        m_nodeDao.save(builder.getFireFly177());
        m_nodeDao.save(builder.getFireFly189());
        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(9));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        assertThat(ips, hasSize(96));
        ips.forEach(System.err::println);

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        System.err.println("---All Subnetworks---");
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(24));

        assertThat(m_nodeTopologyService.findAllPointToPointSubNetwork(),hasSize(0));
        assertThat(m_nodeTopologyService.findAllLegalPointToPointSubNetwork(),hasSize(0));

        final Set<SubNetwork> lpsubnets = m_nodeTopologyService.findAllLoopbacks();
        System.err.println("---All Loopbacks---" + lpsubnets.size());
        lpsubnets.forEach(System.err::println);
        assertThat(lpsubnets, hasSize(17));

        final Set<SubNetwork> lplegalsubnets = m_nodeTopologyService.findAllLegalLoopbacks();
        System.err.println("---All Legal Loopbacks---" + lplegalsubnets.size());
        lplegalsubnets.forEach(System.err::println);
        assertThat(lplegalsubnets, hasSize(9));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        System.err.println("---All Legal Subnetworks---");
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(6));


        final Set<Integer> nodeids = new HashSet<>();
        legalsubnets.forEach(lsn -> nodeids.addAll(lsn.getNodeIds()));
        assertThat(nodeids, hasSize(9));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        assertEquals(9, priorityMap.size());
        priorityMap.values().forEach(v -> assertTrue(v <6));
        LOG.info("{}", priorityMap);

    }

    @Test
    public void nms101SubnetworksTest() {
        final Nms101NetworkBuilder builder = new Nms101NetworkBuilder();
        m_nodeDao.save(builder.getCisco1700());
        m_nodeDao.save(builder.getCisco1700b());
        m_nodeDao.save(builder.getCisco2691());
        m_nodeDao.save(builder.getCisco3600());
        m_nodeDao.save(builder.getCisco3700());
        m_nodeDao.save(builder.getCisco7200a());
        m_nodeDao.save(builder.getCisco7200b());
        m_nodeDao.save(builder.getLaptop());
        m_nodeDao.save(builder.getExampleCom());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(9));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(20));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(11));

        final Set<SubNetwork> ptpsubnets = m_nodeTopologyService.findAllPointToPointSubNetwork();
        System.err.println("---All PointToPointSubnetwork " + ptpsubnets.size());
        ptpsubnets.forEach(System.err::println);
        assertThat(ptpsubnets, hasSize(0));

        final Set<SubNetwork> ptpLegalsubnets = m_nodeTopologyService.findAllLegalPointToPointSubNetwork();
        System.err.println("---All Legal PointToPointSubnetwork " + ptpLegalsubnets.size());
        ptpLegalsubnets.forEach(System.err::println);
        assertThat(ptpLegalsubnets, hasSize(0));

        final Set<SubNetwork> lpbsubnets = m_nodeTopologyService.findAllLoopbacks();
        System.err.println("---All Loopbacks " + lpbsubnets.size());
        lpbsubnets.forEach(System.err::println);
        assertThat(lpbsubnets,hasSize(0));

        final Set<SubNetwork> lpblegalsubnets = m_nodeTopologyService.findAllLegalLoopbacks();
        System.err.println("---All Legal Loopbacks " + lpblegalsubnets.size());
        lpblegalsubnets.forEach(System.err::println);
        assertThat(lpblegalsubnets,hasSize(0));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(6));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        assertEquals(6, priorityMap.size());
        priorityMap.values().forEach(v -> assertTrue(v <6));
        LOG.info("{}", priorityMap);

    }

    @Test
    public void nms102SubnetworksTest() {
        final Nms102NetworkBuilder builder = new Nms102NetworkBuilder();
        m_nodeDao.save(builder.getMikrotik());
        m_nodeDao.save(builder.getSamsung());
        m_nodeDao.save(builder.getMac1());
        m_nodeDao.save(builder.getMac2());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(4));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(5));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(2));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(1));

        assertThat(legalsubnets.iterator().next().getNodeIds(),hasSize(4));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        assertEquals(4, priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, v.intValue()));
        LOG.info("{}", priorityMap);

    }

    @Test
    public void nms0123SubnetworksTest() {
        final Nms0123NetworkBuilder builder = new Nms0123NetworkBuilder();
        m_nodeDao.save(builder.getItpn0111());
        m_nodeDao.save(builder.getItpn0112());
        m_nodeDao.save(builder.getItpn0113());
        m_nodeDao.save(builder.getItpn0114());
        m_nodeDao.save(builder.getItpn0121());
        m_nodeDao.save(builder.getItpn0123());
        m_nodeDao.save(builder.getItpn0201());
        m_nodeDao.save(builder.getItpn0202());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(8));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        assertThat(ips, hasSize(8));
        ips.forEach(System.err::println);

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllLegalSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(1));
        assertEquals(8,subnets.iterator().next().getNodeIds().size());

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        assertEquals(8, priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, v.intValue()));
        LOG.info("{}", priorityMap);

    }

    @Test
    public void nms1055SubnetworksTest() {
        final Nms1055NetworkBuilder builder =new Nms1055NetworkBuilder();
        m_nodeDao.save(builder.getAustin());
        m_nodeDao.save(builder.getDelaware());
        m_nodeDao.save(builder.getPenrose());
        m_nodeDao.save(builder.getPhoenix());
        m_nodeDao.save(builder.getSanjose());
        m_nodeDao.save(builder.getRiovista());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(6));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        System.err.println("---");
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(80));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(43));

        final Set<SubNetwork> ptpsubnets = m_nodeTopologyService.findAllPointToPointSubNetwork();
        System.err.println("---All PointToPointSubnetwork " + ptpsubnets.size());
        ptpsubnets.forEach(System.err::println);
        assertThat(ptpsubnets, hasSize(7));

        final Set<SubNetwork> ptpLegalsubnets = m_nodeTopologyService.findAllLegalPointToPointSubNetwork();
        System.err.println("---All Legal PointToPointSubnetwork " + ptpLegalsubnets.size());
        ptpLegalsubnets.forEach(System.err::println);
        assertThat(ptpLegalsubnets, hasSize(6));

        final Set<SubNetwork> lpbsubnets = m_nodeTopologyService.findAllLoopbacks();
        System.err.println("---All Loopbacks " + lpbsubnets.size());
        lpbsubnets.forEach(System.err::println);
        assertThat(lpbsubnets,hasSize(31));

        final Set<SubNetwork> lpblegalsubnets = m_nodeTopologyService.findAllLegalLoopbacks();
        System.err.println("---All Legal Loopbacks " + lpblegalsubnets.size());
        lpblegalsubnets.forEach(System.err::println);
        assertThat(lpblegalsubnets,hasSize(28));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        System.err.println("---");
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(7));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(6, priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, v.intValue()));

    }

    @Test
    public void nms4005SubnetworksTest() {
        final Nms4005NetworkBuilder builder = new Nms4005NetworkBuilder();
        m_nodeDao.save(builder.getR1());
        m_nodeDao.save(builder.getR2());
        m_nodeDao.save(builder.getR3());
        m_nodeDao.save(builder.getR4());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(4));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(9));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(5));

        final Set<SubNetwork> ptpsubnets = m_nodeTopologyService.findAllPointToPointSubNetwork();
        System.err.println("---All PointToPointSubnetwork " + ptpsubnets.size());
        ptpsubnets.forEach(System.err::println);
        assertThat(ptpsubnets, hasSize(0));

        final Set<SubNetwork> ptpLegalsubnets = m_nodeTopologyService.findAllLegalPointToPointSubNetwork();
        System.err.println("---All Legal PointToPointSubnetwork " + ptpLegalsubnets.size());
        ptpLegalsubnets.forEach(System.err::println);
        assertThat(ptpLegalsubnets, hasSize(0));

        final Set<SubNetwork> lpbsubnets = m_nodeTopologyService.findAllLoopbacks();
        System.err.println("---All Loopbacks " + lpbsubnets.size());
        lpbsubnets.forEach(System.err::println);
        assertThat(lpbsubnets,hasSize(0));

        final Set<SubNetwork> lpblegalsubnets = m_nodeTopologyService.findAllLegalLoopbacks();
        System.err.println("---All Legal Loopbacks " + lpblegalsubnets.size());
        lpblegalsubnets.forEach(System.err::println);
        assertThat(lpblegalsubnets,hasSize(0));

        System.err.println("---");
        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(4));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(4, priorityMap.size());
        priorityMap.values().forEach(v -> assertTrue(v < 3));

    }

    @Test
    public void nms4930SubnetworksTest() {
        final Nms4930NetworkBuilder builder = new Nms4930NetworkBuilder();
        m_nodeDao.save(builder.getDlink1());
        m_nodeDao.save(builder.getDlink2());
        m_nodeDao.save(builder.getHost1());
        m_nodeDao.save(builder.getHost2());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(4));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(7));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(4));

        final Set<SubNetwork> ptpsubnets = m_nodeTopologyService.findAllPointToPointSubNetwork();
        System.err.println("---All PointToPointSubnetwork " + ptpsubnets.size());
        ptpsubnets.forEach(System.err::println);
        assertThat(ptpsubnets, hasSize(0));

        final Set<SubNetwork> ptpLegalsubnets = m_nodeTopologyService.findAllLegalPointToPointSubNetwork();
        System.err.println("---All Legal PointToPointSubnetwork " + ptpLegalsubnets.size());
        ptpLegalsubnets.forEach(System.err::println);
        assertThat(ptpLegalsubnets, hasSize(0));

        final Set<SubNetwork> lpbsubnets = m_nodeTopologyService.findAllLoopbacks();
        System.err.println("---All Loopbacks " + lpbsubnets.size());
        lpbsubnets.forEach(System.err::println);
        assertThat(lpbsubnets,hasSize(0));

        final Set<SubNetwork> lpblegalsubnets = m_nodeTopologyService.findAllLegalLoopbacks();
        System.err.println("---All Legal Loopbacks " + lpblegalsubnets.size());
        lpblegalsubnets.forEach(System.err::println);
        assertThat(lpblegalsubnets,hasSize(0));

        System.err.println("---");
        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(1));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(4, priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, v.intValue()));

    }

    @Test
    public void nms6802SubnetworksTest() {
        final Nms6802NetworkBuilder builder = new Nms6802NetworkBuilder();
        m_nodeDao.save(builder.getCiscoIosXrRouter());
        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(1));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(1));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(0));

    }

    @Test
    public void nms7467SubnetworksTest() {
        final Nms7467NetworkBuilder builder = new Nms7467NetworkBuilder();
        m_nodeDao.save(builder.getCiscoC870());
        m_nodeDao.save(builder.getDarwin108());
        m_nodeDao.save(builder.getLinuxUbuntu());
        m_nodeDao.save(builder.getCiscoWsC2948());
        m_nodeDao.save(builder.getNetGearSw108());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(5));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(26));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(8));

        System.err.println("---");
        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(1));
        assertThat(legalsubnets.iterator().next().getNodeIds(),hasSize(5));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(5, priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, v.intValue()));

    }

    @Test
    public void nms7563SubnetworksTest() {
        final Nms7563NetworkBuilder builder = new Nms7563NetworkBuilder();
        m_nodeDao.save(builder.getCisco01());
        m_nodeDao.save(builder.getHomeServer());
        m_nodeDao.save(builder.getSwitch02());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(3));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(5));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(3));
        System.err.println("---");

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(1));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(3, priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, v.intValue()));

    }

    @Test
    public void nms7777DWSubnetworksTest() {
        final Nms7777DWNetworkBuilder builder = new Nms7777DWNetworkBuilder();
        m_nodeDao.save(builder.getDragonWaveRouter());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(1));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(1));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(1));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(0));
    }

    @Test
    public void nms7918SubnetworksTest() {
        Nms7918NetworkBuilder builder = new Nms7918NetworkBuilder();

        m_nodeDao.save(builder.getAsw01());
        m_nodeDao.save(builder.getOspwl01());
        m_nodeDao.save(builder.getPe01());
        m_nodeDao.save(builder.getSamasw01());
        m_nodeDao.save(builder.getStcasw01());
        m_nodeDao.save(builder.getOspss01());
        m_nodeDao.flush();

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(6));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(6));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(1));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        assertThat(legalsubnets, hasSize(1));
        assertThat(legalsubnets.iterator().next().getNodeIds(), hasSize(6));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(6, priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, v.intValue()));


    }

    @Test
    public void nms8000SubbnetworksTest() {
        final Nms8000NetworkBuilder builder = new Nms8000NetworkBuilder();

        m_nodeDao.save(builder.getNMMR1());
        m_nodeDao.save(builder.getNMMR2());
        m_nodeDao.save(builder.getNMMR3());
        m_nodeDao.save(builder.getNMMSW1());
        m_nodeDao.save(builder.getNMMSW2());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(5));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(12));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(6));

        System.err.println("---");
        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(4));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(5, priorityMap.size());
        priorityMap.values().forEach(v -> assertTrue(v < 3));


    }

    @Test
    public void nms10205aSubnetworksTest() {
        final Nms10205aNetworkBuilder builder = new Nms10205aNetworkBuilder();

        m_nodeDao.save(builder.getBagmane());
        m_nodeDao.save(builder.getMumbai());
        m_nodeDao.save(builder.getMysore());
        m_nodeDao.save(builder.getBangalore());
        m_nodeDao.save(builder.getChennai());
        m_nodeDao.save(builder.getDelhi());
        m_nodeDao.save(builder.getJ635041());
        m_nodeDao.save(builder.getJ635042());
        m_nodeDao.save(builder.getSpaceExSw1());
        m_nodeDao.save(builder.getSpaceExSw2());
        m_nodeDao.save(builder.getSRX100());
        m_nodeDao.save(builder.getSGG550());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(12));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(108));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(42));
        System.err.println("---");

        final Set<SubNetwork> ptpsubnets = m_nodeTopologyService.findAllPointToPointSubNetwork();
        System.err.println("---All PointToPointSubnetwork " + ptpsubnets.size());
        ptpsubnets.forEach(System.err::println);
        assertThat(ptpsubnets, hasSize(17));

        final Set<SubNetwork> ptpLegalsubnets = m_nodeTopologyService.findAllLegalPointToPointSubNetwork();
        System.err.println("---All Legal PointToPointSubnetwork " + ptpLegalsubnets.size());
        ptpLegalsubnets.forEach(System.err::println);
        assertThat(ptpLegalsubnets, hasSize(15));

        final Set<SubNetwork> lpbsubnets = m_nodeTopologyService.findAllLoopbacks();
        System.err.println("---All Loopbacks " + lpbsubnets.size());
        lpbsubnets.forEach(System.err::println);
        assertThat(lpbsubnets,hasSize(14));

        final Set<SubNetwork> lpblegalsubnets = m_nodeTopologyService.findAllLegalLoopbacks();
        System.err.println("---All Legal Loopbacks " + lpblegalsubnets.size());
        lpblegalsubnets.forEach(System.err::println);
        assertThat(lpblegalsubnets,hasSize(9));

        System.err.println("---All Legal ");
        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(16));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(12, priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, (int) v));


    }

    @Test
    public void nms10205bSubnetworksTest() {
        final Nms10205bNetworkBuilder builder = new Nms10205bNetworkBuilder();

        m_nodeDao.save(builder.getBagmane());
        m_nodeDao.save(builder.getMumbai());
        m_nodeDao.save(builder.getMysore());
        m_nodeDao.save(builder.getBangalore());
        m_nodeDao.save(builder.getDelhi());
        m_nodeDao.save(builder.getJ635042());
        m_nodeDao.save(builder.getSpaceExSw1());
        m_nodeDao.save(builder.getSpaceExSw2());
        m_nodeDao.save(builder.getSRX100());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(9));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(92));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        System.err.println("---");
        assertThat(subnets, hasSize(39));

        final Set<SubNetwork> ptpsubnets = m_nodeTopologyService.findAllPointToPointSubNetwork();
        System.err.println("---All PointToPointSubnetwork " + ptpsubnets.size());
        ptpsubnets.forEach(System.err::println);
        assertThat(ptpsubnets, hasSize(17));

        final Set<SubNetwork> ptpLegalsubnets = m_nodeTopologyService.findAllLegalPointToPointSubNetwork();
        System.err.println("---All Legal PointToPointSubnetwork " + ptpLegalsubnets.size());
        ptpLegalsubnets.forEach(System.err::println);
        assertThat(ptpLegalsubnets, hasSize(13));

        final Set<SubNetwork> lpbsubnets = m_nodeTopologyService.findAllLoopbacks();
        System.err.println("---All Loopbacks " + lpbsubnets.size());
        lpbsubnets.forEach(System.err::println);
        assertThat(lpbsubnets,hasSize(14));

        final Set<SubNetwork> lpblegalsubnets = m_nodeTopologyService.findAllLegalLoopbacks();
        System.err.println("---All Legal Loopbacks " + lpblegalsubnets.size());
        lpblegalsubnets.forEach(System.err::println);
        assertThat(lpblegalsubnets,hasSize(9));

        System.err.println("---All Legal ");
        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(14));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(9, priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, (int) v));

    }

    @Test
    public void nms13593SubnetworksTest() {
        final Nms13593NetworkBuilder builder = new Nms13593NetworkBuilder();

        m_nodeDao.save(builder.getZHBGO1Zsr001());
        m_nodeDao.save(builder.getZHBGO1Zsr002());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(2));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(2));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(1));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        assertThat(legalsubnets, hasSize(1));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(2, priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, (int) v));



    }

    @Test
    public void nms13637SubnetworksTest() {
        final Nms13637NetworkBuilder builder = new Nms13637NetworkBuilder();

        m_nodeDao.save(builder.getRouter1());
        m_nodeDao.save(builder.getRouter2());
        m_nodeDao.save(builder.getRouter3());
        m_nodeDao.save(builder.getCiscoHomeSw());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(4));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(4));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(2));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(1));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(3, priorityMap.size());
        priorityMap.values().forEach(v -> assertEquals(0, (int) v));

    }

    @Test
    public void nms13923SubnetworksTest() {
        final Nms13923NetworkBuilder builder = new Nms13923NetworkBuilder();

        m_nodeDao.save(builder.getSrv005());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(1));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(1));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(1));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        assertThat(legalsubnets, hasSize(0));
    }

    @Test
    public void nms17216SubnetworksTest() {
        final Nms17216NetworkBuilder builder = new Nms17216NetworkBuilder();
        m_nodeDao.save(builder.getSwitch1());
        m_nodeDao.save(builder.getSwitch2());
        m_nodeDao.save(builder.getSwitch3());
        m_nodeDao.save(builder.getSwitch4());
        m_nodeDao.save(builder.getSwitch5());
        m_nodeDao.save(builder.getRouter1());
        m_nodeDao.save(builder.getRouter2());
        m_nodeDao.save(builder.getRouter3());
        m_nodeDao.save(builder.getRouter4());

        final List<NodeTopologyEntity> nodes = m_nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(9));

        final List<IpInterfaceTopologyEntity> ips = m_nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(18));

        final Set<SubNetwork> ptpsubnets = m_nodeTopologyService.findAllPointToPointSubNetwork();
        System.err.println("---All PointToPointSubnetwork " + ptpsubnets.size());
        ptpsubnets.forEach(System.err::println);
        assertThat(ptpsubnets, hasSize(3));

        final Set<SubNetwork> ptpLegalsubnets = m_nodeTopologyService.findAllLegalPointToPointSubNetwork();
        System.err.println("---All Legal PointToPointSubnetwork " + ptpLegalsubnets.size());
        ptpLegalsubnets.forEach(System.err::println);
        assertThat(ptpLegalsubnets, hasSize(3));

        final Set<SubNetwork> lpbsubnets = m_nodeTopologyService.findAllLoopbacks();
        System.err.println("---All Loopbacks " + lpbsubnets.size());
        lpbsubnets.forEach(System.err::println);
        assertThat(lpbsubnets,hasSize(0));

        final Set<SubNetwork> lpblegalsubnets = m_nodeTopologyService.findAllLegalLoopbacks();
        System.err.println("---All Legal Loopbacks " + lpblegalsubnets.size());
        lpblegalsubnets.forEach(System.err::println);
        assertThat(lpblegalsubnets,hasSize(0));

        final Set<SubNetwork> subnets = m_nodeTopologyService.findAllSubNetwork();
        System.err.println("---All  " + subnets.size());
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(9));

        final Set<SubNetwork> legalsubnets = m_nodeTopologyService.findAllLegalSubNetwork();
        System.err.println("---All legal --- " + subnets.size());
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(6));

        Map<Integer,Integer> priorityMap = m_nodeTopologyService.getNodeidPriorityMap(ProtocolSupported.NODES);
        LOG.info("{}", priorityMap);
        assertEquals(9, priorityMap.size());
        priorityMap.values().forEach(v -> assertTrue(v < 6));


    }


}

