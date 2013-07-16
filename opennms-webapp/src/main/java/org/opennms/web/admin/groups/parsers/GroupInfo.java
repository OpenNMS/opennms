/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.groups.parsers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import org.opennms.netmgt.config.users.DutySchedule;

/**
 * This is a data class to store the group information from the groups.xml file
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @deprecated Use the Group class instead
 */
public class GroupInfo {
    /**
     * The name of the group
     */
    private String m_groupName;

    /**
     * The comments for the group
     */
    private String m_groupComments;

    /**
     * The list of users in the group
     */
    private List<String> m_users;

    /**
     * The list of duty schedules in the group
     */
    private List<DutySchedule> m_dutySchedules;

    /**
     * Default constructor, intializes the users list
     */
    public GroupInfo() {
        m_groupName = "";
        m_groupComments = "";
        m_users = new ArrayList<String>();
        m_dutySchedules = new Vector<DutySchedule>();
    }

    /**
     * Sets the group name
     *
     * @param aName
     *            the name of the group
     */
    public void setGroupName(String aName) {
        m_groupName = aName;
    }

    /**
     * Returns the group name
     *
     * @return the name of the group
     */
    public String getGroupName() {
        return m_groupName;
    }

    /**
     * Sets the comments for the group
     *
     * @param someComments
     *            the comments for the group
     */
    public void setGroupComments(String someComments) {
        m_groupComments = someComments;
    }

    /**
     * Returns the comments for the group
     *
     * @return the comments for the group
     */
    public String getGroupComments() {
        return m_groupComments;
    }

    /**
     * Adds a username to the list of users
     *
     * @param aUser
     *            a new username
     */
    public void addUser(String aUser) {
        m_users.add(aUser);
    }

    /**
     * Removes a username from the list of users
     *
     * @param aUser
     *            the user to remove
     */
    public void removeUser(String aUser) {
        m_users.remove(aUser);
    }

    /**
     * Returns the list of users
     *
     * @return the list of users
     */
    public List<String> getUsers() {
        return m_users;
    }

    /**
     * Returns a count of the users in the list
     *
     * @return how many users in this group
     */
    public int getUserCount() {
        return m_users.size();
    }

    /**
     * This method adds a duty schedule
     *
     * @param aSchedule
     *            a new duty schedule to associate with a group
     */
    public void addGroupDutySchedule(DutySchedule aSchedule) {
        m_dutySchedules.add(aSchedule);
    }

    /**
     * This method sets a full list of duty schedules for a group
     *
     * @param someSchedules
     *            a list of DutySchedule objects for a group
     */
    public void setDutySchedule(List<DutySchedule> someSchedules) {
        m_dutySchedules = someSchedules;
    }

    /**
     * Returns the number of DutySchedule object for a group
     *
     * @return the number of DutySchedules
     */
    public int getDutyScheduleCount() {
        return m_dutySchedules.size();
    }

    /**
     * Returns the full list of DutySchedules
     *
     * @return the full list of DutySchedules
     */
    public List<DutySchedule> getDutySchedules() {
        return m_dutySchedules;
    }

    /**
     * Returns a boolean indicating if the user is on duty at the specified
     * time.
     *
     * @param aTime
     *            a time to see if the user is on duty
     * @return true if the user is on duty, false otherwise
     */
    public boolean isOnDuty(Calendar aTime) {
        boolean result = false;

        // if there is no schedule assume that the user is on duty
        if (m_dutySchedules.size() == 0) {
            return true;
        }

        for (DutySchedule curSchedule : m_dutySchedules) {
            result = curSchedule.isInSchedule(aTime);

            // don't continue if the time is in this schedule
            if (result) {
                break;
            }
        }

        return result;
    }

    /**
     * Returns a String representation of the group, used primarily for
     * debugging.
     *
     * @return a string representation
     */
    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("name     = " + m_groupName + "\n");
        buffer.append("comments = " + m_groupComments + "\n");
        buffer.append("users:\n");

        for (String user : m_users) {
            buffer.append("\t" + user + "\n");
        }
 
        for (DutySchedule dutySchedule : m_dutySchedules) {
            buffer.append(dutySchedule.toString() + "\n");
        }

        return buffer.toString();
    }
}
