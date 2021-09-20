/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.opennmsd;

import java.util.Date;

class MockNNMEvent implements NNMEvent {
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
        MockNNMEvent event = new MockNNMEvent();
        event.setCategory(category);
        event.setName(name);
        event.setSourceAddress(address);
        event.setSeverity(severity);
        event.setTimeStamp(new Date());
        return event;
    }

    
}