/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;
import org.opennms.netmgt.config.users.User;

/**
 * <p>Manager class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class WebRoleManagerImpl implements WebRoleManager, WebUserManager, WebGroupManager {
    
    private GroupManager m_groupManager;
    private UserManager m_userManager;
    
    private static class InvalidUser extends WebUser {

        public InvalidUser(String name) {
            super(name);
        }
        
        @Override
        public String toString() {
            return "Invalid User ["+getName()+"]";
        }
        
    }
    
    private static class InvalidGroup extends WebGroup {

        public InvalidGroup(String name) {
            super(name);
            super.setUsers(new ArrayList<WebUser>());
        }
        
        @Override
        public String toString() {
            return "Invalid Group ["+getName()+"]";
        }
        
    }
    
    
    private User getBackingUser(String name) {
        try {
            return m_userManager.getUser(name);
        } catch (IOException e) {
            throw new WebRolesException("Error reading users.xml config file", e);
        }
    }
    
    private Collection<User> getBackingUsers() {
        try {
            return m_userManager.getUsers().values();
        } catch (IOException e) {
            throw new WebRolesException("Error reading users.xml config file", e);
        }
    }
    
    private Role getBackingRole(String roleName) {
        return m_groupManager.getRole(roleName);
    }
    
    private Group getBackingGroup(String groupName) {
        try {
            return m_groupManager.getGroup(groupName);
        } catch (IOException e) {
            throw new WebRolesException("Error reading groups.xml config file", e);
        }
    }
    
    private Collection<Group> getBackingGroups() {
        try {
            return m_groupManager.getGroups().values();
        } catch (IOException e) {
            throw new WebRolesException("Error reading groups.xml config file", e);
        }
    }
    
    private Collection<WebUser> getUsersScheduleForRole(WebRole role, Date time) {
        try {
            
            String[] users = m_userManager.getUsersScheduledForRole(role.getName(), new Date());
            List<WebUser> webUsers = new ArrayList<WebUser>(users.length);
            for (String user : users) {
                webUsers.add(getWebUser(user));
            }
            return webUsers;
        } catch (IOException e) {
            throw new WebRolesException("Error reading users.xml config file", e);
        }

    }
    
    private WebRole getWebRole(Role role) {
        return new ManagedRole(role);
    }

    private WebUser getWebUser(String userName) {
        User u = getBackingUser(userName);
        if (u == null) {
            return new InvalidUser(userName);
        } else {
            return new ManagedUser(u);
        }
    }
    
    private WebUser getWebUser(User user) {
        if (user == null) {
            return new InvalidUser("Select A Valid User...");
        } else {
            return new ManagedUser(user);
        }
    }
    
    private WebGroup getWebGroup(String groupName) {
        Group g = getBackingGroup(groupName);
        if (g == null) {
            return new InvalidGroup(groupName);
        } else {
            return new ManagedGroup(g);
        }
    }
    
    private WebGroup getWebGroup(Group group) {
        if (group == null) {
            return new InvalidGroup("Select a valid group...");
        } else {
            return new ManagedGroup(group);
        }
    }
    
    private ManagedRole getManagedRole(WebRole webRole) {
        if (webRole instanceof ManagedRole) {
            return (ManagedRole) webRole;
        }
        
        ManagedRole mgdRole = new ManagedRole(webRole);
        
        return mgdRole;
    }
    
    /**
     * <p>createRole</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebRole} object.
     */
    @Override
    public WebRole createRole() {
        return new ManagedRole();
    }
    
    private class ManagedRole extends WebRole {
        private static final int DESCR=0;
        private static final int USER=1;
        private static final int GROUP=2;
        private static final int NAME=3;
        private final BitSet m_flags = new BitSet();
        private Role m_role;

        ManagedRole() {
        }

        ManagedRole(Role role) {
           super(role.getName());
           m_role = role;
           super.setDescription(role.getDescription().orElse(null));
           super.setDefaultUser(getWebUser(role.getSupervisor()));
           super.setMembershipGroup(getWebGroup(role.getMembershipGroup()));
        }
        
        ManagedRole(WebRole webRole) {
            super(webRole.getName());
            super.setDescription(webRole.getDescription());
            super.setDefaultUser(webRole.getDefaultUser());
            super.setMembershipGroup(webRole.getMembershipGroup());
         }
         
        @Override
        public void setDescription(String description) {
            super.setDescription(description);
            m_flags.set(DESCR);
        }
        
        @Override
        public void setDefaultUser(WebUser defaultUser) {
            super.setDefaultUser(defaultUser);
            m_flags.set(USER);
        }
        @Override
        public void setMembershipGroup(WebGroup memberShipGroup) {
            super.setMembershipGroup(memberShipGroup);
            m_flags.set(GROUP);
        }
        @Override
        public void setName(String name) {
            super.setName(name);
            m_flags.set(NAME);
        }
        
        public void save() {
            try {
                Role role = (m_role == null ? new Role() : m_role);
                if (m_flags.get(DESCR)) {
                    role.setDescription(super.getDescription());
                }
                if (m_flags.get(USER)) {
                    role.setSupervisor(super.getDefaultUser().getName());
                }
                if (m_flags.get(GROUP)) {
                    role.setMembershipGroup(super.getMembershipGroup().getName());
                }
                if (m_flags.get(NAME)) {
                    role.setName(super.getName());
                }
                
                Collection<WebSchedEntry> newEntries = getNewEntries();
                for (WebSchedEntry entry : newEntries) {
                    entry.update(role);
                }
                
                if (m_role != null) {
                    m_groupManager.saveGroups();
                } else {
                    m_groupManager.saveRole(role);
                    m_role = role;
                }
            } catch (Throwable e) {
                throw new WebRolesException("Unable to save role "+getName()+". "+e.getMessage(), e);
            }
            
        }
        @Override
        public Collection<WebUser> getCurrentUsers() {
            if (m_role == null) {
                return new ArrayList<WebUser>(0);
            }
            return getUsersScheduleForRole(this, new Date());
        }
        @Override
        public WebCalendar getCalendar(Date month) {
            return new MonthlyCalendar(month, m_role, m_groupManager);
        }
        @Override
        public Schedule getSchedule(int schedIndex) {
            final int index = schedIndex;
            return m_role.getSchedules().get(index);
        }
        @Override
        public Time getTime(int schedIndex, int timeIndex) {
            final int index = timeIndex;
            return getSchedule(schedIndex).getTimes().get(index);
        }
    }
    
    private class ManagedUser extends WebUser {
        ManagedUser(User user) {
            super(user.getUserId());
        }
    }
    
    private class ManagedGroup extends WebGroup {
        ManagedGroup(Group group) {
            super(group.getName());
            
            List<WebUser> users = new ArrayList<WebUser>();
            for (String userId : getUsers(group)) {
                users.add(getWebUser(userId));
            }
            super.setUsers(users);
        }
        
        private List<String> getUsers(Group group) {
            return group.getUsers();
        }
    }

    /**
     * <p>Constructor for Manager.</p>
     *
     * @param groupManager a {@link org.opennms.netmgt.config.GroupManager} object.
     * @param userManager a {@link org.opennms.netmgt.config.UserManager} object.
     */
    public WebRoleManagerImpl(GroupManager groupManager, UserManager userManager) {
        m_groupManager = groupManager;
        m_userManager = userManager;
    }

    /**
     * <p>getRoles</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<WebRole> getRoles() {
        Collection<Role> roles = m_groupManager.getRoles();
        List<WebRole> webRoles = new ArrayList<WebRole>(roles.size());
        for (Role role : roles) {
            webRoles.add(getWebRole(role));
        }
        return webRoles;
    }

    /** {@inheritDoc} */
    @Override
    public void deleteRole(String roleName) {
        try {
            m_groupManager.deleteRole(roleName);
        } catch (Throwable e) {
            throw new WebRolesException("Error deleting role "+roleName+". "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public WebRole getRole(String roleName) {
        Role role = getBackingRole(roleName);
        return (role == null ? null : getWebRole(role));
    }

    /** {@inheritDoc} */
    @Override
    public void saveRole(WebRole webRole) {
        try {
            ManagedRole mgdRole = getManagedRole(webRole);
            mgdRole.save();
        } catch (Throwable e) {
            throw new WebRolesException("Error saving roles. "+e.getMessage(), e);
        }
    }

    /**
     * <p>getUsers</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<WebUser> getUsers() {
        List<WebUser> users = new ArrayList<WebUser>();
        for (User u : getBackingUsers()) {
            users.add(getWebUser(u));
        }
        return users;
    }

    /** {@inheritDoc} */
    @Override
    public WebUser getUser(String name) {
        return getWebUser(name);
    }

    /**
     * <p>getGroups</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<WebGroup> getGroups() {
        List<WebGroup> groups = new ArrayList<WebGroup>();
        for (Group group : getBackingGroups()) {
            groups.add(getWebGroup(group));
        }
        return groups;
    }

    /** {@inheritDoc} */
    @Override
    public WebGroup getGroup(String groupName) {
        return getWebGroup(groupName);
    }

}
