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
package org.opennms.features.topology.app.internal.service;

import static org.opennms.netmgt.events.api.EventConstants.PARAM_TOPOLOGY_NAMESPACE;

import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.TopologyCache;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.model.events.EventUtils;

import com.google.common.collect.Lists;

public class TopologyServiceEventListener implements EventListener {

    // The UEIs this listener is interested in
    private static final List<String> UEI_LIST =
            Lists.newArrayList(EventConstants.RELOAD_TOPOLOGY_UEI, EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI);

    private final TopologyCache topologyCache;

    private final EventIpcManager eventIpcManager;

    public TopologyServiceEventListener(TopologyCache topologyCache, EventIpcManager eventIpcManager) {
        this.topologyCache = Objects.requireNonNull(topologyCache);
        this.eventIpcManager = Objects.requireNonNull(eventIpcManager);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void onEvent(IEvent e) {
        // Reload given Topology or all
        if (e.getUei().equals(EventConstants.RELOAD_TOPOLOGY_UEI)) {
            final String topologyNamespace = EventUtils.getParm(e, PARAM_TOPOLOGY_NAMESPACE);
            if (topologyNamespace == null || "all".equalsIgnoreCase(topologyNamespace)) {
                topologyCache.invalidateAll();
            } else if (topologyNamespace != null) {
                // At the moment the topology name should be unique
                topologyCache.invalidate(topologyNamespace);
            }
        }
        // BSM has been reloaded, force reload next time
        if (e.getUei().equals(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI)) {
            String daemonName = EventUtils.getParm(e, EventConstants.PARM_DAEMON_NAME);
            if (daemonName != null && "bsmd".equalsIgnoreCase(daemonName)) {
                topologyCache.invalidate("bsm");
            }
        }
    }

    public void init() {
        eventIpcManager.addEventListener(this, UEI_LIST);
    }

    public void destroy() {
        eventIpcManager.removeEventListener(this, UEI_LIST);
    }
}
