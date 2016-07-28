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
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdEdge;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.topology.IsisTopologyLink;

import com.codahale.metrics.MetricRegistry;

public class IsisTopologyProvider extends EnhancedLinkdTopologyProvider {

    public final static String TOPLOGY_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::ISIS";

    private IsIsLinkDao m_isisLinkDao;

    public IsisTopologyProvider(MetricRegistry registry) {
        super(registry, TOPLOGY_NAMESPACE);
    }

    @Override
    protected void loadTopology(Map<Integer, OnmsNode> nodemap, Map<Integer, List<OnmsSnmpInterface>> nodesnmpmap, Map<Integer, OnmsIpInterface> nodeipprimarymap, Map<InetAddress, OnmsIpInterface> ipmap) {
        List<IsisTopologyLink> isislinks = m_isisLinkDao.getLinksForTopology();

        if (isislinks != null && isislinks.size() > 0) {
            for (IsisTopologyLink link : isislinks) {
                LOG.debug("loadtopology: adding isis link: '{}'", link );
                String id = Math.min(link.getSourceId(), link.getTargetId()) + "|" + Math.max(link.getSourceId(), link.getTargetId());
                Vertex source = getVertex(getVertexNamespace(), link.getSrcNodeId().toString());
                if (source == null) {
                    OnmsIpInterface primary= nodeipprimarymap.get(link.getSrcNodeId());
                    source = getDefaultVertex(link.getSrcNodeId(),
                            link.getSrcSysoid(),
                            link.getSrcLabel(),
                            link.getSrcLocation(),
                            link.getSrcNodeType(),
                            primary.isManaged(),
                            InetAddressUtils.str(primary.getIpAddress()));
                    addVertices(source);

                }
                Vertex target = getVertex(getVertexNamespace(), link.getTargetNodeId().toString());
                if (target == null) {
                    OnmsIpInterface targetprimary= nodeipprimarymap.get(link.getSrcNodeId());
                    target = getDefaultVertex(link.getTargetNodeId(),
                            link.getTargetSysoid(),
                            link.getTargetLabel(),
                            link.getTargetLocation(),
                            link.getTargetNodeType(),
                            targetprimary.isManaged(),
                            InetAddressUtils.str(targetprimary.getIpAddress()));
                    addVertices(target);
                }
                IsIsLinkDetail linkDetail = new IsIsLinkDetail(
                        id,
                        source,
                        link.getSourceId(),
                        link.getSrcIfIndex(),
                        target,
                        link.getTargetId(),
                        link.getTargetIfIndex()
                );

                LinkdEdge edge = connectVertices(linkDetail, TOPLOGY_NAMESPACE);
                edge.setTooltipText(getEdgeTooltipText(linkDetail,nodesnmpmap));
            }
        }
    }

    public void setIsisLinkDao(IsIsLinkDao m_isisLinkDao) {
        this.m_isisLinkDao = m_isisLinkDao;
    }
}
