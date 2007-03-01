package org.opennms.netmgt.config;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;

public interface GroupDao {
    /**
     * Set the groups data
     */
    public void setGroups(Map<String, Group> groups);

    /**
     * Get the groups
     */
    public Map<String, Group> getGroups();
    
    /**
     * Returns a boolean indicating if the group name appears in the xml file
     * 
     * @return true if the group exists in the xml file, false otherwise
     */
    public boolean hasGroup(String groupName);

    /**
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
     */
    public void saveGroups();
    
    /**
     * Determines if a group is on duty at a given time. If a group has no duty schedules
     * listed in the config file, that group is assumed to always be on duty.
     * @param group the group whose duty schedule we want
     * @param time the time to check for a duty schedule
     * @return boolean, true if the group is on duty, false otherwise.
     */
    public boolean isGroupOnDuty(String group, Calendar time);
    
    /**
     * Determines when a group is next on duty. If a group has no duty schedules
     * listed in the config file, that group is assumed to always be on duty.
     * @param group the group whose duty schedule we want
     * @param time the time to check for a duty schedule
     * @return long, the time in millisec until the group is next on duty
     */
    public long groupNextOnDuty(String group, Calendar time);
    
    /**
     * Adds a new user and overwrites the "groups.xml"
     */
    public void saveGroup(String name, Group details);
    
    public void saveRole(Role role);
    
    /**
     * Removes the user from the list of groups. Then overwrites to the
     * "groups.xml"
     */
    public void deleteUser(String name);
    
    /**
     * Removes the group from the list of groups. Then overwrites to the
     * "groups.xml"
     */
    public void deleteGroup(String name);
    
    public void deleteRole(String name);
    
    /**
     * Renames the group from the list of groups. Then overwrites to the
     * "groups.xml"
     */
    public void renameGroup(String oldName, String newName);
    
    /**
     * When this method is called group name is changed, so also is the
     * groupname belonging to the view. Also overwrites the "groups.xml" file
     */
    public void renameUser(String oldName, String newName);
    
    public String[] getRoleNames();
    
    public Collection getRoles();
    
    public Role getRole(String roleName);

    public boolean userHasRole(String userId, String roleid);
       
    public List<Schedule> getSchedulesForRoleAt(String roleId, Date time);
    
    public List getUserSchedulesForRole(String userId, String roleid);
    
    public boolean isUserScheduledForRole(String userId, String roleid, Date time);
    
    public OwnedIntervalSequence getRoleScheduleEntries(String roleid, Date start, Date end);
    
    public List<Group> findGroupsForUser(String user);
}
