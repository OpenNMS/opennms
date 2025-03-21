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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InaccessibleObjectException;
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
import org.opennms.core.sysprops.SystemProperties;
import org.opennms.systemreport.system.PsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public abstract class AbstractSystemReportPlugin implements SystemReportPlugin {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSystemReportPlugin.class);
    protected static final long MAX_PROCESS_WAIT = 10000; // milliseconds
    private MBeanServerConnection m_connection = null;

    protected ResourceLocator m_resourceLocator = new SystemReportResourceLocator(MAX_PROCESS_WAIT);

    @Override
    public int getPriority() {
        return 99;
    }

    @Override
    public boolean getFullOutputOnly() {
        return false;
    }

    @Override
    public boolean getOutputsFiles() {
        return false;
    }

    @Override
    public boolean isVisible() { return false; }

    @Override
    public String defaultFormat() { return ".txt";}

    protected ResourceLocator getResourceLocator() {
        return m_resourceLocator;
    }

    protected void setResourceLocator(final ResourceLocator resourceLocator) {
        m_resourceLocator = resourceLocator;
    }

    @Override
    public Map<String, Resource> getEntries() {
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
    
        final StringBuilder sb = new StringBuilder();
        try {
            p = Runtime.getRuntime().exec(command);
            is = p.getInputStream();
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);
            String line=null;
            while ((line = br.readLine()) != null) {
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

    protected Set<Integer> getOpenNMSProcesses() {
        LOG.trace("getOpenNMSProcesses()");
        final Set<Integer> processes = new HashSet<Integer>();
    
        final String jps = getResourceLocator().findBinary("jps");
        
        LOG.trace("jps = {}", jps);
    
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
        final String ps = getResourceLocator().findBinary("ps");
        if (ps != null) {
            
            // try Linux/Mac style
            CommandLine command = CommandLine.parse(ps + " aww -o pid -o args");
            output = new PipedOutputStream();
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
                    LOG.trace("executing '{}'", command);
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
        Integer p = SystemProperties.getInteger("com.sun.management.jmxremote.port");
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
                try {
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
                } catch (InaccessibleObjectException ioe) {
                    // pass
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
