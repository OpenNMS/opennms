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

package org.opennms.features.grpc.exporter.events.alarms;

import org.opennms.features.grpc.exporter.alarms.EventService;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import  org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class EventsExporter implements EventListener {
    private static final Logger LOG = LoggerFactory.getLogger(EventsExporter.class);

    private final EventSubscriptionService eventSubscriptionService;
    private final EventService eventService;

    public EventsExporter(final EventSubscriptionService eventSubscriptionService,
                          final EventService eventService) {
        this.eventSubscriptionService = Objects.requireNonNull(eventSubscriptionService);
        this.eventService = Objects.requireNonNull(eventService);
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
        LOG.info("received new event: {}", event);
        if (event.getNodeid() == null) {
            return;
        }
        this.eventService.sendEventUpdate(event);
    }

}
