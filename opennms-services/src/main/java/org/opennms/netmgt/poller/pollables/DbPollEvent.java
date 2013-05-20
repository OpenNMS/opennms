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

import java.util.Date;


/**
 * Represents a DbPollEvent
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class DbPollEvent extends PollEvent {
    
    int m_eventId;
    String m_uei;
    Date m_date;
    
    /**
     * <p>Constructor for DbPollEvent.</p>
     *
     * @param eventId a int.
     * @param uei a {@link java.lang.String} object.
     * @param date a {@link java.util.Date} object.
     */
    public DbPollEvent(int eventId, String uei, Date date) {
        super(Scope.fromUei(uei));
        m_eventId = eventId;
        m_date = date;
    }
    
    /**
     * <p>getEventId</p>
     *
     * @return a int.
     */
    @Override
    public int getEventId() {
        return m_eventId;
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
     * <p>hashCode</p>
     *
     * @return a int.
     */
    @Override
    public int hashCode() { return m_eventId; }
    
    /**
     * <p>equals</p>
     *
     * @param e a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     * @return a boolean.
     */
    public boolean equals(PollEvent e) {
        if (e == null) return false;
        return m_eventId == e.getEventId();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (o instanceof PollEvent)
            return equals((PollEvent)o);
        return false;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "DbPollEvent[ id: "+getEventId()+" ]";
    }

}
