/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.io.UnsupportedEncodingException;
import java.util.Objects;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.opennms.core.ipc.sink.api.AsyncPolicy;
import org.opennms.core.ipc.sink.xml.AbstractXmlSinkModule;
import org.opennms.core.xml.XmlHandler;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.events.api.Events;
import org.opennms.netmgt.events.api.EventsWrapper;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventsModule extends AbstractXmlSinkModule<EventsWrapper, Events> {

	public static final String MODULE_ID = "Events";

	private static final Logger LOG = LoggerFactory.getLogger(EventsModule.class);

	private String eventString;

	private static final String ENCODING = "UTF-8";

	private final EventdConfig m_config;

	private XmlHandler<Event> eventMarshaler = null;

	public EventsModule(EventdConfig config) {
		super(Events.class);
		this.m_config = config;
		eventMarshaler = new XmlHandler<Event>(Event.class);
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
	public AggregationPolicy<EventsWrapper, Events, Events> getAggregationPolicy() {
		return new AggregationPolicy<EventsWrapper, Events, Events>() {

			@Override
			public int getCompletionSize() {
				return m_config.getBatchSize();
			}

			@Override
			public int getCompletionIntervalMs() {
				return m_config.getBatchIntervalMs();
			}

			@Override
			public Object key(EventsWrapper message) {
				return message.getEvents();
			}

			@Override
			public Events aggregate(Events accumulator, EventsWrapper eventsWrapper) {
				accumulator = eventsWrapper.getEvents();
				return accumulator;
			}

			@Override
			public Events build(Events accumulator) {
				return accumulator;
			}
		};
	}

	@Override
	public Events unmarshal(byte[] eventBytes) {
		try {
			eventString = new String(eventBytes, ENCODING);
			Events events = new Events();
			events.setEvent(eventMarshaler.unmarshal(eventString));
			return events;
		} catch (UnsupportedEncodingException e) {
			LOG.error("Failed to marshal onmsevent from kafkaconsumer." + e.getLocalizedMessage());
		}
		return null;
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
