package org.opennms.systemreport;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.opennms.core.utils.LogUtils;
import org.opennms.systemreport.system.PsParser;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public abstract class AbstractSystemReportPlugin implements SystemReportPlugin {
    protected static final long MAX_PROCESS_WAIT = 10000; // milliseconds
    private MBeanServerConnection m_connection = null;

    public int getPriority() {
        return 99;
    }

    public TreeMap<String, Resource> getEntries() {
        throw new UnsupportedOperationException("You must override getEntries()!");
    }

    public String toString() {
        return String.format("%s[%d]", getName(), getPriority());
    }

    public int compareTo(final SystemReportPlugin o) {
        return new CompareToBuilder()
            .append(this.getPriority(), (o == null? Integer.MIN_VALUE:o.getPriority()))
            .append(this.getName(), (o == null? null:o.getName()))
            .toComparison();
    }

    protected String slurp(final File lsb) {
        if (lsb != null && lsb.exists()) {
            FileInputStream stream = null;
            try {
                stream = new FileInputStream(lsb);
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                return Charset.defaultCharset().decode(bb).toString().replace("[\\r\\n]*$", "");
            } catch (final Exception e) {
                LogUtils.debugf(this, e, "Unable to read from file '%s'", lsb.getPath());
            } finally {
                IOUtils.closeQuietly(stream);
            }
        }
        return null;
    }

    protected String slurpCommand(final String[] command) {
        Process p = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
    
        StringBuffer sb = new StringBuffer();
        try {
            p = Runtime.getRuntime().exec(command);
            is = p.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            while (br.ready()) {
                final String line = br.readLine();
                if (line == null) break;
                sb.append(line);
                if (br.ready()) sb.append("\n");
            }
        } catch (final Throwable e) {
            LogUtils.debugf(this, e, "Failure attempting to run command '%s'", Arrays.asList(command).toString());
        } finally {
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(isr);
            IOUtils.closeQuietly(is);
        }
    
        return sb.toString();
    }

    protected Map<String,String> splitMultilineString(final String regex, final String text) {
        final Map<String,String> map = new HashMap<String,String>();
        
        if (text != null) {
            final StringReader sr = new StringReader(text);
            final BufferedReader br = new BufferedReader(sr);
            try {
                while (br.ready()) {
                    final String line = br.readLine();
                    if (line == null) break;
                    final String[] entry = line.split(regex, 2);
                    if (entry.length == 2) {
                        map.put(entry[0], entry[1]);
                    }
                }
            } catch (final IOException e) {
                LogUtils.debugf(this, e, "an error occurred parsing the text");
            } finally {
                IOUtils.closeQuietly(br);
                IOUtils.closeQuietly(sr);
            }
        }
    
        return map;
    }
    
    protected Resource getResourceFromProperty(final String propertyName) {
        return getResource(System.getProperty(propertyName));
    }

    protected Resource getResource(final String text) {
        if (text == null) return new ByteArrayResource(new byte[0]);
        return new ByteArrayResource(text.getBytes(Charset.defaultCharset()));
    }
    
    protected String findBinary(final String name) {
        List<String> pathEntries = new ArrayList<String>();
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
            if (file.exists() && file.canExecute()) {
                return file.getPath();
            }
            file = new File(dir, name + ".exe");
            if (file.exists() && file.canExecute()) {
                return file.getPath();
            }
        }

        return null;
    }

    protected String slurpOutput(CommandLine command, boolean ignoreExitCode) {
        LogUtils.debugf(this, "running: %s", command.toString());
    
        final Map<String,String> environment = new HashMap<String,String>(System.getenv());
        environment.put("COLUMNS", "2000");
        DataInputStream input = null;
        PipedInputStream pis = null;
        OutputSuckingParser parser = null;
        String topOutput = null;
        final DefaultExecutor executor = new DefaultExecutor();
    
        final PipedOutputStream output = new PipedOutputStream();
        PumpStreamHandler streamHandler = new PumpStreamHandler(output, output);
        executor.setWatchdog(new ExecuteWatchdog(MAX_PROCESS_WAIT));
        executor.setStreamHandler(streamHandler);
    
        try {
            LogUtils.tracef(this, "executing '%s'", command.toString());
            pis = new PipedInputStream(output);
            input = new DataInputStream(pis);
            parser = new OutputSuckingParser(input);
            parser.start();
            int exitValue = executor.execute(command, environment);
            IOUtils.closeQuietly(output);
            parser.join(MAX_PROCESS_WAIT);
            if (!ignoreExitCode && exitValue != 0) {
                LogUtils.debugf(this, "error running '%s': exit value was %d", command.toString(), exitValue);
            } else {
                topOutput = parser.getOutput();
            }
            LogUtils.tracef(this, "finished '%s'", command.toString());
        } catch (final Exception e) {
            LogUtils.debugf(this, e, "Failed to run '%s'", command.toString());
        } finally {
            IOUtils.closeQuietly(output);
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(pis);
        }
        
        return topOutput;
    }

    protected File createTemporaryFileFromString(final String text) {
        File tempFile = null;
        FileWriter fw = null;
        try {
            tempFile = File.createTempFile("topReportPlugin", null);
            tempFile.deleteOnExit();
            fw = new FileWriter(tempFile);
            fw.write(text);
            fw.close();
        } catch (final Exception e) {
            LogUtils.debugf(this, e, "Unable to write to temporary file.");
        } finally {
            IOUtils.closeQuietly(fw);
        }
        return tempFile;
    }

    protected Set<Integer> getOpenNMSProcesses() {
        LogUtils.debugf(this, "getOpenNMSProcesses()");
        final Set<Integer> processes = new HashSet<Integer>();
    
        final String jps = findBinary("jps");
        
        LogUtils.debugf(this, "jps = %s", jps);
    
        DataInputStream input = null;
        PsParser parser = null;
        PipedInputStream pis = null;
        PipedOutputStream output = new PipedOutputStream();
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWatchdog(new ExecuteWatchdog(5000));
    
        if (jps != null) {
            CommandLine command = CommandLine.parse(jps + " -v");
            PumpStreamHandler streamHandler = new PumpStreamHandler(output, System.err);
    
            try {
            LogUtils.tracef(this, "executing '%s'", command.toString());
                pis = new PipedInputStream(output);
                input = new DataInputStream(pis);
                parser = new PsParser(input, "opennms_bootstrap.jar", "status", 0);
                parser.start();
                executor.setStreamHandler(streamHandler);
                int exitValue = executor.execute(command);
                IOUtils.closeQuietly(output);
                parser.join();
                processes.addAll(parser.getProcesses());
                LogUtils.tracef(this, "finished '%s'", command.toString());
                
                if (exitValue != 0) {
                    LogUtils.debugf(this, "error running '%s': exit value was %d", command.toString(), exitValue);
                }
            } catch (final Exception e) {
                LogUtils.debugf(this, e, "Failed to run '%s'", command.toString());
            } finally {
                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(pis);
                IOUtils.closeQuietly(output);
            }
        }
    
        LogUtils.tracef(this, "looking for ps");
        final String ps = findBinary("ps");
        if (ps != null) {
            
            // try Linux/Mac style
            CommandLine command = CommandLine.parse(ps + " aww -o pid -o args");
            output = new PipedOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(output, System.err);
    
            try {
                LogUtils.debugf(this, "executing '%s'", command.toString());
                pis = new PipedInputStream(output);
                input = new DataInputStream(pis);
                parser = new PsParser(input, "opennms_bootstrap.jar", "status", 0);
                parser.start();
                executor.setStreamHandler(streamHandler);
                int exitValue = executor.execute(command);
                IOUtils.closeQuietly(output);
                parser.join(MAX_PROCESS_WAIT);
                processes.addAll(parser.getProcesses());
                LogUtils.tracef(this, "finished '%s'", command.toString());
                
                if (exitValue != 0) {
                    LogUtils.debugf(this, "error running '%s': exit value was %d", command.toString(), exitValue);
                }
            } catch (final Exception e) {
                LogUtils.debugf(this, e, "error running '%s'", command.toString());
            } finally {
                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(pis);
                IOUtils.closeQuietly(output);
            }
    
            if (processes.size() == 0) {
                // try Solaris style
                command = CommandLine.parse(ps + " -ea -o pid -o args");
                output = new PipedOutputStream();
                streamHandler = new PumpStreamHandler(output, System.err);
    
                try {
                    LogUtils.debugf(this, "executing '%s'", command.toString());
                    pis = new PipedInputStream(output);
                    input = new DataInputStream(pis);
                    parser = new PsParser(input, "opennms_bootstrap.jar", "status", 0);
                    parser.start();
                    executor.setStreamHandler(streamHandler);
                    int exitValue = executor.execute(command);
                    IOUtils.closeQuietly(output);
                    parser.join(MAX_PROCESS_WAIT);
                    processes.addAll(parser.getProcesses());
                    LogUtils.tracef(this, "finished '%s'", command.toString());
                    
                    if (exitValue != 0) {
                        LogUtils.debugf(this, "error running '%s': exit value was %d", command.toString(), exitValue);
                    }
                } catch (final Exception e) {
                    LogUtils.debugf(this, e, "error running '%s'", command.toString());
                } finally {
                    IOUtils.closeQuietly(input);
                    IOUtils.closeQuietly(pis);
                    IOUtils.closeQuietly(output);
                }
            }
        }
    
        if (processes.size() == 0) {
            LogUtils.warnf(this, "Unable to find any OpenNMS processes.");
        }
    
        return processes;
    }

    private MBeanServerConnection getConnection() {
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
//            if (processes.size() > 0) {
//                url = getURLForPid(processes.iterator().next());
//            }
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
            return jmxc.getMBeanServerConnection();
        } catch (final Exception e) {
            LogUtils.infof(this, e, "Unable to get JMX connection to OpenNMS.");
        }
        return null;
    }

    /*
    protected JMXServiceURL getURLForPid(Integer pid) throws Exception {
        
        // attach to the target application
        final VirtualMachine vm = VirtualMachine.attach(pid.toString());
        
        // get the connector address
        String connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
        
        // no connector address, so we start the JMX agent
        if (connectorAddress == null) {
            String agent = vm.getSystemProperties().getProperty("java.home") + File.separator + "lib" + File.separator + "management-agent.jar";
            vm.loadAgent(agent);
            
            // agent is started, get the connector address
            connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
            assert connectorAddress != null;
        }
        return new JMXServiceURL(connectorAddress);
    }
    */

    protected void addGetters(final Object o, final Map<String,Resource> map) {
        if (o != null) {
            for (Method method : o.getClass().getDeclaredMethods()) {
                method.setAccessible(true);
                if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
                    Object value;
                    try {
                        value = method.invoke(o);
                    } catch (Exception e) {
                        value = e;
                    }
                    final String key = method.getName().replaceFirst("^get", "").replaceAll("([A-Z])", " $1").replaceFirst("^ ", "");
                    map.put(key, getResource(value.toString()));
                }
            }
        }
    }

    protected <T> List<T> getBeans(final String mxBeanName, final Class<T> clazz) {
        initializeConnection();
        List<T> beans = new ArrayList<T>();
        try {
            ObjectName o = new ObjectName(mxBeanName + ",*");
            for (final ObjectName name : m_connection.queryNames(o, null)) {
                beans.add(getBean(name.getCanonicalName(), clazz));
            }
        } catch (final Exception e) {
            LogUtils.warnf(this, e, "Unable to get beans of type '%s'", mxBeanName);
        }
        return beans;
    }

    protected <T> T getBean(final String mxBeanName, final Class<T> clazz) {
        final List<Class<T>> classes = new ArrayList<Class<T>>();
        classes.add(clazz);
        return getBean(mxBeanName, classes);
    }

    protected <T> T getBean(final String mxBeanName, final List<? extends Class<T>> classes) {
        initializeConnection();
        if (m_connection == null || mxBeanName == null || classes == null || classes.size() == 0) {
            return null;
        }
    
        T bean = null;
        for (final Class<T> c : classes) {
            try {
                bean = ManagementFactory.newPlatformMXBeanProxy(m_connection, mxBeanName, c);
                break;
            } catch (final Exception e) {
                LogUtils.infof(this, e, "Unable to get management bean %s for class %s", mxBeanName, c.getName());
            }
        }
        return bean;
    }

    private void initializeConnection() {
        if (m_connection == null) {
            m_connection = getConnection();
        }
    }
}
