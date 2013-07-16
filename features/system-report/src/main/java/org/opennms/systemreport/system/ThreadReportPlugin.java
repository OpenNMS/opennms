/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.systemreport.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.TreeMap;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class ThreadReportPlugin extends AbstractSystemReportPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadReportPlugin.class);
    @Override
    public String getName() {
        return "Threads";
    }

    @Override
    public String getDescription() {
        return "Java thread dump (full output only)";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();

        LOG.trace("starting thread dump");
        triggerThreadDump();
        LOG.trace("thread dump finished");

        final String outputLog = System.getProperty("opennms.home") + File.separator + "logs" + File.separator + "daemon" + File.separator + "output.log";
        LOG.debug("reading file {}", outputLog);
        final File outputLogFile = new File(outputLog);
        FileReader fr = null;
        BufferedReader bfr = null;
        FileWriter fw = null;
        File threadDumpFile = null;
        String threadDump = null;
        if (outputLogFile.exists()) {
            try {
                threadDumpFile = File.createTempFile("ThreadReportPlugin", null);
                threadDumpFile.deleteOnExit();
                fr = new FileReader(outputLogFile);
                bfr = new BufferedReader(fr);
                fw = new FileWriter(threadDumpFile);
                String line;
                StringBuffer sb = null;
                boolean endOnCarriageReturn = false;
                while ((line = bfr.readLine()) != null) {
                    if (line.startsWith("Full thread dump")) {
                        LOG.debug("found full thread dump");
                        sb = new StringBuffer();
                        sb.append(line).append("\n");
                    } else if (sb != null) {
                        if (endOnCarriageReturn && line.length() == 0) {
                            endOnCarriageReturn = false;
                            threadDump = sb.toString();
                            sb = null;
                        } else if (line.startsWith("Heap")) {
                            endOnCarriageReturn = true;
                            sb.append(line).append("\n");
                        } else {
                            sb.append(line).append("\n");
                        }
                    }
                }
                if (threadDump == null) {
                    LOG.debug("No thread dump was found.");
                } else {
                    fw.append(threadDump);
                    map.put("ThreadDump.txt", new FileSystemResource(threadDumpFile));
                }
            } catch (final Exception e) {
                LOG.debug("Unable to read from '{}'", outputLog, e);
            } finally {
                IOUtils.closeQuietly(fw);
                IOUtils.closeQuietly(bfr);
                IOUtils.closeQuietly(fr);
            }
        } else {
            LOG.warn("could not find output.log in '{}'", outputLog);
        }

        return map;
    }

    private void triggerThreadDump() {
        String kill = findBinary("kill");
        
        if (kill != null) {
            for (final Integer pid : getOpenNMSProcesses()) {
                LOG.debug("pid = {}", pid);
                CommandLine command = CommandLine.parse(kill + " -3 " + pid.toString());
                try {
                    LOG.trace("running '{}'", command);
                    DefaultExecutor executor = new DefaultExecutor();
                    executor.setWatchdog(new ExecuteWatchdog(5000));
                    int exitValue = executor.execute(command);
                    LOG.trace("finished '{}'", command);
                    if (exitValue != 0) {
                        LOG.warn("'{}' exited non-zero: {}", command, exitValue);
                    }
                } catch (final Exception e) {
                    LOG.warn("Unable to run kill -3 on '{}': you might need to run system-report as root.", pid, e);
                }
            }
        }
    }
}
