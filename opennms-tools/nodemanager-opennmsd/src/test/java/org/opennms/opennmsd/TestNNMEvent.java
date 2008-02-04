/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.opennmsd;

import java.util.Date;

class TestNNMEvent implements NNMEvent {
    private String m_category;
    private String m_name;
    private String m_severity;
    private String m_sourceAddress;
    private Date m_timeStamp;
    
    public String getCategory() {
        return m_category;
    }
    public void setCategory(String category) {
        m_category = category;
    }
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }
    public String getSeverity() {
        return m_severity;
    }
    public void setSeverity(String severity) {
        m_severity = severity;
    }
    public String getSourceAddress() {
        return m_sourceAddress;
    }
    public void setSourceAddress(String sourceAddress) {
        m_sourceAddress = sourceAddress;
    }
    public Date getTimeStamp() {
        return m_timeStamp;
    }
    public void setTimeStamp(Date timeStamp) {
        m_timeStamp = timeStamp;
    }
    
    public static NNMEvent createEvent(String category, String severity,
            String name, String address) {
        TestNNMEvent event = new TestNNMEvent();
        event.setCategory(category);
        event.setName(name);
        event.setSourceAddress(address);
        event.setSeverity(severity);
        event.setTimeStamp(new Date());
        return event;
    }

    
}