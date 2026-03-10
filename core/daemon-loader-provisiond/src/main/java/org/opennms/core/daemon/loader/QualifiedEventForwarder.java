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
package org.opennms.core.daemon.loader;

import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;

/**
 * Simple delegating {@link EventForwarder} that exists solely to satisfy
 * {@code @Qualifier("transactionAware")} injection points in standalone
 * daemon containers.
 *
 * <p>In the monolithic OpenNMS, {@code TransactionAwareEventForwarder}
 * defers event sending until after JPA transaction commit. In standalone
 * containers, events go straight to Kafka (no JPA transaction coordination
 * needed), so this class simply delegates to the underlying EventForwarder.</p>
 */
public class QualifiedEventForwarder implements EventForwarder {

    private final EventForwarder delegate;

    public QualifiedEventForwarder(EventForwarder delegate) {
        this.delegate = delegate;
    }

    @Override
    public void sendNow(Event event) {
        delegate.sendNow(event);
    }

    @Override
    public void sendNow(Log eventLog) {
        delegate.sendNow(eventLog);
    }

    @Override
    public void sendNowSync(Event event) {
        delegate.sendNowSync(event);
    }

    @Override
    public void sendNowSync(Log eventLog) {
        delegate.sendNowSync(eventLog);
    }
}
