/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.OwnedInterval;
import org.opennms.core.utils.OwnedIntervalSequence;
import org.opennms.core.utils.Owner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.groups.Groups;
import org.opennms.netmgt.config.groups.Header;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Roles;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.users.DutySchedule;
import org.opennms.netmgt.model.OnmsGroup;
import org.opennms.netmgt.model.OnmsGroupList;


/**
 * <p>Abstract GroupManager class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @author <a href="mailto:ayres@net.orst.edu">Bill Ayres</a>
 * @author <a href="mailto:dj@gregor.com">DJ Gregor</a>
 */
public abstract class GroupManager {
    private static final Logger LOG = LoggerFactory.getLogger(GroupManager.class);

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * The duty schedules for each group
     */
    protected static HashMap<String, List<DutySchedule>> m_dutySchedules;

    /**
     * A mapping of Group object by name
     */
    private Map<String, Group> m_groups;
    private Map<String, Role> m_roles;
    private Header m_oldHeader;

    /**
     * <p>parseXml</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    protected synchronized void parseXml(InputStream stream) throws MarshalException, ValidationException {
        Groupinfo groupinfo = CastorUtils.unmarshal(Groupinfo.class, stream);
        initializeGroupsAndRoles(groupinfo);
    }

    private void initializeGroupsAndRoles(Groupinfo groupinfo) {
        Groups groups = groupinfo.getGroups();
        m_groups = new LinkedHashMap<String, Group>();
        m_oldHeader = groupinfo.getHeader();
        for (Group curGroup : groups.getGroupCollection()) {
            m_groups.put(curGroup.getName(), curGroup);
        }
        buildDutySchedules(m_groups);
        
        Roles roles = groupinfo.getRoles();
        m_roles = new LinkedHashMap<String, Role>();
        if (roles != null) {
        	for (Role role : roles.getRoleCollection()) {
                m_roles.put(role.getName(), role);
            }
        }
    }

    /**
     * Set the groups data
     *
     * @param grp a {@link java.util.Map} object.
     */
    public void setGroups(Map<String, Group> grp) {
        m_groups = grp;
    }

    /**
     * Get the groups
     *
     * @return a {@link java.util.Map} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public Map<String, Group> getGroups() throws IOException, MarshalException, ValidationException {
    
        update();
    
        return Collections.unmodifiableMap(m_groups);
    
    }

    public OnmsGroupList getOnmsGroupList() throws MarshalException, ValidationException, IOException {
        final OnmsGroupList list = new OnmsGroupList();
        
        for (final String name : getGroupNames()) {
            list.add(getOnmsGroup(name));
        }
        list.setTotalCount(list.getCount());

        return list;
    }

    public OnmsGroup getOnmsGroup(final String groupName) throws MarshalException, ValidationException, IOException {
        final Group castorGroup = getGroup(groupName);
        if (castorGroup == null) return null;
        
        final OnmsGroup group = new OnmsGroup(groupName);
        group.setComments(castorGroup.getComments());
        group.setUsers(castorGroup.getUserCollection());
        
        return group;
    }

    public synchronized void save(final OnmsGroup group) throws Exception {
        Group castorGroup = getGroup(group.getName());
        if (castorGroup == null) {
            castorGroup = new Group();
            castorGroup.setName(group.getName());
        }
        castorGroup.setComments(group.getComments());
        castorGroup.setUser(group.getUsers().toArray(EMPTY_STRING_ARRAY));
        
        saveGroup(group.getName(), castorGroup);
    }

    /**
     * <p>update</p>
     *
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     */
    public abstract void update() throws IOException, MarshalException, ValidationException;

    /**
     * Returns a boolean indicating if the group name appears in the xml file
     *
     * @return true if the group exists in the xml file, false otherwise
     * @param groupName a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public boolean hasGroup(String groupName) throws IOException, MarshalException, ValidationException {
        update();
    
        return m_groups.containsKey(groupName);
    }

    /**
     * <p>getGroupNames</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public List<String> getGroupNames() throws IOException, MarshalException, ValidationException {
        update();
    
        return new ArrayList<String>(m_groups.keySet());
    }

    /**
     * Get a group using its name
     *
     * @param name
     *            the name of the group to return
     * @return Group, the group specified by name
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public Group getGroup(String name) throws IOException, MarshalException, ValidationException {
        update();
    
        return m_groups.get(name);
    }

    /**
     * <p>saveGroups</p>
     *
     * @throws java.lang.Exception if any.
     */
    public synchronized void saveGroups() throws Exception {
        Header header = m_oldHeader;

        if (header != null) header.setCreated(EventConstants.formatToString(new Date()));
    
        Groups groups = new Groups();
        for (Group grp : m_groups.values()) {
            groups.addGroup(grp);
        }
        
        
        Roles roles = new Roles();
        for (Role role : m_roles.values()) {
            roles.addRole(role);
        }
    
        Groupinfo groupinfo = new Groupinfo();
        groupinfo.setGroups(groups);
        if (roles.getRoleCount() > 0)
            groupinfo.setRoles(roles);
        groupinfo.setHeader(header);
    
        m_oldHeader = header;
    
        // marshal to a string first, then write the string to the file. This
        // way the original configuration
        // isn't lost if the XML from the marshal is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(groupinfo, stringWriter);
        String data = stringWriter.toString();
        saveXml(data);
    }

    /**
     * Builds a mapping between groups and duty schedules. These are used when
     * determining to send a notice to a given group. This helps speed up the decision process.
     * @param groups the map of groups parsed from the XML configuration file
     */
    private static void buildDutySchedules(Map<String, Group> groups) {
        m_dutySchedules = new HashMap<String, List<DutySchedule>>();
        Iterator<String> i = groups.keySet().iterator();
        while(i.hasNext()) {
            String key = i.next();
            Group curGroup = groups.get(key);
            if (curGroup.getDutyScheduleCount() > 0) {
                List<DutySchedule> dutyList = new ArrayList<DutySchedule>();
                for (String duty : curGroup.getDutyScheduleCollection()) {
                	dutyList.add(new DutySchedule(duty));
                }
                m_dutySchedules.put(key, dutyList);
            }
        }
    }

    /**
     * Determines if a group is on duty at a given time. If a group has no duty schedules
     * listed in the configuration file, that group is assumed to always be on duty.
     *
     * @param group the group whose duty schedule we want
     * @param time the time to check for a duty schedule
     * @return boolean, true if the group is on duty, false otherwise.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public boolean isGroupOnDuty(String group, Calendar time) throws IOException, MarshalException, ValidationException {
        update();
        //if the group has no duty schedules then it is on duty
        if (!m_dutySchedules.containsKey(group)) {
            return true;
        }
        List<DutySchedule> dutySchedules = m_dutySchedules.get(group);
        for (DutySchedule curSchedule : dutySchedules) {
        	if (curSchedule.isInSchedule(time)) {
        		return true;
        	}
        }
        return false;
    }
  
    /**
     * Determines when a group is next on duty. If a group has no duty schedules
     * listed in the configuration file, that group is assumed to always be on duty.
     *
     * @param group the group whose duty schedule we want
     * @param time the time to check for a duty schedule
     * @return long, the time in milliseconds until the group is next on duty
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public long groupNextOnDuty(String group, Calendar time) throws IOException, MarshalException, ValidationException {
        long next = -1;
        update();
        //if the group has no duty schedules then it is on duty
        if (!m_dutySchedules.containsKey(group)) {
            return 0;
        }
        List<DutySchedule> dutySchedules = m_dutySchedules.get(group);
        for (int i = 0; i < dutySchedules.size(); i++) {
            DutySchedule curSchedule = dutySchedules.get(i);
            long tempnext =  curSchedule.nextInSchedule(time);
            if( tempnext < next || next == -1 ) {
                LOG.debug("isGroupOnDuty: On duty in {} millisec from schedule {}", i, tempnext);
                next = tempnext;
            }
        }
        return next;
    } 

    /**
     * <p>saveXml</p>
     *
     * @param data a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void saveXml(String data) throws IOException;

    /**
     * Adds a new user and overwrites the "groups.xml"
     *
     * @param name a {@link java.lang.String} object.
     * @param details a {@link org.opennms.netmgt.config.groups.Group} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void saveGroup(String name, Group details) throws Exception {
        if (name == null || details == null) {
            throw new Exception("GroupFactory:saveGroup  null");
        } else {
            m_groups.put(name, details);
        }
    
        saveGroups();
    }
    
    /**
     * <p>saveRole</p>
     *
     * @param role a {@link org.opennms.netmgt.config.groups.Role} object.
     * @throws java.lang.Exception if any.
     */
    public void saveRole(Role role) throws Exception {
        m_roles.put(role.getName(), role);
        saveGroups();
    }


    /**
     * Removes the user from the list of groups. Then overwrites to the
     * "groups.xml"
     *
     * @param name a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void deleteUser(String name) throws Exception {
        // Check if the user exists
        if (name != null && !name.equals("")) {
            // Remove the user in the group.
        	for (Group group : m_groups.values()) {
        		group.removeUser(name);
        	}

        	for (Role role : m_roles.values()) {
                Iterator<Schedule> s = role.getScheduleCollection().iterator();
                while(s.hasNext()) {
                    Schedule sched = s.next();
                    if (name.equals(sched.getName())) {
                        s.remove();
                    }
                }
            }
        } else {
            throw new Exception("GroupFactory:delete Invalid user name:" + name);
        }
        
        
        // Saves into "groups.xml" file
        saveGroups();
    }

    /**
     * Removes the group from the list of groups. Then overwrites to the
     * "groups.xml"
     *
     * @param name a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void deleteGroup(String name) throws Exception {
        // Check if the group exists
        if (name != null && !name.equals("")) {
            if (m_groups.containsKey(name)) {
                // Remove the group.
                m_groups.remove(name);
            } else
                throw new Exception("GroupFactory:delete Group doesnt exist:" + name);
        } else {
            throw new Exception("GroupFactory:delete Invalid user group:" + name);
        }
        // Saves into "groups.xml" file
        saveGroups();
    }
    
    /**
     * <p>deleteRole</p>
     *
     * @param name a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public void deleteRole(String name) throws Exception {
        if (name != null && !name.equals("")) {
            if (m_roles.containsKey(name)) {
                m_roles.remove(name);
            }
            else 
                throw new Exception("GroupFacotry:deleteRole Role doesn't exist: "+name);
        }
        else
            throw new Exception("GroupFactory:deleteRole Invalid role name: "+name);
        
        saveGroups();
    }


    /**
     * Renames the group from the list of groups. Then overwrites to the
     * "groups.xml"
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newName a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void renameGroup(String oldName, String newName) throws Exception {
    	if (oldName != null && !oldName.equals("")) {
    		if (m_groups.containsKey(oldName)) {
    			Group grp = m_groups.remove(oldName);
    			grp.setName(newName);
    			m_groups.put(newName, grp);
    		} else {
    			throw new Exception("GroupFactory.renameGroup: Group doesn't exist: " + oldName);
    		}
    		// Save into groups.xml
    		saveGroups();
    	}
    }

    /**
     * When this method is called group name is changed, so also is the
     * group name belonging to the view. Also overwrites the "groups.xml" file
     *
     * @param oldName a {@link java.lang.String} object.
     * @param newName a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public synchronized void renameUser(String oldName, String newName) throws Exception {
        // Get the old data
        if (oldName == null || newName == null || oldName == "" || newName == "") {
            throw new Exception("Group Factory: Rename user.. no value ");
        } else {
            Map<String, Group> map = new LinkedHashMap<String, Group>();
            
        	for (Group group : m_groups.values()) {        	   
        	   for(ListIterator<String> userList = group.getUserCollection().listIterator(); userList.hasNext();){
        	       String name = userList.next();
        	       
        	       if(name.equals(oldName)){
        	          userList.set(newName); 
        	       }
        	   }
        	   map.put(group.getName(), group);
        	}
        	
        	m_groups.clear();
        	m_groups.putAll(map);

            for (Role role : m_roles.values()) {
            	for (Schedule sched : role.getScheduleCollection()) {
                    if (oldName.equals(sched.getName())) {
                        sched.setName(newName);
                    }
                }
            }
            
            saveGroups();
        }
    }

    /**
     * <p>getRoleNames</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getRoleNames() {
        return (String[]) m_roles.keySet().toArray(new String[m_roles.keySet().size()]);
    }
    
    /**
     * <p>getRoles</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<Role> getRoles() {
        return m_roles.values();
    }
    
    /**
     * <p>getRole</p>
     *
     * @param roleName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.groups.Role} object.
     */
    public Role getRole(String roleName) {
        return (Role)m_roles.get(roleName);
    }

    /**
     * <p>userHasRole</p>
     *
     * @param userId a {@link java.lang.String} object.
     * @param roleid a {@link java.lang.String} object.
     * @return a boolean.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public boolean userHasRole(String userId, String roleid) throws MarshalException, ValidationException, IOException {
        update();

        for (Schedule sched : getRole(roleid).getScheduleCollection()) {
            if (userId.equals(sched.getName())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * <p>getSchedulesForRoleAt</p>
     *
     * @param roleId a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @return a {@link java.util.List} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public List<Schedule> getSchedulesForRoleAt(String roleId, Date time) throws MarshalException, ValidationException, IOException {
        update();

        List<Schedule> schedules = new ArrayList<Schedule>();
        for (Schedule sched : getRole(roleId).getScheduleCollection()) {
            if (BasicScheduleUtils.isTimeInSchedule(time, BasicScheduleUtils.getGroupSchedule(sched))) {
                schedules.add(sched);
            }
        }
        return schedules;
    }
    
    /**
     * <p>getUserSchedulesForRole</p>
     *
     * @param userId a {@link java.lang.String} object.
     * @param roleId a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public List<Schedule> getUserSchedulesForRole(String userId, String roleId) throws MarshalException, ValidationException, IOException {
        update();

        List<Schedule> scheds = new ArrayList<Schedule>();
        for (Schedule sched : getRole(roleId).getScheduleCollection()) {
            if (userId.equals(sched.getName())) {
                scheds.add(sched);
            }
        }
        return scheds;
        
    }

    /**
     * <p>isUserScheduledForRole</p>
     *
     * @param userId a {@link java.lang.String} object.
     * @param roleId a {@link java.lang.String} object.
     * @param time a {@link java.util.Date} object.
     * @return a boolean.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public boolean isUserScheduledForRole(String userId, String roleId, Date time) throws MarshalException, ValidationException, IOException {
        update();

        for (Schedule sched : getUserSchedulesForRole(userId, roleId)) {
            if (BasicScheduleUtils.isTimeInSchedule(time, BasicScheduleUtils.getGroupSchedule(sched))) {
                return true;
            }
        }
        
        // if no user is scheduled then the supervisor is schedule by default 
        Role role = getRole(roleId);
        if (userId.equals(role.getSupervisor())) {
        	for (Schedule sched : role.getScheduleCollection()) {
                if (BasicScheduleUtils.isTimeInSchedule(time, BasicScheduleUtils.getGroupSchedule(sched))) {
                    // we found another scheduled user
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * <p>getRoleScheduleEntries</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     * @return a {@link org.opennms.core.utils.OwnedIntervalSequence} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public OwnedIntervalSequence getRoleScheduleEntries(String roleid, Date start, Date end) throws MarshalException, ValidationException, IOException {
        update();

        OwnedIntervalSequence schedEntries = new OwnedIntervalSequence();
                 Role role = getRole(roleid);
                 for (int i = 0; i < role.getScheduleCount(); i++) {
                    Schedule sched = (Schedule) role.getSchedule(i);
                     Owner owner = new Owner(roleid, sched.getName(), i);
                     schedEntries.addAll(BasicScheduleUtils.getIntervalsCovering(start, end, BasicScheduleUtils.getGroupSchedule(sched), owner));
                 }
                 
                 OwnedIntervalSequence defaultEntries = new OwnedIntervalSequence(new OwnedInterval(start, end));
                 defaultEntries.removeAll(schedEntries);
                 Owner supervisor = new Owner(roleid, role.getSupervisor());
                 for (Iterator<OwnedInterval> it = defaultEntries.iterator(); it.hasNext();) {
                     OwnedInterval interval = it.next();
                     interval.addOwner(supervisor);
                 }
                 schedEntries.addAll(defaultEntries);
                 return schedEntries;
        
    }

    /**
     * <p>findGroupsForUser</p>
     *
     * @param user a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<Group> findGroupsForUser(String user) {
        List<Group> groups = new ArrayList<Group>();
        
        for (Group group : m_groups.values()) {
            if (group.getUserCollection().contains(user)) {
                groups.add(group);
            }
        }

        return groups;
    }

}
