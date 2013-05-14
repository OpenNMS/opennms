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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ThreadCategory;

/**
 * This is a class to store and execute a console command
 *
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @version $Id: $
 */
public class CommandExecutor implements ExecutorStrategy {
    /**
     * {@inheritDoc}
     *
     * This method executes the command using a Process. The method will decide
     * if an input stream needs to be used.
     */
    @Override
    public int execute(String commandLine, List<Argument> arguments) {
        int returnCode = 0;
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        List<String> commandList = new ArrayList<String>();
        commandList.add(commandLine);

        StringBuffer streamBuffer = new StringBuffer();
        boolean streamed = false;

        // put the non streamed arguments into the argument array
        for (Argument curArg : arguments) {
            // only non streamed arguments go into this list
            if (!curArg.isStreamed()) {
                if (curArg.getSubstitution() != null && !curArg.getSubstitution().trim().equals("")) {
                    commandList.add(curArg.getSubstitution());
                }
                if (curArg.getValue() != null && !curArg.getValue().trim().equals("")) {
                    commandList.add(curArg.getValue());
                }
            } else {
                streamed = true;
                log.debug("streamed argument found");

                if (curArg.getSubstitution() != null && !curArg.getSubstitution().trim().equals("")) {
                    streamBuffer.append(curArg.getSubstitution());
                }
                if (!curArg.getValue().trim().equals("")) {
                    streamBuffer.append(curArg.getValue());
                    if (log.isDebugEnabled()) {
                        log.debug("Streamed argument value: " + curArg.getValue());
                    }
                }
            }
        }

        try {
            // set up the process
            String commandArray[] = new String[commandList.size()];
            commandArray = commandList.toArray(commandArray);
            if (log.isDebugEnabled()) {
                StringBuffer list = new StringBuffer();
                list.append("{ ");
                for (int i = 0; i < commandArray.length; i++) {
                    if (i != 0) {
                        list.append(", ");
                    }
                    list.append(commandArray[i]);
                }
                list.append(" }");
                log.debug(list.toString());
            }

            Process command = Runtime.getRuntime().exec(commandArray);

            // see if we have streamed arguments
            if (streamed) {
                // make sure the output we are writing is buffered
                BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(command.getOutputStream(), "UTF-8"));

                // put the streamed arguments into the stream
                if (log.isDebugEnabled()) {
                    log.debug("Streamed arguments: " + streamBuffer.toString());
                }

                processInput.write(streamBuffer.toString());

                processInput.flush();
                processInput.close();
            }

            // now wait for 30 seconds for the command to complete, if it times
            // out log a message
            long timeout = 30000; // wait for 60 seconds
            long start = System.currentTimeMillis();
            String commandResult = "Command timed out (30 seconds)";
            while ((System.currentTimeMillis() - start) < timeout) {
                try {
                    returnCode = command.exitValue();
                    commandResult = "Command-line binary completed with return code " + returnCode;
                    break;
                } catch (IllegalThreadStateException e) {
                }

                synchronized (this) {
                    wait(1000);
                }
            }

            log.debug(commandResult);
        } catch (IOException e) {
            log.error("Error executing command-line binary: " + commandLine, e);
        } catch (InterruptedException e) {
            log.error("Error executing command-line binary: " + commandLine, e);
        }

        return returnCode;
    }
}
