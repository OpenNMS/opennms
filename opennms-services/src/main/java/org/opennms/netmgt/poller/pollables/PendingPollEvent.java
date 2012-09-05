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
 * @version $Id: $
 */
public class PendingPollEvent extends PollEvent {
    
    private Event m_event;
    private boolean m_pending = true;
    private List<Runnable> m_pendingOutages = new LinkedList<Runnable>();

    /**
     * <p>Constructor for PendingPollEvent.</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public PendingPollEvent(Event event) {
        super(Scope.fromUei(event.getUei()));
        m_event = event;
    }

    /**
     * <p>getDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getDate() {
        try {
            return EventConstants.parseToDate(m_event.getTime());
        } catch (ParseException e) {
            ThreadCategory.getInstance(getClass()).error("Unable to convert event time to date", e);
            return new Date();
        }
    }
    
    /**
     * <p>getEventId</p>
     *
     * @return a int.
     */
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
        return m_pending;
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
    
    //TODO: string builder or don't checking ;-)
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return m_event+", uei: "+m_event.getUei()+", id: "+m_event.getDbid()+", isPending: "+m_pending+", list size: "+m_pendingOutages.size();
    }
}
