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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.LldpUtils;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdEdge;
import org.opennms.netmgt.dao.api.LldpElementDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

import com.codahale.metrics.MetricRegistry;

public class LldpTopologyProvider extends EnhancedLinkdTopologyProvider {

    public final static String TOPOLOGY_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::LLDP";

    private LldpLinkDao m_lldpLinkDao;
    private LldpElementDao m_lldpElementDao;

    public LldpTopologyProvider(MetricRegistry registry) {
        super(registry, TOPOLOGY_NAMESPACE);
    }

    @Override
    protected void loadTopology(Map<Integer, OnmsNode> nodemap, Map<Integer, List<OnmsSnmpInterface>> nodesnmpmap, Map<Integer, OnmsIpInterface> nodeipprimarymap, Map<InetAddress, OnmsIpInterface> ipmap) {
        // Index the nodes by sysName
        final Map<String, OnmsNode> nodesbysysname = new HashMap<>();
        for (OnmsNode node: nodemap.values()) {
            if (node.getSysName() != null) {
                nodesbysysname.putIfAbsent(node.getSysName(), node);
            }
        }

        // Index the LLDP elements by node id
        Map<Integer, LldpElement> lldpelementmap = new HashMap<Integer, LldpElement>();
        for (LldpElement lldpelement: m_lldpElementDao.findAll()) {
            lldpelementmap.put(lldpelement.getNode().getId(), lldpelement);
        }

        // Pull all of the LLDP links and index them by remote chassis id
        List<LldpLink> allLinks = m_lldpLinkDao.findAll();
        Map<String, List<LldpLink>> linksByRemoteChassisId = new HashMap<>();
        for (LldpLink link : allLinks) {
            final String remoteChassisId = link.getLldpRemChassisId();
            List<LldpLink> linksWithRemoteChassisId = linksByRemoteChassisId.get(remoteChassisId);
            if (linksWithRemoteChassisId == null) {
                linksWithRemoteChassisId = new ArrayList<>();
                linksByRemoteChassisId.put(remoteChassisId, linksWithRemoteChassisId);
            }
            linksWithRemoteChassisId.add(link);
        }

        Set<LldpLinkDetail> combinedLinkDetails = new HashSet<LldpLinkDetail>();
        Set<Integer> parsed = new HashSet<Integer>();
        for (LldpLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            LOG.debug("loadtopology: lldp link with id '{}' link '{}' ", sourceLink.getId(), sourceLink);
            LldpElement sourceLldpElement = lldpelementmap.get(sourceLink.getNode().getId());
            LldpLink targetLink = null;

            // Limit the candidate links by only choosing those have a remote chassis id matching the chassis id of the source link
            for (LldpLink link : linksByRemoteChassisId.getOrDefault(sourceLldpElement.getLldpChassisId(), Collections.emptyList())) {
                if (parsed.contains(link.getId())) {
                    continue;
                }

                if (sourceLink.getId().intValue() == link.getId().intValue()) {
                    continue;
                }
                LOG.debug("loadtopology: checking lldp link with id '{}' link '{}' ", link.getId(), link);
                LldpElement element = lldpelementmap.get(link.getNode().getId());
                // Compare the chassis id on the other end of the link
                if (!sourceLink.getLldpRemChassisId().equals(element.getLldpChassisId())) {
                    continue;
                }
                boolean bool1 = sourceLink.getLldpRemPortId().equals(link.getLldpPortId()) && link.getLldpRemPortId().equals(sourceLink.getLldpPortId());
                boolean bool3 = sourceLink.getLldpRemPortIdSubType() == link.getLldpPortIdSubType() && link.getLldpRemPortIdSubType() == sourceLink.getLldpPortIdSubType();

                if (bool1 && bool3) {
                    targetLink=link;
                    LOG.info("loadtopology: found lldp mutual link: '{}' and '{}' ", sourceLink,targetLink);
                    break;
                }
            }

            if (targetLink == null && sourceLink.getLldpRemSysname() != null) {
                final OnmsNode node = nodesbysysname.get(sourceLink.getLldpRemSysname());
                if (node != null) {
                    targetLink = reverseLldpLink(node, sourceLldpElement, sourceLink);
                    LOG.info("loadtopology: found lldp link using lldp rem sysname: '{}' and '{}'", sourceLink, targetLink);
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
            combinedLinkDetails.add(new LldpLinkDetail(Math.min(sourceLink.getId(), targetLink.getId()) + "|" + Math.max(sourceLink.getId(), targetLink.getId()),
                    source, sourceLink, target, targetLink));

        }

        for (LldpLinkDetail linkDetail : combinedLinkDetails) {
            LinkdEdge edge = connectVertices(linkDetail, TOPOLOGY_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail,nodesnmpmap));
        }
    }

    private LldpLink reverseLldpLink(OnmsNode sourcenode, LldpElement element, LldpLink link) {
        LldpLink reverseLink = new LldpLink();
        reverseLink.setId(-link.getId());
        reverseLink.setNode(sourcenode);

        reverseLink.setLldpLocalPortNum(0);
        reverseLink.setLldpPortId(link.getLldpRemPortId());
        reverseLink.setLldpPortIdSubType(link.getLldpRemPortIdSubType());
        reverseLink.setLldpPortDescr(link.getLldpRemPortDescr());
        if (link.getLldpRemPortIdSubType() == LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL) {
            try {
                reverseLink.setLldpPortIfindex(Integer.getInteger(link.getLldpRemPortId()));
            } catch (Exception e) {
                LOG.debug("reverseLldpLink: cannot create ifindex from  LldpRemPortId '{}'", link.getLldpRemPortId());
            }
        }

        reverseLink.setLldpRemChassisId(element.getLldpChassisId());
        reverseLink.setLldpRemChassisIdSubType(element.getLldpChassisIdSubType());
        reverseLink.setLldpRemSysname(element.getLldpSysname());

        reverseLink.setLldpRemPortId(link.getLldpPortId());
        reverseLink.setLldpRemPortIdSubType(link.getLldpPortIdSubType());
        reverseLink.setLldpRemPortDescr(link.getLldpPortDescr());

        reverseLink.setLldpLinkCreateTime(link.getLldpLinkCreateTime());
        reverseLink.setLldpLinkLastPollTime(link.getLldpLinkLastPollTime());

        return reverseLink;
    }

    public void setLldpLinkDao(LldpLinkDao m_lldpLinkDao) {
        this.m_lldpLinkDao = m_lldpLinkDao;
    }

    public void setLldpElementDao(LldpElementDao m_lldpElementDao) {
        this.m_lldpElementDao = m_lldpElementDao;
    }
}
