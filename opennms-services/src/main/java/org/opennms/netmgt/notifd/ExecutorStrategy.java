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

package org.opennms.netmgt.notifd;

import java.util.List;

import org.opennms.core.utils.Argument;

/**
 * <p>ExecutorStrategy interface.</p>
 *
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 *
 * Implement this interface to as a means of executing code be it a Java class
 * or system command.
 *
 * This interface was created to provide a Java Notification plugin interface.
 * The original CommandExecutor class was developed to execute system commands
 * calling its execute method with the command name and a list of command line
 * paramenters. Now the ClassExecutor class implements this interface and the
 * execute method instantiates the Java class specified in the commandLine
 * parameter to the execute method.
 *
 * Now, this interface is implemented by the ClassExecutor class that allows the
 * notification process (NotificationTask) to simply call the execute method and
 * not be concerned if the strategy (binary flag) actually executes a command or
 * instantiates a Java class.
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 *
 * Implement this interface to as a means of executing code be it a Java class
 * or system command.
 *
 * This interface was created to provide a Java Notification plugin interface.
 * The original CommandExecutor class was developed to execute system commands
 * calling its execute method with the command name and a list of command line
 * paramenters. Now the ClassExecutor class implements this interface and the
 * execute method instantiates the Java class specified in the commandLine
 * parameter to the execute method.
 *
 * Now, this interface is implemented by the ClassExecutor class that allows the
 * notification process (NotificationTask) to simply call the execute method and
 * not be concerned if the strategy (binary flag) actually executes a command or
 * instantiates a Java class.
 * @version $Id: $
 */
public interface ExecutorStrategy {
    /**
     * Implement this method to support execute style commands such as a system
     * command or to instantiate a Java class.
     *
     * @param commandLine
     *            the command/class to execute/instantiate
     * @param arguments
     *            a list of Argument objects that need to be passed to the
     *            command line call or the class execute method
     * @return int, the return code of the command/execute method
     */
    public abstract int execute(String commandLine, List<Argument> arguments);
}
