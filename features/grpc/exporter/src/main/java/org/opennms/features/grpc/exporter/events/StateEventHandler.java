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

package org.opennms.features.grpc.exporter.events;

import org.opennms.features.grpc.exporter.StateService;
import org.opennms.features.grpc.exporter.common.MonitoredServiceWithMetadata;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.events.EventListener;
import org.opennms.integration.api.v1.events.EventSubscriptionService;
import org.opennms.integration.api.v1.model.InMemoryEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class StateEventHandler implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(StateEventHandler.class);

    private final EventSubscriptionService eventSubscriptionService;

    private final NodeDao nodeDao;

    private final StateService stateService;

    public StateEventHandler(final EventSubscriptionService eventSubscriptionService,
                             final NodeDao nodeDao,
                             final StateService stateService) {
        this.eventSubscriptionService = Objects.requireNonNull(eventSubscriptionService);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.stateService = Objects.requireNonNull(stateService);
    }

    public void start() {
        this.eventSubscriptionService.addEventListener(this, List.of(
            EventConstants.SERVICE_RESPONSIVE_EVENT_UEI,
            EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI,
            EventConstants.SERVICE_UNMANAGED_EVENT_UEI,

            EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI,
            EventConstants.NODE_LOST_SERVICE_EVENT_UEI,

            EventConstants.INTERFACE_UP_EVENT_UEI,
            EventConstants.INTERFACE_DOWN_EVENT_UEI,

            EventConstants.NODE_UP_EVENT_UEI,
            EventConstants.NODE_DOWN_EVENT_UEI
        ));
    }

    public void stop() {
        this.eventSubscriptionService.removeEventListener(this);
    }

    @Override
    public String getName() {
        return StateEventHandler.class.getName();
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void onEvent(final InMemoryEvent event) {
        LOG.debug("Got event: {}", event);

        if (event.getNodeId() == null) {
            return;
        }

        final var node = nodeDao.getNodeById(event.getNodeId());
        if (node == null) {
            return;
        }

        final var interfaces = event.getInterface() != null
                ? node.getInterfaceByIp(event.getInterface()).stream()
                : node.getIpInterfaces().stream();

        final var services = interfaces
                .flatMap(iface -> (event.getService() != null
                        ? iface.getMonitoredService(event.getService()).stream()
                        : iface.getMonitoredServices().stream()
                    ).map(service -> new MonitoredServiceWithMetadata(node, iface, service)))
                .collect(Collectors.toList());

        this.stateService.sendState(services);
    }
}
