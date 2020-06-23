/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.service;

import static org.opennms.netmgt.events.api.EventConstants.PARAM_TOPOLOGY_NAMESPACE;

import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;

import com.google.common.collect.Lists;

public class TopologyServiceEventListener implements EventListener {

    // The UEIs this listener is interested in
    private static final List<String> UEI_LIST =
            Lists.newArrayList(EventConstants.RELOAD_TOPOLOGY_UEI, EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI);

    private final DefaultTopologyService topologyService;

    private final EventIpcManager eventIpcManager;

    public TopologyServiceEventListener(DefaultTopologyService topologyService, EventIpcManager eventIpcManager) {
        this.topologyService = Objects.requireNonNull(topologyService);
        this.eventIpcManager = Objects.requireNonNull(eventIpcManager);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void onEvent(Event e) {
        // Reload given Topology or all
        if (e.getUei().equals(EventConstants.RELOAD_TOPOLOGY_UEI)) {
            final String topologyNamespace = EventUtils.getParm(e, PARAM_TOPOLOGY_NAMESPACE);
            if (topologyNamespace == null || "all".equalsIgnoreCase(topologyNamespace)) {
                topologyService.invalidateAll();
            } else if (topologyNamespace != null) {
                // At the moment the topology name should be unique
                topologyService.invalidate(topologyNamespace);
            }
        }
        // BSM has been reloaded, force reload next time
        if (e.getUei().equals(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI)) {
            String daemonName = EventUtils.getParm(e, EventConstants.PARM_DAEMON_NAME);
            if (daemonName != null && "bsmd".equalsIgnoreCase(daemonName)) {
                topologyService.invalidate("bsm");
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
