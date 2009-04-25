/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.web.group;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.groups.Group;

/**
 * WebGroup
 *
 * @author brozow
 */
public class WebGroup {
    
    private String m_name;
    private String m_comments;
    private List<String> m_dutySchedules = new ArrayList<String>();
    private List<String> m_authorizedCategories = new ArrayList<String>();
    private List<String> m_users = new ArrayList<String>();
    
    public WebGroup() {
    }

    public WebGroup(Group group, List<String> authorizedCategories) {
        m_name = group.getName();
        m_comments = group.getComments();
        m_dutySchedules.addAll(group.getDutyScheduleCollection());
        m_users.addAll(group.getUserCollection());
        m_authorizedCategories.addAll(authorizedCategories);
    }
    
    /**
     * @return the name
     */
    public String getName() {
        return m_name;
    }
    /**
     * @param name the name to set
     */
    public void setName(String name) {
        m_name = name;
    }
    /**
     * @return the comments
     */
    public String getComments() {
        return m_comments;
    }
    /**
     * @param comments the comments to set
     */
    public void setComments(String comments) {
        m_comments = comments;
    }
    /**
     * @return the dutySchedules
     */
    public List<String> getDutySchedules() {
        return m_dutySchedules;
    }
    /**
     * @param dutySchedules the dutySchedules to set
     */
    public void setDutySchedules(List<String> dutySchedules) {
        m_dutySchedules = dutySchedules;
    }
    
    public void addDutySchedule(String dutySchedule) {
        m_dutySchedules.add(dutySchedule);
    }
    
    /**
     * @return the authorizedCategories
     */
    public List<String> getAuthorizedCategories() {
        return m_authorizedCategories;
    }
    /**
     * @param authorizedCategories the authorizedCategories to set
     */
    public void setAuthorizedCategories(List<String> authorizedCategories) {
        m_authorizedCategories = authorizedCategories;
    }
    
    /**
     * @return the users
     */
    public List<String> getUsers() {
        return m_users;
    }

    /**
     * @param users the users to set
     */
    public void setUsers(List<String> users) {
        m_users = users;
    }

    public List<String> getUnauthorizedCategories(List<String> allCategories) {
        List<String> unauthorizedCategories = new ArrayList<String>(allCategories);
        unauthorizedCategories.removeAll(m_authorizedCategories);
        return unauthorizedCategories;
    }
    
    public List<String> getRemainingUsers(List<String> allUsers) {
        List<String> remainingUsers = new ArrayList<String>(allUsers);
        remainingUsers.removeAll(m_users);
        return remainingUsers;
    }
    
    
}
