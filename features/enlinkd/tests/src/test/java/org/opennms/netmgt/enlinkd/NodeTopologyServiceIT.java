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

import org.junit.Test;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.nb.Nms17216NetworkBuilder;
import org.springframework.beans.factory.annotation.Autowired;

public class NodeTopologyServiceIT extends EnLinkdBuilderITCase {

    @Autowired
    private NodeTopologyService nodeTopologyService;

    @Test
    public void verifyFindAll() {
        final Nms17216NetworkBuilder builder = new Nms17216NetworkBuilder();
        m_nodeDao.save(builder.getSwitch1());
        m_nodeDao.save(builder.getSwitch2());

        final List<NodeTopologyEntity> nodes = nodeTopologyService.findAllNode();
        nodes.stream().forEach(n -> System.err.println(n));
        assertThat(nodes, hasSize(2));
        
        final List<IpInterfaceTopologyEntity> ips = nodeTopologyService.findAllIp();
        ips.stream().forEach(i -> System.err.println(i));
        
        assertThat(ips, hasSize(6));
    }
}
