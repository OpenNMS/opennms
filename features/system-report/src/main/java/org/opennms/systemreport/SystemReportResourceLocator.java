/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
