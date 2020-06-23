/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.group;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.config.groups.Group;

/**
 * WebGroup
 *
 * @author brozow
 * @version $Id: $
 * @since 1.8.1
 */
public class WebGroup {
    
    private String m_name;
    private String m_comments;

    private List<String> m_dutySchedules = new ArrayList<>();
    private List<String> m_authorizedCategories = new ArrayList<>();
    private List<String> m_users = new ArrayList<>();
    
    /**
     * <p>Constructor for WebGroup.</p>
     */
    public WebGroup() {
    }

    /**
     * <p>Constructor for WebGroup.</p>
     *
     * @param group a {@link org.opennms.netmgt.config.groups.Group} object.
     * @param authorizedCategories a {@link java.util.List} object.
     */
    public WebGroup(Group group, List<String> authorizedCategories) {
        m_name = group.getName();
        m_comments = group.getComments().orElse(null);
        m_dutySchedules.addAll(group.getDutySchedules());
        m_users.addAll(group.getUsers());
        m_authorizedCategories.addAll(authorizedCategories);
    }
    
    /**
     * <p>getName</p>
     *
     * @return the name
     */
    public String getName() {
        return m_name;
    }
    /**
     * <p>setName</p>
     *
     * @param name the name to set
     */
    public void setName(String name) {
        m_name = name;
    }
    /**
     * <p>getComments</p>
     *
     * @return the comments
     */
    public String getComments() {
        return m_comments;
    }
    /**
     * <p>setComments</p>
     *
     * @param comments the comments to set
     */
    public void setComments(String comments) {
        m_comments = comments;
    }
    /**
     * <p>getDutySchedules</p>
     *
     * @return the dutySchedules
     */
    public List<String> getDutySchedules() {
        return m_dutySchedules;
    }
    /**
     * <p>setDutySchedules</p>
     *
     * @param dutySchedules the dutySchedules to set
     */
    public void setDutySchedules(List<String> dutySchedules) {
        m_dutySchedules = dutySchedules;
    }
    
    /**
     * <p>addDutySchedule</p>
     *
     * @param dutySchedule a {@link java.lang.String} object.
     */
    public void addDutySchedule(String dutySchedule) {
        m_dutySchedules.add(dutySchedule);
    }
    
    /**
     * <p>getAuthorizedCategories</p>
     *
     * @return the authorizedCategories
     */
    public List<String> getAuthorizedCategories() {
        return m_authorizedCategories;
    }
    /**
     * <p>setAuthorizedCategories</p>
     *
     * @param authorizedCategories the authorizedCategories to set
     */
    public void setAuthorizedCategories(List<String> authorizedCategories) {
        m_authorizedCategories = authorizedCategories;
    }
    
    /**
     * <p>getUsers</p>
     *
     * @return the users
     */
    public List<String> getUsers() {
        return m_users;
    }

    /**
     * <p>setUsers</p>
     *
     * @param users the users to set
     */
    public void setUsers(List<String> users) {
        m_users = users;
    }

    /**
     * <p>getUnauthorizedCategories</p>
     *
     * @param allCategories a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public List<String> getUnauthorizedCategories(List<String> allCategories) {
        List<String> unauthorizedCategories = new ArrayList<String>(allCategories);
        unauthorizedCategories.removeAll(m_authorizedCategories);
        return unauthorizedCategories;
    }
    
    /**
     * <p>getRemainingUsers</p>
     *
     * @param allUsers a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    public List<String> getRemainingUsers(List<String> allUsers) {
        List<String> remainingUsers = new ArrayList<String>(allUsers);
        remainingUsers.removeAll(m_users);
        return remainingUsers;
    }
}
