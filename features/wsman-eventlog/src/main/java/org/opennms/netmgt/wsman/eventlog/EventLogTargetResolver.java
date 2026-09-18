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
package org.opennms.netmgt.wsman.eventlog;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turns a package filter into the interfaces to read. Only interfaces on which the
 * WS-Man service was detected qualify, so provisioning decides which Windows hosts are
 * in scope and the filter only narrows that set.
 */
public class EventLogTargetResolver {

    private static final Logger LOG = LoggerFactory.getLogger(EventLogTargetResolver.class);

    public static final String WSMAN_SERVICE = "WS-Man";

    private final FilterDao filterDao;
    private final NodeDao nodeDao;
    private final SessionUtils sessionUtils;

    public EventLogTargetResolver(FilterDao filterDao, NodeDao nodeDao, SessionUtils sessionUtils) {
        this.filterDao = Objects.requireNonNull(filterDao);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.sessionUtils = Objects.requireNonNull(sessionUtils);
    }

    public List<EventLogTarget> resolve(String filter) {
        final Map<Integer, Map<InetAddress, Set<String>>> byNode = filterDao.getNodeIPAddressServiceMap(filter);
        return sessionUtils.withTransaction(() -> {
            final List<EventLogTarget> targets = new ArrayList<>();
            for (Map.Entry<Integer, Map<InetAddress, Set<String>>> node : byNode.entrySet()) {
                InetAddress address = null;
                for (Map.Entry<InetAddress, Set<String>> iface : node.getValue().entrySet()) {
                    if (iface.getValue() != null && iface.getValue().contains(WSMAN_SERVICE)) {
                        address = iface.getKey();
                        break;
                    }
                }
                if (address == null) {
                    continue;
                }
                final OnmsNode onmsNode = nodeDao.get(node.getKey());
                if (onmsNode == null) {
                    continue;
                }
                final String location = onmsNode.getLocation() != null ? onmsNode.getLocation().getLocationName() : null;
                targets.add(new EventLogTarget(node.getKey(), onmsNode.getLabel(), address, location));
            }
            LOG.debug("Filter '{}' resolved to {} target(s)", filter, targets.size());
            return targets;
        });
    }
}
