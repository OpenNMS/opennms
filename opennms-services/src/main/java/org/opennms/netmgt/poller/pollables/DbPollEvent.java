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
 * Created: January 11, 2005
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

import java.util.Date;


/**
 * Represents a DbPollEvent 
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class DbPollEvent extends PollEvent {
    
    int m_eventId;
    String m_uei;
    Date m_date;
    
    public DbPollEvent(int eventId, String uei, Date date) {
        super(Scope.fromUei(uei));
        m_eventId = eventId;
        m_date = date;
    }
    
    public int getEventId() {
        return m_eventId;
    }
    
    public Date getDate() {
        return m_date;
    }
    
    public int hashCode() { return m_eventId; }
    
    public boolean equals(PollEvent e) {
        if (e == null) return false;
        return m_eventId == e.getEventId();
    }
    
    public boolean equals(Object o) {
        if (o instanceof PollEvent)
            return equals((PollEvent)o);
        return false;
    }
    
    public String toString() {
        return "DbPollEvent[ id: "+getEventId()+" ]";
    }

}
