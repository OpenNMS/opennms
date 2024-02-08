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
package org.opennms.systemreport;

import java.io.DataInputStream;
import java.io.File;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemReportResourceLocator implements ResourceLocator {
    private static final Logger LOG = LoggerFactory.getLogger(SystemReportResourceLocator.class);

    private long m_maxProcessWait;

    public SystemReportResourceLocator(final long maxProcessWait) {
        m_maxProcessWait = maxProcessWait;
    }

    @Override
    public String findBinary(final String name) {
        final List<String> pathEntries = new ArrayList<>();
        final String path = System.getenv().get("PATH");
        if (path != null) {
            for (final String p : path.split(File.pathSeparator)) {
                pathEntries.add(p);
            }
            // make sure sbin is in the path, too, just in case
            pathEntries.add("/sbin");
            pathEntries.add("/usr/sbin");
            pathEntries.add("/usr/local/sbin");
        }

        for (final String dir : pathEntries) {
            File file = new File(dir, name);
            if (file.exists()) {
                return file.getPath();
            }
            file = new File(dir, name + ".exe");
            if (file.exists()) {
                return file.getPath();
            }
        }

        return null;
    }

    @Override
    public String slurpOutput(final String commandString, final boolean ignoreExitCode) {
        final CommandLine command = CommandLine.parse(commandString);
        LOG.debug("running: {}", commandString);
        
        final Map<String,String> environment = new HashMap<String,String>(System.getenv());
        environment.put("COLUMNS", "2000");
        DataInputStream input = null;
        PipedInputStream pis = null;
        OutputSuckingParser parser = null;
        String outputText = null;
        final DefaultExecutor executor = new DefaultExecutor();
    
        final PipedOutputStream output = new PipedOutputStream();
        final PumpStreamHandler streamHandler = new PumpStreamHandler(output, output);
        executor.setWatchdog(new ExecuteWatchdog(m_maxProcessWait));
        executor.setStreamHandler(streamHandler);
        if (ignoreExitCode) {
            executor.setExitValues(null);
        }
    
        try {
            LOG.trace("executing '{}'", commandString);
            pis = new PipedInputStream(output);
            input = new DataInputStream(pis);
            parser = new OutputSuckingParser(input);
            parser.start();
            final int exitValue = executor.execute(command, environment);
            IOUtils.closeQuietly(output);
            parser.join(m_maxProcessWait);
            if (!ignoreExitCode && exitValue != 0) {
                LOG.debug("error running '{}': exit value was {}", commandString, exitValue);
            } else {
                outputText = parser.getOutput();
            }
            LOG.trace("finished '{}'", commandString);
        } catch (final Exception e) {
            LOG.debug("Failed to run '{}'", commandString, e);
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(pis);
        }
        
        return outputText;
    }

}
