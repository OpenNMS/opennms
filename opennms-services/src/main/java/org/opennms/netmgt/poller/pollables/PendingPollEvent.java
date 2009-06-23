/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 5, 2005
 *
 * Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
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
    
    private Event m_event;
    private boolean m_pending = true;
    private List<Runnable> m_pendingOutages = new LinkedList<Runnable>();

    public PendingPollEvent(Event event) {
        super(Scope.fromUei(event.getUei()));
        m_event = event;
    }

    public Date getDate() {
        try {
            return EventConstants.parseToDate(m_event.getTime());
        } catch (ParseException e) {
            ThreadCategory.getInstance(getClass()).error("Unable to convert event time to date", e);
            return new Date();
        }
    }
    
    public int getEventId() {
        return m_event.getDbid();
    }
    
    public void addPending(Runnable r) {
        if (m_pending)
            m_pendingOutages.add(r);
        else
            r.run();
    }
    
    public Event getEvent() {
        return m_event;
    }
    
    public boolean isPending() {
        return m_pending;
    }

    public void complete(Event e) {
        m_pending = false;
    }
    
    public void processPending() {
        for (Runnable r : m_pendingOutages) {
            r.run();
        }
        m_pendingOutages.clear();
        
    }
    
    //TODO: string builder or don't checking ;-)
    public String toString() {
        return m_event+", uei: "+m_event.getUei()+", id: "+m_event.getDbid()+", isPending: "+m_pending+", list size: "+m_pendingOutages.size();
    }
}
