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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.enlinkd.model.IpNetToMedia;

public interface TopologyService {
    static <L, R> TopologyConnection<L, R>  of(L left, R right) {
        return new TopologyConnection<>(left, right);
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
