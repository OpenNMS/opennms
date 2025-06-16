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
/**
 * @author ms043660 (Malatesh.Sudarshan@cerner.com)
 */
package org.opennms.features.events.sink.module;

import java.util.Objects;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.xml.AbstractXmlSinkModule;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

/**
 * @author Malatesh Sudarshan
 */
public class EventSinkModule extends AbstractXmlSinkModule<Event, Log> {

    public static final String MODULE_ID = "Events";

    private final EventdConfig m_config;

    public EventSinkModule(EventdConfig config) {
        super(Log.class);
        this.m_config = config;
    }

    @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public int getNumConsumerThreads() {
        return m_config.getNumThreads();
    }

    @Override
    public AggregationPolicy<Event, Log, Log> getAggregationPolicy() {
        return new AggregationPolicy<Event, Log, Log>() {

            @Override
            public int getCompletionSize() {
                return m_config.getBatchSize();
            }

            @Override
            public int getCompletionIntervalMs() {
                return m_config.getBatchIntervalMs();
            }

            @Override
            public Object key(Event event) {
                return event;
            }

            @Override
            public Log aggregate(Log eventLog, Event event) {
                if (eventLog == null) {
                    eventLog = new Log();
                    eventLog.addEvent(event);
                }
                eventLog.addEvent(event);
                return eventLog;
            }

            @Override
            public Log build(Log accumulator) {
                return accumulator;
            }
        };
    }

    @Override
    public AsyncPolicy getAsyncPolicy() {
        return new AsyncPolicy() {
            @Override
            public int getQueueSize() {
                return m_config.getQueueSize();
            }

            @Override
            public int getNumThreads() {
                return m_config.getNumThreads();
            }

            @Override
            public boolean isBlockWhenFull() {
                return true;
            }
        };
    }

    @Override
    public Event unmarshalSingleMessage(byte[] bytes) {
        Log log = unmarshal(bytes);
        return log.getEvents().getEvent(0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(MODULE_ID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }

}
