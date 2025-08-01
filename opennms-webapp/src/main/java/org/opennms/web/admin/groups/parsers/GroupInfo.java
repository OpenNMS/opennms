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
        m_users = new ArrayList<>();
        m_dutySchedules = new Vector<>();
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
        final StringBuilder buffer = new StringBuilder();

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
