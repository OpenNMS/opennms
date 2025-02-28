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

package org.opennms.features.grpc.exporter.nmsinventory;

import org.opennms.core.utils.SystemInfoUtils;
import org.opennms.features.grpc.exporter.mapper.EventsMapper;
import org.opennms.integration.api.v1.runtime.RuntimeInfo;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Objects;

public class EventService {
    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    private final RuntimeInfo runtimeInfo;

    private final NmsInventoryGrpcClient client;

    public EventService(
                        final RuntimeInfo runtimeInfo,
                        final NmsInventoryGrpcClient client
    ) {
        this.runtimeInfo = Objects.requireNonNull(runtimeInfo);
        this.client = Objects.requireNonNull(client);
    }

    public void sendEventUpdate(final IEvent event) {
            final var events = EventsMapper.INSTANCE.toEventUpdateList(List.of(event), this.runtimeInfo, SystemInfoUtils.getInstanceId(), false);
            this.client.sendEventUpdate(events);
    }
}
