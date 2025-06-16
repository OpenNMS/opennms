/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

    boolean parseUpdates();
    void updatesAvailable();
    boolean hasUpdates();
    void refresh();
}
