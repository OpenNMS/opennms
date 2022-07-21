/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.service.api;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;
import org.opennms.netmgt.enlinkd.model.BridgeElement;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.model.OnmsNode;

public interface TopologyService {
    static <L, R> TopologyConnection<L, R>  of(L left, R right) {
        return new TopologyConnection<>(left, right);
    }

    static void removeBridge(BroadcastDomain domain, int bridgeId) {
        Bridge bridge = null;
        for (Bridge curbridge: domain.getBridges()) {
            if (curbridge.getNodeId() == bridgeId) {
                bridge=curbridge;
                break;
            }
        }
        // if not in domain: return
        if (bridge==null)
            return;
        // if last bridge in domain: clear all and return
        if (domain.getBridges().size() == 1) {
            domain.getSharedSegments().clear();
            domain.getBridges().clear();
            return;
        }

        domain.clearTopologyForBridge(bridgeId);
        Set<Bridge> bridges = new HashSet<>();
        for (Bridge cur: domain.getBridges()) {
            if (cur.getNodeId() == bridgeId)
                continue;
            bridges.add(cur);
        }
        domain.setBridges(bridges);
    }

    static SharedSegment create(BridgeMacLink link) {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(getFromBridgeMacLink(link));
        segment.getMacsOnSegment().add(link.getMacAddress());
        segment.setDesignatedBridge(link.getNode().getId());
        segment.setCreateTime(link.getBridgeMacLinkCreateTime());
        segment.setLastPollTime(link.getBridgeMacLinkLastPollTime());
        return segment;
    }

    static SharedSegment create(BridgeBridgeLink link) {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(getFromBridgeBridgeLink(link));
        segment.getBridgePortsOnSegment().add(getFromDesignatedBridgeBridgeLink(link));
        segment.setDesignatedBridge(link.getDesignatedNode().getId());
        segment.setCreateTime(link.getBridgeBridgeLinkCreateTime());
        segment.setLastPollTime(link.getBridgeBridgeLinkLastPollTime());
        return segment;
    }

    static void merge(BroadcastDomain domain,
                      SharedSegment upsegment,
                      Map<BridgePortWithMacs, Set<BridgePortWithMacs>> splitted,
                      Set<String> macsonsegment,
                      BridgePort rootport,
                      Set<BridgePortWithMacs> throughset) {

        splitted.keySet().forEach(designated -> {
            Set<BridgePortWithMacs> ports = splitted.get(designated);
            SharedSegment splitsegment = new SharedSegment();
            splitsegment.getBridgePortsOnSegment().add(designated.getPort());
            splitsegment.setDesignatedBridge(designated.getPort().getNodeId());
            Set<String> macs = new HashSet<>(designated.getMacs());
            ports.forEach(bft ->
            {
                macs.retainAll(bft.getMacs());
                domain.cleanForwarders(bft.getPort().getNodeId());
                upsegment.getBridgePortsOnSegment().remove(bft.getPort());
                splitsegment.getBridgePortsOnSegment().add(bft.getPort());
            });
            splitsegment.getMacsOnSegment().addAll(macs);
            domain.getSharedSegments().add(splitsegment);
            domain.cleanForwarders(macs);
        });

        //Add macs from forwarders
        Map<String, Integer> forfpmacs = new HashMap<>();
        upsegment.getBridgePortsOnSegment().forEach(port ->
        {
            domain.getForwarders(port.getNodeId()).stream().filter(forward -> forward.getPort().equals(port)).
            forEach( forward ->
                    forward.getMacs().forEach(mac -> {
                        int itemsfound=1;
                        if (forfpmacs.containsKey(mac)) {
                            itemsfound = forfpmacs.get(mac);
                            itemsfound++;
                        }
                        forfpmacs.put(mac, itemsfound);
                    }));

            Set<String> clearmacs = new HashSet<>();
            forfpmacs.keySet().forEach(mac -> {
                if (forfpmacs.get(mac) == upsegment.getBridgePortsOnSegment().size()) {
                    upsegment.getMacsOnSegment().add(mac);
                    clearmacs.add(mac);
                }
            });
            domain.cleanForwarders(clearmacs);

        });

        upsegment.getBridgePortsOnSegment().add(rootport);
        upsegment.getMacsOnSegment().retainAll(macsonsegment);
        domain.cleanForwarders(upsegment.getMacsOnSegment());

        throughset.forEach(bft -> createAndAddToBroadcastDomain(domain,
                                                                                       bft));
    }

    static void createAndAddToBroadcastDomain(BroadcastDomain domain, BridgePortWithMacs bft) {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(bft.getPort());
        segment.getMacsOnSegment().addAll(bft.getMacs());
        segment.setDesignatedBridge(bft.getPort().getNodeId());
        domain.getSharedSegments().add(segment);
        domain.cleanForwarders(bft.getMacs());
    }

    static SharedSegment create() {
        return new SharedSegment();

    }

    static BridgePort getFromBridgeMacLink(BridgeMacLink link) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(link.getNode().getId());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setVlan(link.getVlan());
        return bp;
    }

    static BridgePort getFromBridgeBridgeLink(BridgeBridgeLink link) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(link.getNode().getId());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setVlan(link.getVlan());
        return bp;
    }

    static BridgePort getFromDesignatedBridgeBridgeLink(BridgeBridgeLink link) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(link.getDesignatedNode().getId());
        bp.setBridgePort(link.getDesignatedPort());
        bp.setBridgePortIfIndex(link.getDesignatedPortIfIndex());
        bp.setVlan(link.getDesignatedVlan());
        return bp;
    }

    static List<BridgeBridgeLink> generate(BridgePort designatedPort, Set<BridgePort> ports) {
        OnmsNode designatedNode = new OnmsNode();
        designatedNode.setId(designatedPort.getNodeId());
        List<BridgeBridgeLink> links = new ArrayList<>();
        for (BridgePort port:ports) {
            if (port.equals(designatedPort)) {
                continue;
            }
            BridgeBridgeLink link = new BridgeBridgeLink();
            OnmsNode node = new OnmsNode();
            node.setId(port.getNodeId());
            link.setNode(node);
            link.setBridgePort(port.getBridgePort());
            link.setBridgePortIfIndex(port.getBridgePortIfIndex());
            link.setVlan(port.getVlan());
            link.setDesignatedNode(designatedNode);
            link.setDesignatedPort(designatedPort.getBridgePort());
            link.setDesignatedPortIfIndex(designatedPort.getBridgePortIfIndex());
            link.setDesignatedVlan(designatedPort.getVlan());
            links.add(link);
        }
        return links;

    }

    static TopologyShared of(SharedSegment shs, List<MacPort> macPortsOnSegment) {
        TopologyShared tps = new TopologyShared(new ArrayList<>(shs.getBridgePortsOnSegment()),
                                                macPortsOnSegment, shs.getDesignatedPort());


        final Set<String>  noPortMacs = new HashSet<>(shs.getMacsOnSegment());
        macPortsOnSegment.forEach(mp -> noPortMacs.removeAll(mp.getMacPortMap().keySet()));

        if (noPortMacs.size() >0) {
            tps.setCloud(new MacCloud(noPortMacs));
        }
        return tps;
    }

    static Set<String> getIdentifier(List<BridgeElement> elems) {
        Set<String> identifiers = new HashSet<>();
        for (BridgeElement element: elems) {
            if (InetAddressUtils.isValidBridgeAddress(element.getBaseBridgeAddress())) {
                identifiers.add(element.getBaseBridgeAddress());
            }

        }
        return identifiers;
    }

    static String getDesignated(List<BridgeElement> elems) {
        for (BridgeElement element: elems) {
            if (InetAddressUtils.
                    isValidStpBridgeId(element.getStpDesignatedRoot())
                    && !element.getBaseBridgeAddress().
                    equals(InetAddressUtils.getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot()))) {
                String designated=InetAddressUtils.
                               getBridgeAddressFromStpBridgeId(element.getStpDesignatedRoot());
                if (InetAddressUtils.isValidBridgeAddress(designated)) {
                    return designated;
                }
            }
        }
        return null;
    }

    static void createRootBridge(BroadcastDomain domain, Integer nodeid) {
        Bridge bridge = new Bridge(nodeid);
        bridge.setRootBridge();
        domain.getBridges().add(bridge);
    }

    static Bridge create(BroadcastDomain domain, Integer nodeid, Integer rootport) {
        Bridge bridge = new Bridge(nodeid);
        bridge.setRootPort(rootport);
        domain.getBridges().add(bridge);
        return bridge;
    }

    static MacPort create(IpNetToMedia media) {

        Set<InetAddress> ips = new HashSet<>();
        ips.add(media.getNetAddress());

        MacPort port = new MacPort();
        port.setNodeId(media.getNodeId());
        port.setIfIndex(media.getIfIndex());
        port.setMacPortName(media.getPort());
        port.getMacPortMap().put(media.getPhysAddress(), ips);
        return port;
    }

    boolean parseUpdates();
    void updatesAvailable();
    boolean hasUpdates();
    void refresh();
}
