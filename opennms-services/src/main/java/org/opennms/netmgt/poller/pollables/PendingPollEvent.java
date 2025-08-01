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
package org.opennms.netmgt.poller.pollables;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.core.sysprops.SystemProperties;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.poller.DefaultPollContext;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a PendingPollEvent
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class PendingPollEvent extends PollEvent {
    private static final Logger LOG = LoggerFactory.getLogger(PendingPollEvent.class);

    // how long to wait, in milliseconds, before giving up on waiting for a poll event to get an event ID, defaults to 10 minutes
    private static final long PENDING_EVENT_TIMEOUT = SystemProperties.getLong("org.opennms.netmgt.poller.pendingEventTimeout", 1000L * 60L * 10L);

    private IEvent m_event;
    private final Date m_date;
    private long m_expirationTimeInMillis;
    private final AtomicBoolean m_pending = new AtomicBoolean(true);
    private final Queue<Runnable> m_pendingOutages = new ConcurrentLinkedQueue<>();

    /**
     * <p>Constructor for PendingPollEvent.</p>
     *
     * @param event a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    public PendingPollEvent(final IEvent event) {
        super(Scope.fromUei(event.getUei()));
        m_event = event;
        m_date = m_event.getTime();
        m_expirationTimeInMillis = m_date.getTime() + PENDING_EVENT_TIMEOUT;
    }

    /**
     * <p>getDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Override
    public Date getDate() {
        return m_date;
    }
    
    /**
     * <p>getEventId</p>
     *
     * @return a int.
     */
    @Override
    public long getEventId() {
        return m_event.getDbid();
    }
    
    /**
     * <p>addPending</p>
     *
     * @param r a {@link java.lang.Runnable} object.
     */
    public void addPending(Runnable r) {
        if (m_pending.get()) {
            m_pendingOutages.add(r);
        } else {
            r.run();
        }
    }
    
    /**
     * <p>getEvent</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    public IEvent getEvent() {
        return m_event;
    }
    
    /**
     * <p>isPending</p>
     *
     * @return a boolean.
     */

    public boolean isPending() {
        if (m_pending.get()) {
            // still pending, check if we've timed out
            if (isTimedOut()) {
                m_pending.set(false);
            }
        }
        return m_pending.get();
    }

    boolean isTimedOut() {
        return System.currentTimeMillis() > m_expirationTimeInMillis;
    }

    /**
     * Changes the state of this event from "pending" to "not pending".
     * It is important that this call be thread-safe and idempotent because
     * it may be invoked by multiple {@link DefaultPollContext#onEvent(IEvent)}
     * threads.
     *
     * @param e a {@link org.opennms.netmgt.events.api.model.IEvent} object.
     */
    public void complete(IEvent e) {
        m_event = e;
        m_pending.set(false);
    }
    
    /**
     * Synchronously processes all pending tasks attached to this event.
     * It is important that this call be thread-safe and idempotent because
     * it may be invoked by multiple {@link DefaultPollContext#onEvent(Event)}
     * threads.
     */
    public void processPending() {
        while (!m_pendingOutages.isEmpty()) {
            Runnable runnable = m_pendingOutages.poll();
            if (runnable != null) {
                runnable.run();
            } else {
                return;
            }
        }
    }
    
    public String toString() {
        return m_event+", uei: "+m_event.getUei()+", id: "+m_event.getDbid()+", isPending: "+m_pending.get()+", list size: "+m_pendingOutages.size();
    }

    // for unit testing
    void setExpirationTimeInMillis(final long time) {
        m_expirationTimeInMillis = time;
    }
}
