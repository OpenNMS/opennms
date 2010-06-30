//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
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
package org.opennms.web.admin.roles;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.groups.Schedule;

/**
 * <p>Abstract WebRole class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class WebRole {
    
    private String m_name;
    private String m_description;
    private WebUser m_defaultUser;
    private WebGroup m_membershipGroup;
    private List<WebSchedEntry> m_newEntries = new ArrayList<WebSchedEntry>();
    
    
    /**
     * <p>Constructor for WebRole.</p>
     */
    public WebRole() {
    }
    
    /**
     * <p>Constructor for WebRole.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public WebRole(String name) {
        m_name = name;
    }

    /**
     * <p>getDefaultUser</p>
     *
     * @return a {@link org.opennms.web.admin.roles.WebUser} object.
     */
    public WebUser getDefaultUser() {
        return m_defaultUser;
    }
    /**
     * <p>setDefaultUser</p>
     *
     * @param defaultUser a {@link org.opennms.web.admin.roles.WebUser} object.
     */
    public void setDefaultUser(WebUser defaultUser) {
        m_defaultUser = defaultUser;
    }
    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_description;
    }
    /**
     * <p>setDescription</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        m_description = description;
    }
    /**
     * <p>getMembershipGroup</p>
     *
     * @return a {@link org.opennms.web.admin.roles.WebGroup} object.
     */
    public WebGroup getMembershipGroup() {
        return m_membershipGroup;
    }
    /**
     * <p>setMembershipGroup</p>
     *
     * @param memberShipGroup a {@link org.opennms.web.admin.roles.WebGroup} object.
     */
    public void setMembershipGroup(WebGroup memberShipGroup) {
        m_membershipGroup = memberShipGroup;
    }
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }
    
    /**
     * <p>getSchedule</p>
     *
     * @param schedIndex a int.
     * @return a {@link org.opennms.netmgt.config.groups.Schedule} object.
     */
    abstract public Schedule getSchedule(int schedIndex);
    
    /**
     * <p>getTime</p>
     *
     * @param schedIndex a int.
     * @param timeIndex a int.
     * @return a {@link org.opennms.netmgt.config.common.Time} object.
     */
    abstract public Time getTime(int schedIndex, int timeIndex);

    /**
     * <p>getCurrentUsers</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    abstract public Collection getCurrentUsers();
    
    /**
     * <p>getWeeklyCalendar</p>
     *
     * @return a {@link org.opennms.web.admin.roles.WebCalendar} object.
     */
    public WebCalendar getWeeklyCalendar() {
        return null;
    }
    
    /**
     * <p>getCalendar</p>
     *
     * @return a {@link org.opennms.web.admin.roles.WebCalendar} object.
     */
    public WebCalendar getCalendar() {
        return getCalendar(new Date());
    }

    /**
     * <p>getCalendar</p>
     *
     * @param month a {@link java.util.Date} object.
     * @return a {@link org.opennms.web.admin.roles.WebCalendar} object.
     */
    abstract public WebCalendar getCalendar(Date month);

    /**
     * <p>addEntry</p>
     *
     * @param entry a {@link org.opennms.web.admin.roles.WebSchedEntry} object.
     */
    public void addEntry(WebSchedEntry entry) {
        
        m_newEntries.add(entry);
    }
    
    /**
     * <p>getNewEntries</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<WebSchedEntry> getNewEntries() {
        return m_newEntries;
    }
    
    

}
