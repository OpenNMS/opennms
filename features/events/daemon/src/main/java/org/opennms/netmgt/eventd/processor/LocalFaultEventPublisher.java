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

import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local-mode replacement for {@link FaultEventPublisher} when Kafka is not
 * configured. Logs fault events at DEBUG level instead of publishing to Kafka.
 *
 * This allows the EventRouter pipeline to function without Kafka infrastructure.
 * When Kafka is enabled via Karaf feature, the real FaultEventPublisher bean
 * will replace this one.
 */
public class LocalFaultEventPublisher implements EventProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(LocalFaultEventPublisher.class);

    @Override
    public void process(Log eventLog) throws EventProcessorException {
        process(eventLog, false);
    }

    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        if (eventLog.getEvents() == null) {
            return;
        }
        for (Event event : eventLog.getEvents().getEvent()) {
            LOG.debug("Local fault event (Kafka not configured): uei={} dbid={} node={}",
                    event.getUei(), event.getDbid(), event.getNodeid());
        }
    }
}
