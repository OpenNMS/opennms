/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.pollables;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PendingPollEvent
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class PendingPollEvent extends PollEvent {
    // how long to wait, in milliseconds, before giving up on waiting for a poll event to get an event ID, defaults to 10 minutes
    private static final long PENDING_EVENT_TIMEOUT = Long.getLong("org.opennms.netmgt.poller.pendingEventTimeout", 1000L * 60L * 10L);

    private final Event m_event;
    private Date m_date;
    private long m_expirationTimeInMillis;
    private boolean m_pending = true;
    private List<Runnable> m_pendingOutages = new LinkedList<Runnable>();

    /**
     * <p>Constructor for PendingPollEvent.</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public PendingPollEvent(final Event event) {
        super(Scope.fromUei(event.getUei()));
        m_event = event;
        try {
            m_date = EventConstants.parseToDate(m_event.getTime());
        } catch (final ParseException e) {
            ThreadCategory.getInstance(getClass()).error("Unable to convert event time to date", e);
            m_date = new Date();
        }
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
        if (m_pending)
            m_pendingOutages.add(r);
        else
            r.run();
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
        if (m_pending) {
            // still pending, check if we've timed out
            if (isTimedOut()) {
                m_pending = false;
            }
        }
        return m_pending;
    }

    boolean isTimedOut() {
        return System.currentTimeMillis() > m_expirationTimeInMillis;
    }

    /**
     * <p>complete</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public void complete(Event e) {
        m_pending = false;
    }
    
    /**
     * <p>processPending</p>
     */
    public void processPending() {
        for (Runnable r : m_pendingOutages) {
            r.run();
        }
        m_pendingOutages.clear();
        
    }
    
    public String toString() {
        return m_event+", uei: "+m_event.getUei()+", id: "+m_event.getDbid()+", isPending: "+m_pending+", list size: "+m_pendingOutages.size();
    }

    // for unit testing
    void setExpirationTimeInMillis(final long time) {
        m_expirationTimeInMillis = time;
    }
}
