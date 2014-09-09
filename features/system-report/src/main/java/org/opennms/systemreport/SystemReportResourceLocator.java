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
import org.opennms.core.utils.LogUtils;

public class SystemReportResourceLocator implements ResourceLocator {
    private long m_maxProcessWait;

    public SystemReportResourceLocator(final long maxProcessWait) {
        m_maxProcessWait = maxProcessWait;
    }

    @Override
    public String findBinary(final String name) {
        final List<String> pathEntries = new ArrayList<String>();
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
        LogUtils.debugf(this, "running: %s", commandString);
        
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
    
        try {
            LogUtils.tracef(this, "executing '%s'", commandString);
            pis = new PipedInputStream(output);
            input = new DataInputStream(pis);
            parser = new OutputSuckingParser(input);
            parser.start();
            final int exitValue = executor.execute(command, environment);
            IOUtils.closeQuietly(output);
            parser.join(m_maxProcessWait);
            if (!ignoreExitCode && exitValue != 0) {
                LogUtils.debugf(this, "error running '%s': exit value was %d", commandString, exitValue);
            } else {
                outputText = parser.getOutput();
            }
            LogUtils.tracef(this, "finished '%s'", commandString);
        } catch (final Exception e) {
            LogUtils.debugf(this, e, "Failed to run '%s'", commandString);
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(pis);
        }
        
        return outputText;
    }

}
