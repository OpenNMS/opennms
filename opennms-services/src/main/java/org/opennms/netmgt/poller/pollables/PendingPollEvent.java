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

package org.opennms.netmgt.poller.pollables;

import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static final long PENDING_EVENT_TIMEOUT = Long.getLong("org.opennms.netmgt.poller.pendingEventTimeout", 1000L * 60L * 10L);

    private final Event m_event;
    private final Date m_date;
    private long m_expirationTimeInMillis;
    private final AtomicBoolean m_pending = new AtomicBoolean(true);
    private final Queue<Runnable> m_pendingOutages = new ConcurrentLinkedQueue<>();

    /**
     * <p>Constructor for PendingPollEvent.</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public PendingPollEvent(final Event event) {
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
    public int getEventId() {
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
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event getEvent() {
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
     * it may be invoked by multiple {@link DefaultPollContext#onEvent(Event)}
     * threads.
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void complete(Event e) {
        m_pending.set(false);
    }
    
    /**
     * Synchronously processes all pending tasks attached to this event.
     * It is important that this call be thread-safe and idempotent because
     * it may be invoked by multiple {@link DefaultPollContext#onEvent(Event)}
     * threads.
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
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
