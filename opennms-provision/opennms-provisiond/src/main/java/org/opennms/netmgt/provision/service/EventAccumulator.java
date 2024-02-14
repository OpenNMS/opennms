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
package org.opennms.netmgt.provision.service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class EventAccumulator implements EventForwarder {
    private static final Logger LOG = LoggerFactory.getLogger(EventAccumulator.class);

    private final EventForwarder m_eventForwarder;
    private final Queue<Event> m_events = new ConcurrentLinkedQueue<>();

    public EventAccumulator(final EventForwarder forwarder) {
        m_eventForwarder = forwarder;
    }

    @Override
    public void sendNow(final Event event) {
        if (event != null) {
            m_events.add(event);
        }
    }

    @Override
    public void sendNow(final Log log) {
        if (log != null && log.getEvents() != null && log.getEvents().getEventCount() > 0) {
            m_events.addAll(log.getEvents().getEventCollection());
        }
    }

    @Override
    public void sendNowSync(Event event) {
        sendNow(event);
    }

    @Override
    public void sendNowSync(Log eventLog) {
        sendNow(eventLog);
    }

    /**
     * Thread-safe and idempotent.
     */
    public void flush() {
        int i = 0;
        while (!m_events.isEmpty()) {
            Event event = m_events.poll();
            if (event != null) {
                m_eventForwarder.sendNow(event);
                i++;
            } else {
                break;
            }
        }
        LOG.debug("flush(): sent {} events: {}", i, m_events);
    }

}
