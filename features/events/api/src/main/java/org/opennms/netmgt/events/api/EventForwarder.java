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
import org.opennms.netmgt.xml.event.Log;

/**
 * <p>EventForwarder interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface EventForwarder {
    
    /**
     * Asynchronously sends an event to eventd.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void sendNow(Event event);

    /**
     * Asynchronously sends a set of events to eventd.
     *
     * @param eventLog a {@link org.opennms.netmgt.xml.event.Log} object.
     */
    public void sendNow(Log eventLog);

    /**
     * Synchronously sends an event to eventd.
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void sendNowSync(Event event);

    /**
     * Synchronously sends a set of events to eventd.
     *
     * @param eventLog a {@link org.opennms.netmgt.xml.event.Log} object.
     */
    public void sendNowSync(Log eventLog);
}
