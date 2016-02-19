/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeProvider;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.dao.api.OspfElementDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.model.OspfLink;

public class OspfEdgeProvider implements EdgeProvider {

    private OspfElementDao m_ospfElementDao;
    private OspfLinkDao m_ospfLinkDao;

    @Override
    public String getEdgeNamespace() {
        return AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD + "::OSPF";
    }

    @Override
    public boolean contributesTo(String namespace) {
        return namespace.equals(AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD);
    }

    @Override
    public Edge getEdge(String namespace, String id) {
        return null;
    }

    @Override
    public Edge getEdge(EdgeRef reference) {
        return null;
    }

    @Override
    public List<Edge> getEdges(Criteria... criteria) {
        List<OspfLink> allLinks =  getOspfLinkDao().findAll();
        Set<Edge> combinedLinks = new HashSet<Edge>();
        for(OspfLink sourceLink : allLinks) {

            for (OspfLink targetLink : allLinks) {
                boolean ipAddrCheck = sourceLink.getOspfRemIpAddr().equals(targetLink.getOspfIpAddr()) && targetLink.getOspfRemIpAddr().equals(sourceLink.getOspfIpAddr());
                if(ipAddrCheck) {
                    String id = "ospf::" + Math.min(sourceLink.getId(), targetLink.getId()) + "||" + Math.max(sourceLink.getId(), targetLink.getId());
                    Vertex source = new AbstractVertex(AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNode().getNodeId(), sourceLink.getNode().getLabel());
                    Vertex target = new AbstractVertex(AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, targetLink.getNode().getNodeId(), targetLink.getNode().getLabel());
                    Edge edge = new AbstractEdge(getEdgeNamespace(), id, source, target);
                    combinedLinks.add(edge);
                }
            }
        }
        return Arrays.asList(combinedLinks.toArray(new Edge[0]));
    }

    @Override
    public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void addEdgeListener(EdgeListener listener) {

    }

    @Override
    public void removeEdgeListener(EdgeListener listener) {

    }

    @Override
    public void clearEdges() {

    }

    public OspfElementDao getOspfElementDao() {
        return m_ospfElementDao;
    }

    public void setOspfElementDao(OspfElementDao ospfElementDao) {
        m_ospfElementDao = ospfElementDao;
    }

    public OspfLinkDao getOspfLinkDao() {
        return m_ospfLinkDao;
    }

    public void setOspfLinkDao(OspfLinkDao ospfLinkDao) {
        m_ospfLinkDao = ospfLinkDao;
    }
}
