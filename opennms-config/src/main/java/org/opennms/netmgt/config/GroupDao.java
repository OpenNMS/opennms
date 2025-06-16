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
package org.opennms.netmgt.config;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.OwnedIntervalSequence;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;


/**
 * <p>GroupDao interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public interface GroupDao {
    /**
     * Set the groups data
     *
     * @param groups a {@link java.util.Map} object.
     */
    void setGroups(Map<String, Group> groups);

    /**
     * Get the groups
     *
     * @return a {@link java.util.Map} object.
     */
    Map<String, Group> getGroups();
    
    /**
     * Returns a boolean indicating if the group name appears in the xml file
     *
     * @return true if the group exists in the xml file, false otherwise
     * @param groupName a {@link java.lang.String} object.
     */
    boolean hasGroup(String groupName);

    /**
     * <p>getGroupNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<String> getGroupNames();

    /**
     * Get a group using its name
     *
     * @param name
     *            the name of the group to return
     * @return Group, the group specified by name
     */
    Group getGroup(String name);

    /**
     * <p>saveGroups</p>
     */
    void saveGroups();
    
    /**
     * Determines if a group is on duty at a given time. If a group has no duty schedules
     * listed in the config file, that group is assumed to always be on duty.
     *
     * @param group the group whose duty schedule we want
     * @param time the time to check for a duty schedule
     * @return boolean, true if the group is on duty, false otherwise.
     */
    boolean isGroupOnDuty(String group, Calendar time);
    
    /**
     * Determines when a group is next on duty. If a group has no duty schedules
     * listed in the config file, that group is assumed to always be on duty.
     *
     * @param group the group whose duty schedule we want
     * @param time the time to check for a duty schedule
     * @return long, the time in millisec until the group is next on duty
     */
    long groupNextOnDuty(String group, Calendar time);
    
    /**
     * Adds a new user and overwrites the "groups.xml"
     *
     * @param name a {@link java.lang.String} object.
     * @param details a {@link org.opennms.netmgt.config.groups.Group} object.
     */
    void saveGroup(String name, Group details);
    
    /**
     * <p>saveRole</p>
     *
     * @param role a {@link org.opennms.netmgt.config.groups.Role} object.
     */
    void saveRole(Role role);
    
    /**
     * Removes the user from the list of groups. Then overwrites to the
     * "groups.xml"
     *
     * @param name a {@link java.lang.String} object.
     */
    void deleteUser(String name);
    
    /**
     * Removes the group from the list of groups. Then overwrites to the
     * "groups.xml"
     *
     * @param name a {@link java.lang.String} object.
     */
    void deleteGroup(String name);
    
    /**
     * <p>deleteRole</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    void deleteRole(String name);
    
    /**
     * Renames the group from the list of groups. Then overwrites to the
     * "groups.xml"
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newName a {@link java.lang.String} object.
     */
    void renameGroup(String oldName, String newName);
    
    /**
     * When this method is called group name is changed, so also is the
     * groupname belonging to the view. Also overwrites the "groups.xml" file
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newName a {@link java.lang.String} object.
     */
    void renameUser(String oldName, String newName);
    
    /**
     * <p>getRoleNames</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    String[] getRoleNames();
    
    /**
     * <p>getRoles</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<Role> getRoles();
    
    /**
     * <p>getRole</p>
     *
     * @param roleName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.groups.Role} object.
     */
    Role getRole(String roleName);

    /**
     * <p>userHasRole</p>
     *
     * @param userId a {@link java.lang.String} object.
     * @param roleid a {@link java.lang.String} object.
     * @return a boolean.
     */
    boolean userHasRole(String userId, String roleid);
       
    /**
     * <p>getSchedulesForRoleAt</p>
     *
     * @param roleId a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @return a {@link java.util.List} object.
     */
    List<Schedule> getSchedulesForRoleAt(String roleId, Date time);
    
    /**
     * <p>getUserSchedulesForRole</p>
     *
     * @param userId a {@link java.lang.String} object.
     * @param roleid a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<Schedule> getUserSchedulesForRole(String userId, String roleid);
    
    /**
     * <p>isUserScheduledForRole</p>
     *
     * @param userId a {@link java.lang.String} object.
     * @param roleid a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @return a boolean.
     */
    boolean isUserScheduledForRole(String userId, String roleid, Date time);
    
    /**
     * <p>getRoleScheduleEntries</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @return a {@link org.opennms.core.utils.OwnedIntervalSequence} object.
     */
    OwnedIntervalSequence getRoleScheduleEntries(String roleid, Date start, Date end);
    
    /**
     * <p>findGroupsForUser</p>
     *
     * @param user a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    List<Group> findGroupsForUser(String user);
    
    /**
     * <p>getDefaultMapForUser</p>
     *
     * @param user a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getDefaultMapForUser(String user);
}
