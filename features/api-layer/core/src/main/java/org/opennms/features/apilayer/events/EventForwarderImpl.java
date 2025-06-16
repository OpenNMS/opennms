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
package org.opennms.features.apilayer.events;

import java.util.Objects;

import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.events.EventForwarder;
import org.opennms.integration.api.v1.model.InMemoryEvent;

public class EventForwarderImpl implements EventForwarder {

    private final org.opennms.netmgt.events.api.EventForwarder eventForwarder;

    public EventForwarderImpl(org.opennms.netmgt.events.api.EventForwarder eventForwarder) {
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
    }

    @Override
    public void sendAsync(InMemoryEvent event) {
        eventForwarder.sendNow(ModelMappers.toEvent(event));
    }

    @Override
    public void sendSync(InMemoryEvent event) {
        eventForwarder.sendNowSync(ModelMappers.toEvent(event));
    }
}
