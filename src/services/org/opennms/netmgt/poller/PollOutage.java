//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.poller;

import java.util.Date;

import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollOutage 
 *
 * @author brozow
 */
public class PollOutage {
    
    private Event m_lostEvent;
    private Date m_lostDate;
    private Event m_regainEvent;
    private Date m_regainDate;
    private PollableElement m_element;

    /**
     * @param element
     * @param e
     * @param date
     */
    public PollOutage(PollableElement element, Event lostEvent, Date lostDate) {
        m_element = element;
        m_lostEvent = lostEvent;
        m_lostDate = lostDate;
    }

    /**
     * @param e
     * @param date
     */
    public void resolve(Event regainEvent, Date regainDate) {
        m_regainEvent = regainEvent;
        m_regainDate = regainDate;
    }

    /**
     * 
     */
    public void open() {
        
    }


    public PollableElement getElement() {
        return m_element;
    }
    public void setElement(PollableElement element) {
        m_element = element;
    }
    public Date getLostDate() {
        return m_lostDate;
    }
    public void setLostDate(Date lostDate) {
        m_lostDate = lostDate;
    }
    public Event getLostEvent() {
        return m_lostEvent;
    }
    public void setLostEvent(Event lostEvent) {
        m_lostEvent = lostEvent;
    }
    public Date getRegainDate() {
        return m_regainDate;
    }
    public void setRegainDate(Date regainDate) {
        m_regainDate = regainDate;
    }
    public Event getRegainEvent() {
        return m_regainEvent;
    }
    public void setRegainEvent(Event regainEvent) {
        m_regainEvent = regainEvent;
    }
}
