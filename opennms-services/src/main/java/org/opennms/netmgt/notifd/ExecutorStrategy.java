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
package org.opennms.netmgt.notifd;

import java.util.List;

import org.opennms.netmgt.model.notifd.Argument;

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
