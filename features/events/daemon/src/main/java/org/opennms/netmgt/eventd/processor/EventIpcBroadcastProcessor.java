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
package org.opennms.netmgt.eventd.processor;

import java.util.Objects;

import org.opennms.netmgt.events.api.EventIpcBroadcaster;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

/**
 * EventProcessor that broadcasts events to other interested
 * daemons with EventIpcBroadcaster.broadcastNow(Event).
 *
 * @author ranger
 * @version $Id: $
 */
public class EventIpcBroadcastProcessor implements EventProcessor, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(EventIpcBroadcastProcessor.class);
    private EventIpcBroadcaster m_eventIpcBroadcaster;

    private final Timer logBroadcastTimer;
    private final Meter eventBroadcastMeter;

    public EventIpcBroadcastProcessor(MetricRegistry registry) {
        logBroadcastTimer = Objects.requireNonNull(registry).timer("eventlogs.process.broadcast");
        eventBroadcastMeter = registry.meter("events.process.broadcast");
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    @Override
    public void afterPropertiesSet() throws IllegalStateException {
        Assert.state(m_eventIpcBroadcaster != null, "property eventIpcBroadcaster must be set");
    }

    /**
     * If synchronous mode is not specified, the event is broadcasted 
     * asynchronously by default.
     */
    @Override
    public void process(Log eventLog) throws EventProcessorException {
        process(eventLog, false);
    }

    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        if (eventLog != null && eventLog.getEvents() != null && eventLog.getEvents().getEvent() != null) {
            try (Context context = logBroadcastTimer.time()) {
                for(Event eachEvent : eventLog.getEvents().getEvent()) {
                    process(eventLog.getHeader(), eachEvent, synchronous);
                    eventBroadcastMeter.mark();
                }
            }
        }
    }

    private void process(Header eventHeader, Event event, boolean synchronous) {
        if (event.getLogmsg() != null && event.getLogmsg().getDest().equals("suppress")) {
            LOG.debug("process: skip sending event {} to other daemons because is marked as suppress", event.getUei());
        } else {
            m_eventIpcBroadcaster.broadcastNow(event, synchronous);
        }
    }

    /**
     * <p>getEventIpcBroadcaster</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventIpcBroadcaster} object.
     */
    public EventIpcBroadcaster getEventIpcBroadcaster() {
        return m_eventIpcBroadcaster;
    }

    /**
     * <p>setEventIpcBroadcaster</p>
     *
     * @param eventIpcManager a {@link org.opennms.netmgt.events.api.EventIpcBroadcaster} object.
     */
    public void setEventIpcBroadcaster(EventIpcBroadcaster eventIpcManager) {
        m_eventIpcBroadcaster = eventIpcManager;
    }
}
