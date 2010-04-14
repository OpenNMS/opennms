//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc..  All rights reserved.
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

package org.opennms.netmgt.notifd;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ThreadCategory;

/**
 * This is a class to store and execute a console command
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public class CommandExecutor implements ExecutorStrategy {
    /**
     * This method executes the command using a Process. The method will decide
     * if an input stream needs to be used.
     * 
     * @param commandLine
     *            the command to execute as a command line call
     * @param arguments
     *            a list of Argument objects that need to be passed to the
     *            command line call
     * @return int, the return code of the command
     */
    public int execute(String commandLine, List<Argument> arguments) {
        int returnCode = 0;
        Category log = ThreadCategory.getInstance(getClass());

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
            commandArray = (String[]) commandList.toArray(commandArray);
            if (log.isDebugEnabled()) {
                log.debug(commandList);
            }

            Process command = Runtime.getRuntime().exec(commandArray);

            // see if we have streamed arguments
            if (streamed) {
                // make sure the output we are writting is buffered
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
                    commandResult = "Command completed with return code " + returnCode;
                    break;
                } catch (IllegalThreadStateException e) {
                }

                synchronized (this) {
                    wait(1000);
                }
            }

            log.debug(commandResult);
        } catch (IOException e) {
            log.error("Error executing command " + commandLine, e);
        } catch (InterruptedException e) {
            log.error("Error executing command " + commandLine, e);
        }

        return returnCode;
    }
}
