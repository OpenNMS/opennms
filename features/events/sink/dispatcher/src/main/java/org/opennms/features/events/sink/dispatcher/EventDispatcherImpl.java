/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.events.sink.dispatcher;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.ipc.sink.api.MessageDispatcherFactory;
import org.opennms.features.events.sink.module.EventSinkModule;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class is used to send events from minion, not exposed on OpenNMS.
 */
public class EventDispatcherImpl implements EventForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(EventDispatcherImpl.class);

    private MessageDispatcherFactory messageDispatcherFactory;

    private AsyncDispatcher<Event> asyncDispatcher;

    private EventdConfig eventdConfig;


    public EventDispatcherImpl(MessageDispatcherFactory messageDispatcherFactory, EventdConfig eventdConfig) {
        this.messageDispatcherFactory = messageDispatcherFactory;
        this.eventdConfig = eventdConfig;
    }

    @Override
    public void sendNow(Event event) {
        try {
            getAsyncDispatcher().send(event).whenComplete((t, ex) -> {
                if (ex != null) {
                    LOG.error("Failed to sent Event with uei = {}", event.getUei(), ex);
                }
            });
        } catch (Exception e) {
            LOG.error("Failed to sent Event with uei = {}", event.getUei(), e);
        }
    }

    @Override
    public void sendNow(Log eventLog) {
        eventLog.getEvents().getEventCollection().forEach(this::sendNow);
    }

    @Override
    public void sendNowSync(Event event) {
        sendNow(event);
    }

    @Override
    public void sendNowSync(Log eventLog) {
        sendNow(eventLog);
    }

    public AsyncDispatcher<Event> getAsyncDispatcher() {
        if (asyncDispatcher == null) {
            asyncDispatcher = messageDispatcherFactory.createAsyncDispatcher(new EventSinkModule(eventdConfig));
        }
        return asyncDispatcher;
    }
}
