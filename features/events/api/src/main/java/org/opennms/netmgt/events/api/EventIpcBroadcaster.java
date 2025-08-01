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
package org.opennms.netmgt.events.api;

import org.opennms.netmgt.xml.event.Event;

/**
 * Back-end interface for the EventIpcManager.  Used by eventd to send events
 * to interested listeners.
 */
public interface EventIpcBroadcaster {
    /**
     * Called by eventd to send an event to all interested listeners.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param synchronous Whether this call should wait for all {@link EventListener}
     *   calls to finish before returning.
     */
    void broadcastNow(Event event, boolean synchronous);
}
