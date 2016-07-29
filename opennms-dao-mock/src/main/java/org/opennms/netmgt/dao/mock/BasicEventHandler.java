/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.util.List;

import org.opennms.netmgt.events.api.EventHandler;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.events.api.support.EventLogSplitter;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicEventHandler implements EventHandler {

	private static final Logger LOG = LoggerFactory.getLogger(BasicEventHandler.class);

	private List<EventProcessor> m_eventProcessors;

	/**
	 * <p>getEventProcessors</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<EventProcessor> getEventProcessors() {
		return m_eventProcessors;
	}

	/**
	 * <p>setEventProcessors</p>
	 *
	 * @param eventProcessors a {@link java.util.List} object.
	 */
	public void setEventProcessors(List<EventProcessor> eventProcessors) {
		m_eventProcessors = eventProcessors;
	}

	@Override
	public void handle(Log eventLog) {
		for (Log event : EventLogSplitter.splitEventLogs(eventLog)) {
			for (final EventProcessor eventProcessor : m_eventProcessors) {
				try {
					eventProcessor.process(event);
				} catch (EventProcessorException e) {
					LOG.warn("Unable to process event using processor {}; not processing with any later processors.", eventProcessor, e);
					break;
				} catch (Throwable t) {
					LOG.warn("Unknown exception processing event with processor {}; not processing with any later processors.", eventProcessor, t);
					break;
				}
			}
		}
	}
}
