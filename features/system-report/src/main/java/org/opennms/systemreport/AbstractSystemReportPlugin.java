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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.systemreport.system.PsParser;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public abstract class AbstractSystemReportPlugin implements SystemReportPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSystemReportPlugin.class);
    protected static final long MAX_PROCESS_WAIT = 10000; // milliseconds
    private MBeanServerConnection m_connection = null;

    @Override
    public int getPriority() {
        return 99;
    }

    @Override
    public TreeMap<String, Resource> getEntries() {
        throw new UnsupportedOperationException("You must override getEntries()!");
    }

    @Override
    public String toString() {
        return String.format("%s[%d]", getName(), getPriority());
    }

    @Override
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
                LOG.debug("Unable to read from file '{}'", lsb.getPath(), e);
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
            LOG.debug("Failure attempting to run command '{}'", Arrays.asList(command), e);
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
                LOG.debug("an error occurred parsing the text", e);
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
        return new ByteArrayResource(text.getBytes());
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

    protected String slurpOutput(CommandLine command, boolean ignoreExitCode) {
        LOG.debug("running: {}", command);
    
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
            LOG.trace("executing '{}'", command);
            pis = new PipedInputStream(output);
            input = new DataInputStream(pis);
            parser = new OutputSuckingParser(input);
            parser.start();
            int exitValue = executor.execute(command, environment);
            IOUtils.closeQuietly(output);
            parser.join(MAX_PROCESS_WAIT);
            if (!ignoreExitCode && exitValue != 0) {
                LOG.debug("error running '{}': exit value was {}", command, exitValue);
            } else {
                topOutput = parser.getOutput();
            }
            LOG.trace("finished '{}'", command);
        } catch (final Exception e) {
            LOG.debug("Failed to run '{}'", command, e);
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
            LOG.debug("Unable to write to temporary file.", e);
        } finally {
            IOUtils.closeQuietly(fw);
        }
        return tempFile;
    }

    protected Set<Integer> getOpenNMSProcesses() {
        LOG.debug("getOpenNMSProcesses()");
        final Set<Integer> processes = new HashSet<Integer>();
    
        final String jps = findBinary("jps");
        
        LOG.debug("jps = {}", jps);
    
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
            LOG.trace("executing '{}'", command);
                pis = new PipedInputStream(output);
                input = new DataInputStream(pis);
                parser = new PsParser(input, "opennms_bootstrap.jar", "status", 0);
                parser.start();
                executor.setStreamHandler(streamHandler);
                int exitValue = executor.execute(command);
                IOUtils.closeQuietly(output);
                parser.join();
                processes.addAll(parser.getProcesses());
                LOG.trace("finished '{}'", command);
                
                if (exitValue != 0) {
                    LOG.debug("error running '{}': exit value was {}", command, exitValue);
                }
            } catch (final Exception e) {
                LOG.debug("Failed to run '{}'", command, e);
            } finally {
                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(pis);
                IOUtils.closeQuietly(output);
            }
        }
    
        LOG.trace("looking for ps");
        final String ps = findBinary("ps");
        if (ps != null) {
            
            // try Linux/Mac style
            CommandLine command = CommandLine.parse(ps + " aww -o pid -o args");
            output = new PipedOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(output, System.err);
    
            try {
                LOG.debug("executing '{}'", command);
                pis = new PipedInputStream(output);
                input = new DataInputStream(pis);
                parser = new PsParser(input, "opennms_bootstrap.jar", "status", 0);
                parser.start();
                executor.setStreamHandler(streamHandler);
                int exitValue = executor.execute(command);
                IOUtils.closeQuietly(output);
                parser.join(MAX_PROCESS_WAIT);
                processes.addAll(parser.getProcesses());
                LOG.trace("finished '{}'", command);
                
                if (exitValue != 0) {
                    LOG.debug("error running '{}': exit value was {}", command, exitValue);
                }
            } catch (final Exception e) {
                LOG.debug("error running '{}'", command, e);
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
                    LOG.debug("executing '{}'", command);
                    pis = new PipedInputStream(output);
                    input = new DataInputStream(pis);
                    parser = new PsParser(input, "opennms_bootstrap.jar", "status", 0);
                    parser.start();
                    executor.setStreamHandler(streamHandler);
                    int exitValue = executor.execute(command);
                    IOUtils.closeQuietly(output);
                    parser.join(MAX_PROCESS_WAIT);
                    processes.addAll(parser.getProcesses());
                    LOG.trace("finished '{}'", command);
                    
                    if (exitValue != 0) {
                        LOG.debug("error running '{}': exit value was {}", command, exitValue);
                    }
                } catch (final Exception e) {
                    LOG.debug("error running '{}'", command, e);
                } finally {
                    IOUtils.closeQuietly(input);
                    IOUtils.closeQuietly(pis);
                    IOUtils.closeQuietly(output);
                }
            }
        }
    
        if (processes.size() == 0) {
            LOG.warn("Unable to find any OpenNMS processes.");
        }
    
        return processes;
    }

    private MBeanServerConnection getConnection() {
        final List<Integer> ports = new ArrayList<Integer>();
        Integer p = Integer.getInteger("com.sun.management.jmxremote.port");
        if (p != null) ports.add(p);
        ports.add(18980);
        ports.add(1099);
        for (final Integer port : ports) {
            LOG.trace("Trying JMX at localhost:{}/jmxrmi", port);
            try {
                JMXServiceURL url = new JMXServiceURL(String.format("service:jmx:rmi:///jndi/rmi://localhost:%d/jmxrmi", port));
                JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
                return jmxc.getMBeanServerConnection();
            } catch (final Exception e) {
                LOG.debug("Unable to get JMX connection to OpenNMS on port {}.", port, e);
            }
        }
        return null;
    }

    protected void addGetters(final Object o, final Map<String,Resource> map) {
        if (o != null) {
            for (Method method : o.getClass().getDeclaredMethods()) {
                method.setAccessible(true);
                if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
                    Object value;
                    try {
                        value = method.invoke(o);
                    } catch (Throwable e) {
                        value = e;
                    }
                    final String key = method.getName().replaceFirst("^get", "").replaceAll("([A-Z])", " $1").replaceFirst("^ ", "").replaceAll("\\bVm\\b", "VM");
                    map.put(key, getResource(value.toString()));
                }
            }
        }
    }

    protected <T> List<T> getBeans(final String mxBeanName, final Class<T> clazz) {
        initializeConnection();
        List<T> beans = new ArrayList<T>();
        if (m_connection == null)  return beans;
        try {
            ObjectName o = new ObjectName(mxBeanName + ",*");
            for (final ObjectName name : (Set<ObjectName>)m_connection.queryNames(o, null)) {
                beans.add(getBean(name.getCanonicalName(), clazz));
            }
        } catch (final Exception e) {
            LOG.warn("Unable to get beans of type '{}'", mxBeanName, e);
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
                LOG.info("Unable to get management bean {} for class {}", mxBeanName, c.getName(), e);
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
