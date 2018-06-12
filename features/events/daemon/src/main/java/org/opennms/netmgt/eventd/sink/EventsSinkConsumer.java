/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import javax.annotation.PostConstruct;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.eventd.Eventd;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventsWrapper;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.beans.factory.annotation.Autowired;

public class EventsSinkConsumer implements MessageConsumer<EventsWrapper, Log> {

	@Autowired
    private EventdConfig m_config;
	
	@PostConstruct
	public void init() throws Exception {
		messageConsumerManager.registerConsumer(this);
	}

	@Autowired
	private MessageConsumerManager messageConsumerManager;

	@Autowired
	private EventForwarder eventForwarder;

	@Override
	public SinkModule<EventsWrapper, Log> getModule() {
		return new EventsModule(m_config);
	}

	@Override
	public void handleMessage(Log eventLog) {
		try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(Eventd.LOG4J_CATEGORY)) {
			eventForwarder.sendNowSync(eventLog);
		}

	}
}
