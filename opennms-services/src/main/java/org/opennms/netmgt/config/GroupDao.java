/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 1, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public void setGroups(Map<String, Group> groups);

    /**
     * Get the groups
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Group> getGroups();
    
    /**
     * Returns a boolean indicating if the group name appears in the xml file
     *
     * @return true if the group exists in the xml file, false otherwise
     * @param groupName a {@link java.lang.String} object.
     */
    public boolean hasGroup(String groupName);

    /**
     * <p>getGroupNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getGroupNames();

    /**
     * Get a group using its name
     *
     * @param name
     *            the name of the group to return
     * @return Group, the group specified by name
     */
    public Group getGroup(String name);

    /**
     * <p>saveGroups</p>
     */
    public void saveGroups();
    
    /**
     * Determines if a group is on duty at a given time. If a group has no duty schedules
     * listed in the config file, that group is assumed to always be on duty.
     *
     * @param group the group whose duty schedule we want
     * @param time the time to check for a duty schedule
     * @return boolean, true if the group is on duty, false otherwise.
     */
    public boolean isGroupOnDuty(String group, Calendar time);
    
    /**
     * Determines when a group is next on duty. If a group has no duty schedules
     * listed in the config file, that group is assumed to always be on duty.
     *
     * @param group the group whose duty schedule we want
     * @param time the time to check for a duty schedule
     * @return long, the time in millisec until the group is next on duty
     */
    public long groupNextOnDuty(String group, Calendar time);
    
    /**
     * Adds a new user and overwrites the "groups.xml"
     *
     * @param name a {@link java.lang.String} object.
     * @param details a {@link org.opennms.netmgt.config.groups.Group} object.
     */
    public void saveGroup(String name, Group details);
    
    /**
     * <p>saveRole</p>
     *
     * @param role a {@link org.opennms.netmgt.config.groups.Role} object.
     */
    public void saveRole(Role role);
    
    /**
     * Removes the user from the list of groups. Then overwrites to the
     * "groups.xml"
     *
     * @param name a {@link java.lang.String} object.
     */
    public void deleteUser(String name);
    
    /**
     * Removes the group from the list of groups. Then overwrites to the
     * "groups.xml"
     *
     * @param name a {@link java.lang.String} object.
     */
    public void deleteGroup(String name);
    
    /**
     * <p>deleteRole</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void deleteRole(String name);
    
    /**
     * Renames the group from the list of groups. Then overwrites to the
     * "groups.xml"
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newName a {@link java.lang.String} object.
     */
    public void renameGroup(String oldName, String newName);
    
    /**
     * When this method is called group name is changed, so also is the
     * groupname belonging to the view. Also overwrites the "groups.xml" file
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newName a {@link java.lang.String} object.
     */
    public void renameUser(String oldName, String newName);
    
    /**
     * <p>getRoleNames</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getRoleNames();
    
    /**
     * <p>getRoles</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection getRoles();
    
    /**
     * <p>getRole</p>
     *
     * @param roleName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.groups.Role} object.
     */
    public Role getRole(String roleName);

    /**
     * <p>userHasRole</p>
     *
     * @param userId a {@link java.lang.String} object.
     * @param roleid a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean userHasRole(String userId, String roleid);
       
    /**
     * <p>getSchedulesForRoleAt</p>
     *
     * @param roleId a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @return a {@link java.util.List} object.
     */
    public List<Schedule> getSchedulesForRoleAt(String roleId, Date time);
    
    /**
     * <p>getUserSchedulesForRole</p>
     *
     * @param userId a {@link java.lang.String} object.
     * @param roleid a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List getUserSchedulesForRole(String userId, String roleid);
    
    /**
     * <p>isUserScheduledForRole</p>
     *
     * @param userId a {@link java.lang.String} object.
     * @param roleid a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @return a boolean.
     */
    public boolean isUserScheduledForRole(String userId, String roleid, Date time);
    
    /**
     * <p>getRoleScheduleEntries</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.config.OwnedIntervalSequence} object.
     */
    public OwnedIntervalSequence getRoleScheduleEntries(String roleid, Date start, Date end);
    
    /**
     * <p>findGroupsForUser</p>
     *
     * @param user a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<Group> findGroupsForUser(String user);
}
