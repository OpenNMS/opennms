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

import org.opennms.features.grpc.exporter.InventoryService;
import org.opennms.features.grpc.exporter.common.MonitoredServiceWithMetadata;
import org.opennms.integration.api.v1.dao.NodeDao;
import org.opennms.integration.api.v1.events.EventListener;
import org.opennms.integration.api.v1.events.EventSubscriptionService;
import org.opennms.integration.api.v1.model.InMemoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class InventoryEventHandler implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(InventoryEventHandler.class);

    private final EventSubscriptionService eventSubscriptionService;

    private final NodeDao nodeDao;

    private final InventoryService inventoryService;

    public InventoryEventHandler(final EventSubscriptionService eventSubscriptionService,
                                 final NodeDao nodeDao,
                                 final InventoryService inventoryService) {
        this.eventSubscriptionService = Objects.requireNonNull(eventSubscriptionService);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.inventoryService = Objects.requireNonNull(inventoryService);
    }

    public void start() {
        this.eventSubscriptionService.addEventListener(this, List.of(
            // Events related to a service
            EventConstants.NODE_GAINED_SERVICE_EVENT_UEI,
            EventConstants.SERVICE_DELETED_EVENT_UEI,

            //Events related to an interface
            EventConstants.INTERFACE_DELETED_EVENT_UEI,
            EventConstants.INTERFACE_REPARENTED_EVENT_UEI,

            //Events related to a node
            EventConstants.NODE_DELETED_EVENT_UEI
        ));
    }

    public void stop() {
        this.eventSubscriptionService.removeEventListener(this);
    }

    @Override
    public String getName() {
        return InventoryEventHandler.class.getName();
    }

    @Override
    public int getNumThreads() {
        return 1;
    }

    @Override
    public void onEvent(final InMemoryEvent event) {
        LOG.debug("Got event: {}", event);

        switch (event.getUei()) {
            case EventConstants.NODE_GAINED_SERVICE_EVENT_UEI:
                if (event.getNodeId() == null || event.getInterface() == null || event.getService() == null) {
                    return;
                }

                final var node = this.nodeDao.getNodeById(event.getNodeId());
                if (node == null) {
                    return;
                }

                final var iface = node.getInterfaceByIp(event.getInterface()).orElse(null);
                if (iface == null) {
                    return;
                }

                final var service = iface.getMonitoredService(event.getService()).orElse(null);
                if (service == null) {
                    return;
                }

                LOG.debug("New service: {}/{}/{}", node.getId(), iface.getIpAddress().getHostAddress(), service.getName());

                this.inventoryService.sendAddService(new MonitoredServiceWithMetadata(node, iface, service));
                break;

            case EventConstants.SERVICE_DELETED_EVENT_UEI:
                // There is not much we can do here for now

            default:
                this.inventoryService.sendSnapshot();
                break;
        }
    }
}
