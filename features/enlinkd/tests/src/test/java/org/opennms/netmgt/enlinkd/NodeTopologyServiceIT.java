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
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.SubNetwork;
import org.opennms.netmgt.nb.Nms0001NetworkBuilder;
import org.opennms.netmgt.nb.Nms0002NetworkBuilder;
import org.opennms.netmgt.nb.Nms17216NetworkBuilder;
import org.opennms.netmgt.nb.Nms7563NetworkBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class NodeTopologyServiceIT extends EnLinkdBuilderITCase {

    @Autowired
    private NodeTopologyService nodeTopologyService;

    @Test
    public void findAllTest() {
        final Nms17216NetworkBuilder builder = new Nms17216NetworkBuilder();
        m_nodeDao.save(builder.getSwitch1());
        m_nodeDao.save(builder.getSwitch2());

        final List<NodeTopologyEntity> nodes = nodeTopologyService.findAllNode();
        nodes.forEach(System.err::println);
        assertThat(nodes, hasSize(2));
        
        final List<IpInterfaceTopologyEntity> ips = nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        
        assertThat(ips, hasSize(6));
    }

    @Test
    public void nms0001SubnetworksTest() {
        final Nms0001NetworkBuilder builder = new Nms0001NetworkBuilder();
        m_nodeDao.save(builder.getFroh());
        m_nodeDao.save(builder.getOedipus());
        m_nodeDao.save(builder.getSiegFrie());

        final List<IpInterfaceTopologyEntity> ips = nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(47));

        final Set<SubNetwork> subnets = nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(21));

        final Set<SubNetwork> legalsubnets = nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(1));

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

        final List<IpInterfaceTopologyEntity> ips = nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(146));

        final Set<SubNetwork> subnets = nodeTopologyService.findAllLegalSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(4));
    }

    @Test
    public void nms7563SubnetworksTest() {
        final Nms7563NetworkBuilder builder = new Nms7563NetworkBuilder();
        m_nodeDao.save(builder.getCisco01());
        m_nodeDao.save(builder.getHomeServer());
        m_nodeDao.save(builder.getSwitch02());

        final List<IpInterfaceTopologyEntity> ips = nodeTopologyService.findAllIp();
        ips.forEach(System.err::println);
        assertThat(ips, hasSize(5));

        final Set<SubNetwork> subnets = nodeTopologyService.findAllSubNetwork();
        subnets.forEach(System.err::println);
        assertThat(subnets, hasSize(3));

        final Set<SubNetwork> legalsubnets = nodeTopologyService.findAllLegalSubNetwork();
        legalsubnets.forEach(System.err::println);
        assertThat(legalsubnets, hasSize(1));

    }

}
