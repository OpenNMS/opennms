/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2005-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.admin.roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.groups.Schedule;

public abstract class WebRole {
    
    private String m_name;
    private String m_description;
    private WebUser m_defaultUser;
    private WebGroup m_membershipGroup;
    private List<WebSchedEntry> m_newEntries = new ArrayList<WebSchedEntry>();
    
    
    public WebRole() {
    }
    
    public WebRole(String name) {
        m_name = name;
    }

    public WebUser getDefaultUser() {
        return m_defaultUser;
    }
    public void setDefaultUser(WebUser defaultUser) {
        m_defaultUser = defaultUser;
    }
    public String getDescription() {
        return m_description;
    }
    public void setDescription(String description) {
        m_description = description;
    }
    public WebGroup getMembershipGroup() {
        return m_membershipGroup;
    }
    public void setMembershipGroup(WebGroup memberShipGroup) {
        m_membershipGroup = memberShipGroup;
    }
    public String getName() {
        return m_name;
    }
    
    public void setName(String name) {
        m_name = name;
    }
    
    abstract public Schedule getSchedule(int schedIndex);
    
    abstract public Time getTime(int schedIndex, int timeIndex);

    abstract public Collection getCurrentUsers();
    
    public WebCalendar getWeeklyCalendar() {
        return null;
    }
    
    public WebCalendar getCalendar() {
        return getCalendar(new Date());
    }

    abstract public WebCalendar getCalendar(Date month);

    public void addEntry(WebSchedEntry entry) {
        
        m_newEntries.add(entry);
    }
    
    public Collection<WebSchedEntry> getNewEntries() {
        return m_newEntries;
    }
    
    

}
