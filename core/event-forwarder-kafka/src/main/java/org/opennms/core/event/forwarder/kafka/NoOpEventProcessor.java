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
package org.opennms.core.event.forwarder.kafka;

import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.xml.event.Log;

/**
 * No-op {@link EventProcessor} used in daemon containers where event
 * expansion (eventconf lookup) is not available. Events pass through
 * un-expanded; the core side handles expansion after Kafka consumption.
 */
public class NoOpEventProcessor implements EventProcessor {

    @Override
    public void process(Log eventLog) throws EventProcessorException {
        // no-op
    }

    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        // no-op
    }
}
