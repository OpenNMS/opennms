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
import org.opennms.netmgt.dao.CastorObjectRetrievalFailureException;
import org.opennms.netmgt.dao.castor.CastorExceptionTranslator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

/**
 * <p>GroupManagerGroupDao class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class GroupManagerGroupDao implements GroupDao, InitializingBean {
    private static final GroupManagerCastorExceptionTranslator CASTOR_EXCEPTION_TRANSLATOR = new GroupManagerCastorExceptionTranslator();
    
    private GroupManager m_groupManager;
    
    /**
     * <p>Constructor for GroupManagerGroupDao.</p>
     */
    public GroupManagerGroupDao() {
    }

    /** {@inheritDoc} */
    public void deleteGroup(String name) {
        try {
            m_groupManager.deleteGroup(name);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("deleting group '" + name + "'", e);
        }
    }

    /** {@inheritDoc} */
    public void deleteRole(String name) {
        try {
            m_groupManager.deleteRole(name);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("deleting role '" + name + "'", e);
        }
    }

    /** {@inheritDoc} */
    public void deleteUser(String name) {
        try {
            m_groupManager.deleteUser(name);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("deleting user '" + name + "'", e);
        }
    }

    /** {@inheritDoc} */
    public List<Group> findGroupsForUser(String user) {
        return m_groupManager.findGroupsForUser(user);
    }

    /** {@inheritDoc} */
    public Group getGroup(String name) {
        try {
            return m_groupManager.getGroup(name);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("getting group '" + name + "'", e);
        }
    }

    /**
     * <p>getGroupNames</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getGroupNames() {
        try {
            return m_groupManager.getGroupNames();
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("getting group names", e);
        }
    }

    /**
     * <p>getGroups</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, Group> getGroups() {
        try {
            return m_groupManager.getGroups();
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("getting groups", e);
        }
    }

    /** {@inheritDoc} */
    public Role getRole(String name) {
        try {
            return m_groupManager.getRole(name);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("getting role '" + name + "'", e);
        }
    }

    /**
     * <p>getRoleNames</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getRoleNames() {
        return m_groupManager.getRoleNames();
    }

    /** {@inheritDoc} */
    public OwnedIntervalSequence getRoleScheduleEntries(String role, Date start, Date end) {
        try {
            return m_groupManager.getRoleScheduleEntries(role, start, end);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("getting scheduled entries for role '" + role + "' between " + start + " and " + end, e);
        }
    }

    /**
     * <p>getRoles</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection getRoles() {
        try {
            return m_groupManager.getRoles();
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("getting roles", e);
        }
    }

    /** {@inheritDoc} */
    public List<Schedule> getSchedulesForRoleAt(String role, Date time) {
        try {
            return m_groupManager.getSchedulesForRoleAt(role, time);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("getting schedules for role '" + role + "' at " + time, e);
        }
    }

    /** {@inheritDoc} */
    public List getUserSchedulesForRole(String user, String role) {
        try {
            return m_groupManager.getUserSchedulesForRole(user, role);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("getting user schedules for user '" + user + "' for role '" + role + "'", e);
        }
    }

    /** {@inheritDoc} */
    public long groupNextOnDuty(String group, Calendar time) {
        try {
            return m_groupManager.groupNextOnDuty(group, time);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("getting next on duty time for group '" + group + "' after " + time, e);
        }
    }

    /** {@inheritDoc} */
    public boolean hasGroup(String name) {
        try {
            return m_groupManager.hasGroup(name);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("getting group '" + name + "'", e);
        }
    }

    /** {@inheritDoc} */
    public boolean isGroupOnDuty(String group, Calendar time) {
        try {
            return m_groupManager.isGroupOnDuty(group, time);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("getting group '" + group + "' to see if it is on duty at " + time, e);
        }
    }

    /** {@inheritDoc} */
    public boolean isUserScheduledForRole(String user, String role, Date time) {
        try {
            return m_groupManager.isUserScheduledForRole(user, role, time);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("checking to see if user '" + user + "' is schedule for role '" + role + "' at " + time, e);
        }
    }

    /** {@inheritDoc} */
    public void renameGroup(String oldName, String newName) {
        try {
            m_groupManager.renameGroup(oldName, newName);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("renaming group from '" + oldName + "' to '" + newName + "'", e);
        }
    }

    /** {@inheritDoc} */
    public void renameUser(String oldName, String newName) {
        try {
            m_groupManager.renameUser(oldName, newName);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("renaming user from '" + oldName + "' to '" + newName + "'", e);
        }
    }

    /** {@inheritDoc} */
    public void saveGroup(String name, Group details) {
        try {
            m_groupManager.saveGroup(name, details);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("saving group '" + name + "' with details " + details, e);
        }
    }

    /**
     * <p>saveGroups</p>
     */
    public void saveGroups() {
        try {
            m_groupManager.saveGroups();
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("saving groups", e);
        }
    }

    /** {@inheritDoc} */
    public void saveRole(Role name) {
        try {
            m_groupManager.saveRole(name);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("saving role '" + name + "'", e);
        }
    }

    /** {@inheritDoc} */
    public void setGroups(Map<String, Group> groups) {
        m_groupManager.setGroups(groups);
    }

    /** {@inheritDoc} */
    public boolean userHasRole(String user, String role) {
        try {
            return m_groupManager.userHasRole(user, role);
        } catch (Exception e) {
            throw CASTOR_EXCEPTION_TRANSLATOR.translate("checking to see if user '" + user + "' has role '" + role + "'", e);
        }
    }
    
    /**
     * <p>afterPropertiesSet</p>
     */
    public void afterPropertiesSet() {
        Assert.state(m_groupManager != null, "groupManager property must be set and be non-null");
    }

    /**
     * <p>getGroupManager</p>
     *
     * @return a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    public GroupManager getGroupManager() {
        return m_groupManager;
    }

    /**
     * <p>setGroupManager</p>
     *
     * @param groupManager a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    public void setGroupManager(GroupManager groupManager) {
        m_groupManager = groupManager;
    }
    
    public static class GroupManagerCastorExceptionTranslator extends CastorExceptionTranslator {
        public DataAccessException translate(String task, Exception e) {
            return new CastorObjectRetrievalFailureException("General error while " + task + ": " + e, e);
        }
    }

}
