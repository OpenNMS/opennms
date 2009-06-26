//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.admin.groups.parsers;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a data class to store the group information from the groups.xml file
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @version 1.1.1.1
 * 
 */
public class Group implements Cloneable {
    /**
     */
    public static final String GROUP_NAME_PROPERTY = "groupName";

    /**
     * The name of the group
     */
    private String m_groupName;

    /**
     * The comments for the group
     */
    private String m_groupComments;

    /**
     * The group info for the group
     */
     private GroupInfo m_groupInfo;

    /**
     * The list of users in the group
     */
    private List<String> m_users;

    /**
     */
    private PropertyChangeSupport m_propChange;

    /**
     * Default constructor, intializes the users list
     */
    public Group() {
        m_propChange = new PropertyChangeSupport(this);

        m_groupName = "";
        m_groupComments = "";
        m_users = new ArrayList<String>();
        m_groupInfo = new GroupInfo();
    }

    /**
     */
    public Group clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }

        Group newGroup = new Group();

        newGroup.setGroupName(m_groupName);
        newGroup.setGroupComments(m_groupComments);

        for (int i = 0; i < m_users.size(); i++) {
            newGroup.addUser(m_users.get(i));
        }

        return newGroup;
    }

    /**
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        m_propChange.addPropertyChangeListener(listener);
    }

    /**
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        m_propChange.removePropertyChangeListener(listener);
    }

    /**
     * Returns the group information for this group
     * 
     * @return the group info
     */
    public GroupInfo getGroupInfo() {
        return m_groupInfo;
    }

    /**
     * Sets the group information for this group
     * 
     * @param someInfo the group info
     */
    public void setGroupInfo(GroupInfo someInfo) {
        m_groupInfo = someInfo;
    }

    /**
     * Sets the group name
     * 
     * @param aName
     *            the name of the group
     */
    public void setGroupName(String aName) {
        String old = m_groupName;
        m_groupName = aName;
        m_propChange.firePropertyChange(GROUP_NAME_PROPERTY, old, m_groupName);
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
     * Returns whether the group has this user in its users list
     * 
     * @return true if user is in list, false if not
     */
    public boolean hasUser(String aUser) {
        return m_users.contains(aUser);
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
     * Removes all users from the group.
     */
    public void clearUsers() {
        m_users.clear();
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
     * Returns a String representation of the group, used primarily for
     * debugging.
     * 
     * @return a string representation
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("name     = " + m_groupName + "\n");
        buffer.append("comments = " + m_groupComments + "\n");
        buffer.append("users:\n");

        for (String user : m_users) {
            buffer.append("\t" + user + "\n");
        }

        return buffer.toString();
    }
}
