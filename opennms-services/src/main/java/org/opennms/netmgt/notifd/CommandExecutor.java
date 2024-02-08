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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.notifd.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(CommandExecutor.class);
    /**
     * {@inheritDoc}
     *
     * This method executes the command using a Process. The method will decide
     * if an input stream needs to be used.
     */
    @Override
    public int execute(String commandLine, List<Argument> arguments) {
        int returnCode = 0;

        List<String> commandList = new ArrayList<>();
        commandList.add(commandLine);

        final StringBuilder streamBuffer = new StringBuilder();
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
                LOG.debug("streamed argument found");

                if (curArg.getSubstitution() != null && !curArg.getSubstitution().trim().equals("")) {
                    streamBuffer.append(curArg.getSubstitution());
                }
                if (!curArg.getValue().trim().equals("")) {
                    streamBuffer.append(curArg.getValue());
                    LOG.debug("Streamed argument value: {}", curArg.getValue());
                }
            }
        }

        try {
            // set up the process
            String[] commandArray = new String[commandList.size()];
            commandArray = commandList.toArray(commandArray);
            if (LOG.isDebugEnabled()) {
                StringBuffer list = new StringBuffer();
                list.append("{ ");
                for (int i = 0; i < commandArray.length; i++) {
                    if (i != 0) {
                        list.append(", ");
                    }
                    list.append(commandArray[i]);
                }
                list.append(" }");
                LOG.debug(list.toString());
            }

            Process command = Runtime.getRuntime().exec(commandArray);

            // see if we have streamed arguments
            if (streamed) {
                // make sure the output we are writing is buffered
                BufferedWriter processInput = new BufferedWriter(new OutputStreamWriter(command.getOutputStream(), StandardCharsets.UTF_8));

                // put the streamed arguments into the stream
                LOG.debug("Streamed arguments: {}", streamBuffer);

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

            LOG.debug(commandResult);
        } catch (IOException e) {
            LOG.error("Error executing command-line binary: {}", commandLine, e);
        } catch (InterruptedException e) {
            LOG.error("Error executing command-line binary: {}", commandLine, e);
        }

        return returnCode;
    }
}
