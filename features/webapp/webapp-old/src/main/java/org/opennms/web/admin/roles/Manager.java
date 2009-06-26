//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 24: Fomrat code, Java 5 generics and for loops. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.web.admin.roles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.GroupManager;
import org.opennms.netmgt.config.UserManager;
import org.opennms.netmgt.config.common.Time;
import org.opennms.netmgt.config.groups.Group;
import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.users.User;

public class Manager implements WebRoleManager, WebUserManager, WebGroupManager {
    
    private GroupManager m_groupManager;
    private UserManager m_userManager;
    
    private class InvalidUser extends WebUser {

        public InvalidUser(String name) {
            super(name);
        }
        
        public String toString() {
            return "Invalid User ["+getName()+"]";
        }
        
    }
    
    private class InvalidGroup extends WebGroup {

        public InvalidGroup(String name) {
            super(name);
            super.setUsers(Collections.EMPTY_LIST);
        }
        
        public String toString() {
            return "Invalid Group ["+getName()+"]";
        }
        
    }
    
    
    private User getBackingUser(String name) {
        try {
            return m_userManager.getUser(name);
        } catch (MarshalException e) {
            throw new WebRolesException("Error marshalling users.xml config file", e);
        } catch (ValidationException e) {
            throw new WebRolesException("Error validating users.xml config file", e);
        } catch (IOException e) {
            throw new WebRolesException("Error reading users.xml config file", e);
        }
    }
    
    private Collection<User> getBackingUsers() {
        try {
            return m_userManager.getUsers().values();
        } catch (MarshalException e) {
            throw new WebRolesException("Error marshalling users.xml config file", e);
        } catch (ValidationException e) {
            throw new WebRolesException("Error validating users.xml config file", e);
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
        } catch (MarshalException e) {
            throw new WebRolesException("Error marshalling groups.xml config file", e);
        } catch (ValidationException e) {
            throw new WebRolesException("Error validating groups.xml config file", e);
        } catch (IOException e) {
            throw new WebRolesException("Error reading groups.xml config file", e);
        }
    }
    
    private Collection<Group> getBackingGroups() {
        try {
            return m_groupManager.getGroups().values();
        } catch (MarshalException e) {
            throw new WebRolesException("Error marshalling groups.xml config file", e);
        } catch (ValidationException e) {
            throw new WebRolesException("Error validating groups.xml config file", e);
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
            
        } catch (MarshalException e) {
            throw new WebRolesException("Error marshalling users.xml config file", e);
        } catch (ValidationException e) {
            throw new WebRolesException("Error validating users.xml config file", e);
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
        
        ManagedRole mgdRole = new ManagedRole();
        mgdRole.setName(webRole.getName());
        mgdRole.setDescription(webRole.getDescription());
        mgdRole.setDefaultUser(webRole.getDefaultUser());
        mgdRole.setMembershipGroup(webRole.getMembershipGroup());
        
        return mgdRole;
    }
    
    public WebRole createRole() {
        return new ManagedRole();
    }
    
    class ManagedRole extends WebRole {
        private static final int DESCR=0;
        private static final int USER=1;
        private static final int GROUP=2;
        private static final int NAME=3;
        BitSet m_flags = new BitSet();
        Role m_role;
        ManagedRole(String roleName) {
            this(getBackingRole(roleName));
        }
        ManagedRole(Role role) {
           super(role.getName());
           m_role = role;
           super.setDescription(role.getDescription());
           super.setDefaultUser(getWebUser(role.getSupervisor()));
           super.setMembershipGroup(getWebGroup(role.getMembershipGroup()));
        }
        
        ManagedRole() {
            super();
            m_role = null;
        }
        
        public void setDescription(String description) {
            super.setDescription(description);
            m_flags.set(DESCR);
        }
        
        public void setDefaultUser(WebUser defaultUser) {
            super.setDefaultUser(defaultUser);
            m_flags.set(USER);
        }
        public void setMembershipGroup(WebGroup memberShipGroup) {
            super.setMembershipGroup(memberShipGroup);
            m_flags.set(GROUP);
        }
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
            } catch (Exception e) {
                throw new WebRolesException("Unable to save role "+getName()+". "+e.getMessage(), e);
            }
            
        }
        public Collection<WebUser> getCurrentUsers() {
            if (m_role == null) {
                return new ArrayList<WebUser>(0);
            }
            return getUsersScheduleForRole(this, new Date());
        }
        public WebCalendar getCalendar(Date month) {
            return new MonthlyCalendar(month, m_role, m_groupManager);
        }
        public Schedule getSchedule(int schedIndex) {
            return m_role.getSchedule(schedIndex);
        }
        public Time getTime(int schedIndex, int timeIndex) {
            return getSchedule(schedIndex).getTime(timeIndex);
        }
        public void addEntry(String user, Date startDate, Date endDate) {
            // TODO Auto-generated method stub
            
        }
        
        
    }
    
    class ManagedUser extends WebUser {
        User m_user;
        ManagedUser(String userId) {
            this(getBackingUser(userId));
        }
        ManagedUser(User user) {
            super(user.getUserId());
            m_user = user;
        }
    }
    
    class ManagedGroup extends WebGroup {
        Group m_group;
        ManagedGroup(String groupName) {
            this(getBackingGroup(groupName));
        }
        ManagedGroup(Group group) {
            super(group.getName());
            
            List<WebUser> users = new ArrayList<WebUser>();
            for (String userId : getUsers(group)) {
                users.add(getWebUser(userId));
            }
            super.setUsers(users);
        }
        
        @SuppressWarnings("unchecked")
        private List<String> getUsers(Group group) {
            return group.getUserCollection();
        }
        
    }

    public Manager(GroupManager groupManager, UserManager userManager) {
        m_groupManager = groupManager;
        m_userManager = userManager;
    }

    public Collection<WebRole> getRoles() {
        Collection<Role> roles = m_groupManager.getRoles();
        List<WebRole> webRoles = new ArrayList<WebRole>(roles.size());
        for (Role role : roles) {
            webRoles.add(getWebRole(role));
        }
        return webRoles;
    }

    public void deleteRole(String roleName) {
        try {
            m_groupManager.deleteRole(roleName);
        } catch (Exception e) {
            throw new WebRolesException("Error deleting role "+roleName+". "+e.getMessage(), e);
        }
    }

    public WebRole getRole(String roleName) {
        Role role = getBackingRole(roleName);
        return (role == null ? null : getWebRole(role));
    }

    public void saveRole(WebRole webRole) {
        try {
            ManagedRole mgdRole = getManagedRole(webRole);
            mgdRole.save();
        } catch (Exception e) {
            throw new WebRolesException("Error saving roles. "+e.getMessage(), e);
        }
    }

    public Collection<WebUser> getUsers() {
        List<WebUser> users = new ArrayList<WebUser>();
        for (User u : getBackingUsers()) {
            users.add(getWebUser(u));
        }
        return users;
    }

    public WebUser getUser(String name) {
        return getWebUser(name);
    }

    public Collection<WebGroup> getGroups() {
        List<WebGroup> groups = new ArrayList<WebGroup>();
        for (Group group : getBackingGroups()) {
            groups.add(getWebGroup(group));
        }
        return groups;
    }

    public WebGroup getGroup(String groupName) {
        return getWebGroup(groupName);
    }

}
