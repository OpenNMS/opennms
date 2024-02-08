/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
