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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdEdge;
import org.opennms.netmgt.dao.api.CdpElementDao;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

import com.codahale.metrics.MetricRegistry;

public class CdpTopologyProvider extends EnhancedLinkdTopologyProvider {

    public final static String TOPOLOGY_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::CDP";

    private CdpLinkDao m_cdpLinkDao;
    private CdpElementDao m_cdpElementDao;

    public CdpTopologyProvider(MetricRegistry registry) {
        super(registry, TOPOLOGY_NAMESPACE);
    }

    @Override
    protected void loadTopology(Map<Integer, OnmsNode> nodemap, Map<Integer, List<OnmsSnmpInterface>> nodesnmpmap, Map<Integer, OnmsIpInterface> nodeipprimarymap, Map<InetAddress, OnmsIpInterface> ipmap) {
        Map<Integer, CdpElement> cdpelementmap = new HashMap<Integer, CdpElement>();
        for (CdpElement cdpelement: m_cdpElementDao.findAll()) {
            cdpelementmap.put(cdpelement.getNode().getId(), cdpelement);
        }

        List<CdpLink> allLinks = m_cdpLinkDao.findAll();
        Set<CdpLinkDetail> combinedLinkDetails = new HashSet<CdpLinkDetail>();
        Set<Integer> parsed = new HashSet<Integer>();

        for (CdpLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId()))
                continue;
            LOG.debug("loadtopology: cdp link with id '{}' link '{}' ", sourceLink.getId(), sourceLink);
            CdpElement sourceCdpElement = cdpelementmap.get(sourceLink.getNode().getId());
            CdpLink targetLink = null;
            for (CdpLink link : allLinks) {
                if (sourceLink.getId().intValue() == link.getId().intValue()|| parsed.contains(link.getId()))
                    continue;
                LOG.debug("loadtopology: checking cdp link with id '{}' link '{}' ", link.getId(), link);
                CdpElement element = cdpelementmap.get(link.getNode().getId());
                //Compare the remote data to the targetNode element data
                if (!sourceLink.getCdpCacheDeviceId().equals(element.getCdpGlobalDeviceId()) || !link.getCdpCacheDeviceId().equals(sourceCdpElement.getCdpGlobalDeviceId()))
                    continue;

                if (sourceLink.getCdpInterfaceName().equals(link.getCdpCacheDevicePort()) && link.getCdpInterfaceName().equals(sourceLink.getCdpCacheDevicePort())) {
                    targetLink=link;
                    LOG.info("loadtopology: found cdp mutual link: '{}' and '{}' ", sourceLink,targetLink);
                    break;
                }
            }

            if (targetLink == null) {
                if (sourceLink.getCdpCacheAddressType() == CdpLink.CiscoNetworkProtocolType.ip) {
                    try {
                        InetAddress targetAddress = InetAddressUtils.addr(sourceLink.getCdpCacheAddress());
                        if (ipmap.containsKey(targetAddress)) {
                            targetLink = reverseCdpLink(ipmap.get(targetAddress), sourceCdpElement, sourceLink );
                            LOG.info("loadtopology: found cdp link using cdp cache address: '{}' and '{}'", sourceLink, targetLink);
                        }
                    } catch (Exception e) {
                        LOG.warn("loadtopology: cannot convert ip address: {}", sourceLink.getCdpCacheAddress(), e);
                    }
                }
            }

            if (targetLink == null) {
                LOG.info("loadtopology: cannot found target node for link: '{}'", sourceLink);
                continue;
            }

            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            Vertex source =  getOrCreateVertex(nodemap.get(sourceLink.getNode().getId()),nodeipprimarymap.get(sourceLink.getNode().getId()));
            Vertex target = getOrCreateVertex(nodemap.get(targetLink.getNode().getId()),nodeipprimarymap.get(targetLink.getNode().getId()));
            combinedLinkDetails.add(new CdpLinkDetail(Math.min(sourceLink.getId(), targetLink.getId()) + "|" + Math.max(sourceLink.getId(), targetLink.getId()),
                    source, sourceLink, target, targetLink));

        }

        for (CdpLinkDetail linkDetail : combinedLinkDetails) {
            LinkdEdge edge = connectVertices(linkDetail, TOPOLOGY_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail,nodesnmpmap));
        }
    }

    protected CdpLink reverseCdpLink(OnmsIpInterface iface, CdpElement element, CdpLink link) {
        CdpLink reverseLink = new CdpLink();
        reverseLink.setId(-link.getId());
        reverseLink.setNode(iface.getNode());
        reverseLink.setCdpCacheIfIndex(iface.getIfIndex());
        reverseLink.setCdpInterfaceName(link.getCdpCacheDevicePort());
        reverseLink.setCdpCacheDeviceId(element.getCdpGlobalDeviceId());
        reverseLink.setCdpCacheDevicePort(link.getCdpInterfaceName());
        return reverseLink;
    }

    public void setCdpLinkDao(CdpLinkDao m_cdpLinkDao) {
        this.m_cdpLinkDao = m_cdpLinkDao;
    }

    public void setCdpElementDao(CdpElementDao m_cdpElementDao) {
        this.m_cdpElementDao = m_cdpElementDao;
    }
}
