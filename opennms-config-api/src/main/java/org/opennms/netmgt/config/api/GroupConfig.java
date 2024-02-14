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
package org.opennms.netmgt.config.api;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.OwnedIntervalSequence;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.model.OnmsGroup;
import org.opennms.netmgt.model.OnmsGroupList;

/**
 * An interface for GroupManager
 * 
 * @author <a href="ryan@mail1.opennms.com"> Ryan Lambeth </a>
 *
 */
public interface GroupConfig {

	/**
	 * <p>setGroups</p>
	 * 
	 * @param a Map<String, Group>
	 */
	void setGroups(Map<String, Group> grp);
	
	/**
	 * <p>getGroups</p>
	 * 
	 * @return a Map<String, Group>
	 * @throws IOException
	 */
	Map<String, Group> getGroups() throws IOException;
	
	/**
	 * <p>getOnmsGroupList</p>
	 * 
	 * @return an OnmsGroupList
	 * @throws IOException
	 */
	OnmsGroupList getOnmsGroupList() throws IOException;
	
	/**
	 * <p>getOnmsGroup</p>
	 * 
	 * @param a String
	 * @return an OnmsGroup
	 * @throws IOException
	 */
	OnmsGroup getOnmsGroup(final String groupName) throws IOException;
	
	/**
	 * <p>save</p>
	 * 
	 * @param an OnmsGroup
	 * @throws Exception
	 */
	void save(final OnmsGroup group) throws Exception;
	
	/**
	 * <p>hasGroup</p>
	 * 
	 * @param a String
	 * @return a boolean
	 * @throws IOException
	 */
	boolean hasGroup(String groupName) throws IOException;
	
	/**
	 * <p>getGroupNames</p>
	 * 
	 * @return a List<String>
	 * @throws IOException
	 */
	List<String> getGroupNames() throws IOException;
	
	/**
	 * <p>getGroup</p>
	 * 
	 * @param a String
	 * @return a Group
	 * @throws IOException
	 */
	Group getGroup(String name) throws IOException;
	
	/**
	 * <p>saveGroups</p>
	 * 
	 * @throws Exception
	 */
	void saveGroups() throws Exception;
	
	/**
	 * <p>isGroupOnDuty</p>
	 * 
	 * @param a String
	 * @param a Calendar
	 * @return a boolean
	 * @throws IOException
	 */
	boolean isGroupOnDuty(String group, Calendar time) throws IOException;
	
	/**
	 * <p>groupNextOnDuty</p>
	 * 
	 * @param a String
	 * @param a Calendar
	 * @return a long
	 * @throws IOException
	 */
	long groupNextOnDuty(String group, Calendar time) throws IOException;
	
	/**
	 * <p>saveGroup</p>
	 * 
	 * @param a String
	 * @param a Group
	 * @throws Exception
	 */
	void saveGroup(String name, Group details) throws Exception;
	
	/**
	 * <p>saveRole</p>
	 * 
	 * @param a Role
	 * @throws Exception
	 */
	void saveRole(Role role) throws Exception;
	
	/**
	 * <p>deleteUser</p>
	 * 
	 * @param a String
	 * @throws Exception
	 */
	void deleteUser(String name) throws Exception;
	
	/**
	 * <p>deleteGroup</p>
	 * 
	 * @param a String
	 * @throws Exception
	 */
	void deleteGroup(String name) throws Exception;
	
	/**
	 * <p>deleteRole</p>
	 * 
	 * @param a String
	 * @throws Exception
	 */
	void deleteRole(String name) throws Exception;
	
	/**
	 * <p>renameGroup</p>
	 * 
	 * @param a String
	 * @param a String
	 * @throws Exception
	 */
	void renameGroup(String oldName, String newName) throws Exception;
	
	/**
	 * <p>renameUser</p>
	 * 
	 * @param a String
	 * @param a String
	 * @throws Exception
	 */
	void renameUser(String oldName, String newName) throws Exception;
	
	/**
	 * <p>getRoleNames</p>
	 * 
	 * @return a String[]
	 */
	String[] getRoleNames();
	
	/**
	 * <p>getRoles</p>
	 * 
	 * @return a Collection<Role>
	 */
	Collection<Role> getRoles();
	
	/**
	 * <p>getRole</p>
	 * 
	 * @param a String
	 * @return a Role
	 */
	Role getRole(String roleName);
	
	/**
	 * <p>userHasRole</p>
	 * 
	 * @param a String
	 * @param a String
	 * @return a boolean
	 * @throws IOException
	 */
	boolean userHasRole(String userId, String roleid) throws IOException;
	
	/**
	 * <p>getSchedulesForRoleAt</p>
	 * 
	 * @param a String
	 * @param a String
	 * @return a List<Schedule>
	 * @throws IOException
	 */
	List<Schedule> getSchedulesForRoleAt(String roleId, Date time) throws IOException;
	
	/**
	 * <p>getUserSchedulesForRole</p>
	 * 
	 * @param a String
	 * @param a String
	 * @return a List<Schedule>
	 * @throws IOException
	 */
	List<Schedule> getUserSchedulesForRole(String userId, String roleId) throws IOException;
	
	/**
	 * <p>isUserScheduledForRole</p>
	 * 
	 * @param a String
	 * @param a String
	 * @param a Date
	 * @return a boolean
	 * @throws IOException
	 */
	boolean isUserScheduledForRole(String userId, String roleId, Date time) throws IOException;
	
	/**
	 * <p>getRoleScheduleEntries</p>
	 * 
	 * @param a String
	 * @param a Date
	 * @param a Date
	 * @return an OwnedIntervalSequence
	 * @throws IOException
	 */
	OwnedIntervalSequence getRoleScheduleEntries(String roleid, Date start, Date end) throws IOException;
	
	/**
	 * <p>findGroupsForUser</p>
	 * 
	 * @param a String
	 * @return a List<Group>
	 */
	List<Group> findGroupsForUser(String user);
}
