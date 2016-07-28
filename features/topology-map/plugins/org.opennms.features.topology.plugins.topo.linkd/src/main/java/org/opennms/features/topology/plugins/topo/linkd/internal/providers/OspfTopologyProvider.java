/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal.providers;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdEdge;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OspfLink;

import com.codahale.metrics.MetricRegistry;

public class OspfTopologyProvider extends EnhancedLinkdTopologyProvider {

    public final static String TOPOLOGY_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::OSPF";

    private OspfLinkDao m_ospfLinkDao;

    public OspfTopologyProvider(MetricRegistry registry) {
        super(registry, TOPOLOGY_NAMESPACE);
    }

    @Override
    protected void loadTopology(Map<Integer, OnmsNode> nodemap, Map<Integer, List<OnmsSnmpInterface>> nodesnmpmap, Map<Integer, OnmsIpInterface> nodeipprimarymap, Map<InetAddress, OnmsIpInterface> ipmap) {
        List<OspfLink> allLinks =  m_ospfLinkDao.findAll();
        Set<OspfLinkDetail> combinedLinkDetails = new HashSet<OspfLinkDetail>();
        Set<Integer> parsed = new HashSet<Integer>();
        for(OspfLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId()))
                continue;
            LOG.debug("loadtopology: ospf link with id '{}'", sourceLink.getId());
            for (OspfLink targetLink : allLinks) {
                if (sourceLink.getId().intValue() == targetLink.getId().intValue() || parsed.contains(targetLink.getId()))
                    continue;
                LOG.debug("loadtopology: checking ospf link with id '{}'", targetLink.getId());
                if(sourceLink.getOspfRemIpAddr().equals(targetLink.getOspfIpAddr()) && targetLink.getOspfRemIpAddr().equals(sourceLink.getOspfIpAddr())) {
                    LOG.info("loadtopology: found ospf mutual link: '{}' and '{}' ", sourceLink,targetLink);
                    parsed.add(sourceLink.getId());
                    parsed.add(targetLink.getId());
                    Vertex source =  getOrCreateVertex(nodemap.get(sourceLink.getNode().getId()),nodeipprimarymap.get(sourceLink.getNode().getId()));
                    Vertex target = getOrCreateVertex(nodemap.get(targetLink.getNode().getId()),nodeipprimarymap.get(targetLink.getNode().getId()));
                    OspfLinkDetail linkDetail = new OspfLinkDetail(
                            Math.min(sourceLink.getId(), targetLink.getId()) + "|" + Math.max(sourceLink.getId(), targetLink.getId()),
                            source, sourceLink, target, targetLink);
                    combinedLinkDetails.add(linkDetail);
                    break;
                }
            }
        }

        for (OspfLinkDetail linkDetail : combinedLinkDetails) {
            LinkdEdge edge = connectVertices(linkDetail, TOPOLOGY_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail,nodesnmpmap));
        }
    }

    public void setOspfLinkDao(OspfLinkDao m_ospfLinkDao) {
        this.m_ospfLinkDao = m_ospfLinkDao;
    }
}
