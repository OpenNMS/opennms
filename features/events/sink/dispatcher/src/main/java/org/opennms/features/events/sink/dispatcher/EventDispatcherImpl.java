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
