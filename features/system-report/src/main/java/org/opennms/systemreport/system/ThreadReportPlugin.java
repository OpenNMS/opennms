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
import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.AbstractSystemReportPlugin;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class ThreadReportPlugin extends AbstractSystemReportPlugin {
    public String getName() {
        return "Threads";
    }

    public String getDescription() {
        return "Java Thread Dump";
    }

    public int getPriority() {
        return 4;
    }

    public TreeMap<String, Resource> getEntries() {
        final TreeMap<String,Resource> map = new TreeMap<String,Resource>();

        LogUtils.tracef(this, "starting thread dump");
        triggerThreadDump();
        LogUtils.tracef(this, "thread dump finished");

        final String outputLog = System.getProperty("opennms.home") + File.separator + "logs" + File.separator + "daemon" + File.separator + "output.log";
        LogUtils.debugf(this, "reading file " + outputLog);
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
                        LogUtils.debugf(this, "found full thread dump");
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
                    LogUtils.debugf(this, "No thread dump was found.");
                } else {
                    fw.append(threadDump);
                    map.put("ThreadDump.txt", new FileSystemResource(threadDumpFile));
                }
            } catch (final Exception e) {
                LogUtils.debugf(this, e, "Unable to read from '%s'", outputLog);
            } finally {
                IOUtils.closeQuietly(fw);
                IOUtils.closeQuietly(bfr);
                IOUtils.closeQuietly(fr);
            }
        } else {
            LogUtils.warnf(this, "could not find output.log in '%s'", outputLog);
        }

        return map;
    }

    private void triggerThreadDump() {
        String kill = findBinary("kill");
        
        if (kill != null) {
            for (final Integer pid : getOpenNMSProcesses()) {
                LogUtils.debugf(this, "pid = " + pid);
                CommandLine command = CommandLine.parse(kill + " -3 " + pid.toString());
                try {
                    LogUtils.tracef(this, "running '%s'", command.toString());
                    DefaultExecutor executor = new DefaultExecutor();
                    executor.setWatchdog(new ExecuteWatchdog(5000));
                    int exitValue = executor.execute(command);
                    LogUtils.tracef(this, "finished '%s'", command.toString());
                    if (exitValue != 0) {
                        LogUtils.warnf(this, "'%s' exited non-zero: %d", command.toString(), exitValue);
                    }
                } catch (final Exception e) {
                    LogUtils.warnf(this, e, "Unable to run kill -3 on '%s': you might need to run system-report as root.", pid.toString());
                }
            }
        }
    }
}
