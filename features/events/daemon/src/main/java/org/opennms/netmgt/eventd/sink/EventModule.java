/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

/**
 * @author ms043660 (Malatesh.Sudarshan@cerner.com)
 */
package org.opennms.netmgt.eventd.sink;

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
public class EventModule extends AbstractXmlSinkModule<Event, Log> {

    public static final String MODULE_ID = "Events";

    private final EventdConfig m_config;

    public EventModule(EventdConfig config) {
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
