/*
 * Created on Sep 8, 2004
 * Copyright (c) 2005, The OpenNMS Group, Inc..
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.core.utils;

import java.util.List;

/**
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
    public abstract int execute(String commandLine, List arguments);
}
