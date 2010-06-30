/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created November 16, 2005
 *
 * Copyright (C) 2005-2006 The OpenNMS Group, Inc.  All rights reserved.
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
