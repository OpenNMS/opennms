/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;

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
     * @return a {@link org.opennms.netmgt.config.WebUser} object.
     */
    public WebUser getDefaultUser() {
        return m_defaultUser;
    }
    /**
     * <p>setDefaultUser</p>
     *
     * @param defaultUser a {@link org.opennms.netmgt.config.WebUser} object.
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
     * @return a {@link org.opennms.netmgt.config.WebGroup} object.
     */
    public WebGroup getMembershipGroup() {
        return m_membershipGroup;
    }
    /**
     * <p>setMembershipGroup</p>
     *
     * @param memberShipGroup a {@link org.opennms.netmgt.config.WebGroup} object.
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
    abstract public Collection<WebUser> getCurrentUsers();
    
    /**
     * <p>getWeeklyCalendar</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebCalendar} object.
     */
    public WebCalendar getWeeklyCalendar() {
        return null;
    }
    
    /**
     * <p>getCalendar</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebCalendar} object.
     */
    public WebCalendar getCalendar() {
        return getCalendar(new Date());
    }

    /**
     * <p>getCalendar</p>
     *
     * @param month a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.config.WebCalendar} object.
     */
    abstract public WebCalendar getCalendar(Date month);

    /**
     * <p>addEntry</p>
     *
     * @param entry a {@link org.opennms.netmgt.config.WebSchedEntry} object.
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
