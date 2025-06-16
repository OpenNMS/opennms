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
package org.opennms.netmgt.eventd.sink;

import javax.annotation.PostConstruct;

import org.opennms.core.ipc.sink.api.MessageConsumer;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.logging.Logging;
import org.opennms.features.events.sink.module.EventSinkModule;
import org.opennms.netmgt.config.api.EventdConfig;
import org.opennms.netmgt.eventd.Eventd;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Malatesh Sudarshan
 */
public class EventSinkConsumer implements MessageConsumer<Event, Log> {

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
    public SinkModule<Event, Log> getModule() {
        return new EventSinkModule(m_config);
    }

    @Override
    public void handleMessage(Log eventLog) {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(Eventd.LOG4J_CATEGORY)) {
            eventForwarder.sendNowSync(eventLog);
        }

    }

    public void setconfig(EventdConfig m_config) {
        this.m_config = m_config;
    }

    public void setMessageConsumerManager(
            MessageConsumerManager messageConsumerManager) {
        this.messageConsumerManager = messageConsumerManager;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }
}
