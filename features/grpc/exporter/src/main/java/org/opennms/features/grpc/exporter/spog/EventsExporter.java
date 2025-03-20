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

package org.opennms.features.grpc.exporter.spog;

import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.features.grpc.exporter.mapper.EventsMapper;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class EventsExporter implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(EventsExporter.class);

    private final EventSubscriptionService eventSubscriptionService;
    private final RuntimeInfo runtimeInfo;

    private final NmsInventoryGrpcClient client;

    private final boolean eventExportEnabled;

    public EventsExporter(final EventSubscriptionService eventSubscriptionService,
                          RuntimeInfo runtimeInfo,
                          NmsInventoryGrpcClient client,
                          boolean eventExportEnabled) {
        this.eventSubscriptionService = Objects.requireNonNull(eventSubscriptionService);
        this.runtimeInfo = runtimeInfo;
        this.client = client;
        this.eventExportEnabled = eventExportEnabled;
    }

    public void start() {
        this.eventSubscriptionService.addEventListener(this);
    }

    public void stop() {
        this.eventSubscriptionService.removeEventListener(this);
    }

    @Override
    public String getName() {
        return EventsExporter.class.getName();
    }

    @Override
    public void onEvent(IEvent event) {
        sendEventUpdate(event);
    }

    private void sendEventUpdate(final IEvent event) {

        if (!event.hasNodeid()) {
            return;
        }

        if (!client.isEnabled()) {
            LOG.debug("NMS Inventory service disabled, not sending event updates");
            return;
        }

        if (!eventExportEnabled) {
            LOG.debug("Event Export disabled, not sending event updates");
            return;
        }

        LOG.debug("Received new event with uei : {} and id : {}", event.getUei(), event.getDbid());
        final var events = EventsMapper.INSTANCE.toEventUpdateList(List.of(event), this.runtimeInfo, SystemInfoUtils.getInstanceId(), false);
        this.client.sendEventUpdate(events);
    }

}
