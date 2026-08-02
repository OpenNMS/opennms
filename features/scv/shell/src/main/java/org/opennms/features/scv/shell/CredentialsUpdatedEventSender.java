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
package org.opennms.features.scv.shell;

import java.util.Date;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;

/**
 * Notifies interested daemons that credentials changed, so they can discard cached
 * values interpolated from ${scv:...} expressions. The event carries no node id
 * since credentials are not tied to a single node.
 */
final class CredentialsUpdatedEventSender {

    private CredentialsUpdatedEventSender() {}

    static void sendCredentialsUpdatedEvent(final EventForwarder eventForwarder, final String source) {
        if (eventForwarder == null) {
            // not available on all containers (e.g. Minion without an event sink)
            return;
        }
        try {
            final Event event = new Event();
            event.setUei(EventConstants.NODE_METADATA_UPDATED_EVENT_UEI);
            event.setSource(source);
            event.setTime(new Date());
            eventForwarder.sendNow(event);
        } catch (Exception e) {
            System.err.println("Failed to send " + EventConstants.NODE_METADATA_UPDATED_EVENT_UEI + " event: " + e.getMessage());
        }
    }
}
