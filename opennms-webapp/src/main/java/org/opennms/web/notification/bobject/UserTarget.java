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

package org.opennms.web.notification.bobject;

import org.opennms.web.admin.users.parsers.User;

/**
 * A NotificationTarget representing a user target parsed from the notifications
 * xml file.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version 1.1.1.1
 * @since 1.6.12
 */
public class UserTarget extends NotificationTarget {
    /**
     * The userId of the target
     */
    private String m_userName;

    /**
     * The User object associated with this target
     */
    private User m_user;

    /**
     * The command name to use to contact this user
     */
    private String m_commandName;

    /**
     * The Command object to use to contact this user
     */
    private Command m_command;

    /**
     * Default Constructor
     */
    public UserTarget() {
    }

    /**
     * Sets the userId for this target
     *
     * @param name
     *            the username to be set.
     */
    public void setUserName(String name) {
        m_userName = name;
    }

    /**
     * Returns the userId for this target
     *
     * @return the username.
     */
    public String getUserName() {
        return m_userName;
    }

    /**
     * Sets the User object for this target
     *
     * @param user
     *            the user object to be set.
     */
    public void setUser(User user) {
        m_user = user;
    }

    /**
     * Returns the User object for this target
     *
     * @return the user object.
     */
    public User getUser() {
        return m_user;
    }

    /**
     * Sets the command name for this target
     *
     * @param commandName
     *            the command name to be set.
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
     * Sets the Command object for this target
     *
     * @param command
     *            the command to be set.
     */
    public void setCommand(Command command) {
        m_command = command;
    }

    /**
     * Returns the Command object for this target
     *
     * @return the command.
     */
    public Command getCommand() {
        return m_command;
    }

    /**
     * Returns the type of the target
     *
     * @return the type, compare to NotificationTask.TARGET_TYPE_USER.
     */
    public int getType() {
        return TARGET_TYPE_USER;
    }
}
