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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdEdge;
import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.dao.api.IpNetToMediaDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;

import com.codahale.metrics.MetricRegistry;

public class BridgeTopologyProvider extends EnhancedLinkdTopologyProvider {

    public final static String TOPOLOGY_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::BRIDGE";

    private IpNetToMediaDao m_ipNetToMediaDao;
    private BridgeBridgeLinkDao m_bridgeBridgeLinkDao;
    private BridgeMacLinkDao m_bridgeMacLinkDao;
    private BridgeTopologyDao m_bridgeTopologyDao;

    public BridgeTopologyProvider(MetricRegistry registry) {
        super(registry, TOPOLOGY_NAMESPACE);
    }

    @Override
    protected void loadTopology(Map<Integer, OnmsNode> nodemap, Map<Integer, List<OnmsSnmpInterface>> nodesnmpmap, Map<Integer, OnmsIpInterface> nodeipprimarymap, Map<InetAddress, OnmsIpInterface> ipmap) {
        final Map<String, List<OnmsIpInterface>> macToIpMap = getMacToIpMap(ipmap);

        for (BroadcastDomain domain: m_bridgeTopologyDao.getAllPersisted(m_bridgeBridgeLinkDao, m_bridgeMacLinkDao)) {
            LOG.info("loadtopology: parsing broadcast Domain: '{}', {}", domain);
            for (SharedSegment segment: domain.getTopology()) {
                if (segment.noMacsOnSegment() && segment.getBridgeBridgeLinks().size() == 1) {
                    for (BridgeBridgeLink link : segment.getBridgeBridgeLinks()) {
                        Vertex source = getOrCreateVertex(nodemap.get(link.getNode().getId()), nodeipprimarymap.get(link.getNode().getId()));
                        Vertex target = getOrCreateVertex(nodemap.get(link.getDesignatedNode().getId()), nodeipprimarymap.get(link.getDesignatedNode().getId()));
                        BridgeLinkDetail detail = new BridgeLinkDetail(getVertexNamespace(),source,link.getBridgePortIfIndex(),  target, link.getDesignatedPortIfIndex(), link.getBridgePort(), link.getDesignatedPort(), link.getId(),link.getId() );
                        LinkdEdge edge = connectVertices(detail, TOPOLOGY_NAMESPACE);
                        edge.setTooltipText(getEdgeTooltipText(detail,nodesnmpmap));
                    }
                    continue;
                }
                if (segment.getBridgeMacLinks().size() == 1 && segment.getBridgeBridgeLinks().size() == 0) {
                    for (BridgeMacLink sourcelink: segment.getBridgeMacLinks()) {
                        if (macToIpMap.containsKey(sourcelink.getMacAddress()) && macToIpMap.get(sourcelink.getMacAddress()).size() > 0) {
                            List<OnmsIpInterface> targetInterfaces = macToIpMap.get(sourcelink.getMacAddress());
                            OnmsIpInterface targetIp = targetInterfaces.get(0);
                            if (segment.getBridgeIdsOnSegment().contains(targetIp.getNode().getId()))
                                continue;
                            Vertex source = getOrCreateVertex(nodemap.get(sourcelink.getNode().getId()), nodeipprimarymap.get(sourcelink.getNode().getId()));
                            Vertex target = getOrCreateVertex(nodemap.get(targetIp.getNode().getId()), nodeipprimarymap.get(targetIp.getNode().getId()));
                            LinkdEdge edge = connectVertices(sourcelink, source, target, TOPOLOGY_NAMESPACE);
                            edge.setTooltipText(getEdgeTooltipText(sourcelink,source,target,targetInterfaces,nodesnmpmap));
                        }
                    }
                    continue;
                }
                String cloudId = segment.getDesignatedBridge()+":"+segment.getDesignatedPort();
                AbstractVertex cloudVertex = addVertex(cloudId, 0, 0);
                cloudVertex.setLabel("");
                cloudVertex.setIconKey("cloud");
                cloudVertex.setTooltipText("Shared Segment: " + nodemap.get(segment.getDesignatedBridge()).getLabel() + " port: " + segment.getDesignatedPort());
                addVertices(cloudVertex);
                LOG.info("loadtopology: adding cloud: id: '{}', {}", cloudId, cloudVertex.getTooltipText() );
                for (BridgePort targetport: segment.getBridgePortsOnSegment()) {
                    Vertex target = getOrCreateVertex(nodemap.get(targetport.getNode().getId()), nodeipprimarymap.get(targetport.getNode().getId()));
                    LinkdEdge edge = connectVertices(targetport, cloudVertex, target, TOPOLOGY_NAMESPACE);
                    edge.setTooltipText(getEdgeTooltipText(targetport,target,nodesnmpmap));
                }
                for (String targetmac: segment.getMacsOnSegment()) {
                    if (macToIpMap.containsKey(targetmac) && macToIpMap.get(targetmac).size() > 0) {
                        List<OnmsIpInterface> targetInterfaces = macToIpMap.get(targetmac);
                        OnmsIpInterface targetIp = targetInterfaces.get(0);
                        if (segment.getBridgeIdsOnSegment().contains(targetIp.getNode().getId()))
                            continue;
                        Vertex target = getOrCreateVertex(nodemap.get(targetIp.getNode().getId()), nodeipprimarymap.get(targetIp.getNode().getId()));
                        LinkdEdge edge = connectCloudMacVertices(targetmac, cloudVertex, target, TOPOLOGY_NAMESPACE);
                        edge.setTooltipText(getEdgeTooltipText(targetmac,target,targetInterfaces));
                    }
                }
            }
        }
    }

    private Map<String, List<OnmsIpInterface>> getMacToIpMap(Map<InetAddress, OnmsIpInterface> ipmap) {
        final HashMap<String, List<OnmsIpInterface>> macToIpMap = new HashMap<>();
        try {
            Set<String> duplicatednodemac = new HashSet<String>();
            Map<String, Integer> mactonodemap = new HashMap<String, Integer>();
            LOG.info("Loading ip net to media");
            for (IpNetToMedia ipnettomedia: m_ipNetToMediaDao.findAll()) {
                if (duplicatednodemac.contains(ipnettomedia.getPhysAddress())) {
                    LOG.info("load ip net media: different nodeid found for ip: {} mac: {}. Skipping...", InetAddressUtils.str(ipnettomedia.getNetAddress()), ipnettomedia.getPhysAddress());
                    continue;
                }
                OnmsIpInterface ip = ipmap.get(ipnettomedia.getNetAddress());
                if (ip == null) {
                    LOG.info("load ip net media: no nodeid found for ip: {} mac: {}. Skipping...",InetAddressUtils.str(ipnettomedia.getNetAddress()), ipnettomedia.getPhysAddress());
                    continue;
                }
                if (mactonodemap.containsKey(ipnettomedia.getPhysAddress())) {
                    if (mactonodemap.get(ipnettomedia.getPhysAddress()).intValue() != ip.getNode().getId().intValue()) {
                        LOG.info("load ip net media: different nodeid found for ip: {} mac: {}. Skipping...",InetAddressUtils.str(ipnettomedia.getNetAddress()), ipnettomedia.getPhysAddress());
                        duplicatednodemac.add(ipnettomedia.getPhysAddress());
                        continue;
                    }
                }

                if (!macToIpMap.containsKey(ipnettomedia.getPhysAddress())) {
                    macToIpMap.put(ipnettomedia.getPhysAddress(), new ArrayList<>());
                    mactonodemap.put(ipnettomedia.getPhysAddress(), ip.getNode().getId());
                }
                macToIpMap.get(ipnettomedia.getPhysAddress()).add(ip);
            }
            for (String dupmac: duplicatednodemac)
                macToIpMap.remove(dupmac);

            LOG.info("Ip net to media loaded");
        } catch (Exception e){
            LOG.error("Exception getting ip net to media list: "+e.getMessage(),e);
        }
        return macToIpMap;
    }

    public void setIpNetToMediaDao(IpNetToMediaDao m_ipNetToMediaDao) {
        this.m_ipNetToMediaDao = m_ipNetToMediaDao;
    }

    public void setBridgeBridgeLinkDao(BridgeBridgeLinkDao m_bridgeBridgeLinkDao) {
        this.m_bridgeBridgeLinkDao = m_bridgeBridgeLinkDao;
    }

    public void setBridgeMacLinkDao(BridgeMacLinkDao m_bridgeMacLinkDao) {
        this.m_bridgeMacLinkDao = m_bridgeMacLinkDao;
    }

    public void setBridgeTopologyDao(BridgeTopologyDao m_bridgeTopologyDao) {
        this.m_bridgeTopologyDao = m_bridgeTopologyDao;
    }
}
