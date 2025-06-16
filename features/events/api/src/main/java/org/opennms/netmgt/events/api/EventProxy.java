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
 * This is the interface used to send events into the event subsystem - It is
 * typically used by the poller framework plugins that perform service
 * monitoring to send out appropriate events. Can also be used by capsd,
 * discovery etc.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 */
public interface EventProxy {
    /**
     * This method is called to send the event out
     *
     * @param event
     *            the event to be sent out
     * @exception EventProxyException
     *                thrown if the send fails for any reason
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    public void send(Event event) throws EventProxyException;

    /**
     * This method is called to send an event log containing multiple events out
     *
     * @param eventLog
     *            the events to be sent out
     * @exception EventProxyException
     *                thrown if the send fails for any reason
     * @throws org.opennms.netmgt.events.api.EventProxyException if any.
     */
    public void send(Log eventLog) throws EventProxyException;
}
