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
// Modifications:
//
// 2007 Jul 24: Java 5 generics. - dj@opennms.org
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

package org.opennms.web.notification.bobject;

import java.util.ArrayList;
import java.util.List;

/**
 * A NotificationTarget representing a user target parsed from the
 * notifications.xml.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 * @since 1.6.12
 */
public class GroupTarget extends NotificationTarget {
    /**
     * The userId of the target
     */
    private String m_groupName;

    /**
     * The User object associated with this target
     */
    private List<UserTarget> m_userTargets;

    /**
     * The command name to use to contact this user
     */
    private String m_commandName;

    /**
     * Default Constructor
     */
    public GroupTarget() {
        m_userTargets = new ArrayList<UserTarget>();
    }

    /**
     * Sets the userId for this target
     *
     * @param name
     *            the group name.
     */
    public void setGroupName(String name) {
        m_groupName = name;
    }

    /**
     * Returns the userId for this target
     *
     * @return the group name.
     */
    public String getGroupName() {
        return m_groupName;
    }

    /**
     * Sets the command name for this target
     *
     * @param commandName
     *            the command name.
     */
    public void setCommandName(String commandName) {
        m_commandName = commandName;
    }

    /**
     * Returns the command name for this target
     *
     * @return the command name.
     */
    public String getCommandName() {
        return m_commandName;
    }

    /**
     * Adds a user target to this group target
     *
     * @param target
     *            a user target to be added.
     */
    public void addUserTarget(UserTarget target) {
        m_userTargets.add(target);
    }

    /**
     * Returns the list of UserTargets in this group target
     *
     * @return the list of user targets.
     */
    public List<UserTarget> getUserTargets() {
        return m_userTargets;
    }

    /**
     * Returns the type of the target
     *
     * @return the target type, compare to NotificationTask.TARGET_TYPE_USER.
     */
    public int getType() {
        return TARGET_TYPE_GROUP;
    }
}
