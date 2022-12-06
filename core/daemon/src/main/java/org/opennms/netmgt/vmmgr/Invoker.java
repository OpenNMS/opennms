/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vmmgr;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.opennms.core.logging.Logging;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.config.service.Argument;
import org.opennms.netmgt.config.service.Invoke;
import org.opennms.netmgt.config.service.InvokeAtType;
import org.opennms.netmgt.config.service.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.tongfei.progressbar.DelegatingProgressBarConsumer;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * <p>
 * The Manager is responsible for launching/starting all services in the VM
 * that it is started for. The Manager operates in two modes, normal and
 * server
 * </p>
 * <p>
 * normal mode: In the normal mode, the Manager starts all services configured
 * for its VM in the service-configuration.xml and starts listening for
 * control events on the 'control-broadcast' JMS topic for stop control
 * messages for itself
 * </p>
 * <p>
 * server mode: In the server mode, the Manager starts up and listens on the
 * 'control-broadcast' JMS topic for 'start' control messages for services in
 * its VM and a stop control message for itself. When a start for a service is
 * received, it launches only that service and sends a successful 'running' or
 * an 'error' response to the Controller
 * </p>
 * <p>
 * <strong>Note: </strong>The Manager is NOT intelligent - if it receives a
 * stop control event, it will exit - does not check to see if the services
 * its started are all stopped
 * <p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver</a>
 * @author <a href="mailto:sowmya@opennms.org">Sowmya Nataraj</a>
 */
public class Invoker {
	
	private static final Logger LOG = LoggerFactory.getLogger(Invoker.class);

    /*
     * These are used both as the logging category (prefix) and logger name,
     * so we can handle these logs specially and ensure other logging logic
     * doesn't mess with them.
     */
    public static final String LOG4J_CATEGORY_PROGRESS = "progressbar";
    public static final String LOG4J_CATEGORY_MANAGER_JSON = "manager-json";

    private static final Logger LOG_PROGRESS = LoggerFactory.getLogger(LOG4J_CATEGORY_PROGRESS);
    private static final Logger LOG_MANAGER_JSON = LoggerFactory.getLogger(LOG4J_CATEGORY_MANAGER_JSON);
	
    private MBeanServer m_server;
    private InvokeAtType m_atType;
    private boolean m_reverse = false;
    private boolean m_failFast = true;
    private List<InvokerService> m_services;

    private final Path m_statusPath;
    private final Path m_managerJsonPath;

    private final String m_daemonName;
    private final int m_terminalColumns;

    private static SignalHandler s_handler;

    /**
     * <p>Constructor for Invoker.</p>
     */
    public Invoker() {
        m_statusPath = Path.of(System.getProperty("opennms.home"), "logs", "status-check.txt");
        m_managerJsonPath = Path.of(System.getProperty("opennms.home"), "logs", "manager.json");

        m_daemonName = System.getProperty("opennms.name", "OpenNMS");
        m_terminalColumns = Integer.parseInt(System.getProperty("terminal.columns", "100"));

        this.registerSignalHandlerOnce();
    }
    
    public void instantiateClasses() {

        /*
         * Preload the classes and register a new instance with the
         * MBeanServer.
         */
        for (InvokerService invokerService : getServices()) {
            Service service = invokerService.getService();
            try {
                // preload the class
            	LOG.debug("loading class {}", service.getClassName());
                

                Class<?> clazz = Class.forName(service.getClassName());

                // Get a new instance of the class
                LOG.debug("create new instance of {}", service.getClassName());
                
                Map<String,String> mdc = Logging.getCopyOfContextMap();
                Object bean;
                try {
                    bean = clazz.getDeclaredConstructor().newInstance();
                } finally {
                    Logging.setContextMap(mdc);
                }

                // Register the mbean
                LOG.debug("registering mbean instance {}", service.getName());
                
                ObjectName name = new ObjectName(service.getName());
                invokerService.setMbean(getServer().registerMBean(bean, name));

                // Set attributes
                final List<org.opennms.netmgt.config.service.Attribute> attribs = service.getAttributes();
                if (attribs != null) {
                    for (final org.opennms.netmgt.config.service.Attribute attrib : attribs) {
                    	LOG.debug("setting attribute {}", attrib.getName());
                        getServer().setAttribute(name, getAttribute(attrib));
                    }
                }
            } catch (Throwable t) {
                LOG.error("An error occurred loading the mbean {} of type {}", service.getName(), service.getClassName(), t);
                invokerService.setBadThrowable(t);
            }
        }
    }

    private void registerSignalHandlerOnce() {
        if (s_handler != null) return;

        try {
            s_handler = Signal.handle(new Signal("USR1"), signal -> writeStatusUpdate());
        } catch (final Throwable t) {
            System.err.println("WARNING: failed to register service status signal handler");
            t.printStackTrace();
        }
    }

    private void writeStatusUpdate() {
        if (m_statusPath == null) {
            System.err.println("WARNING: status signal triggered, but no status path is configured!");
            return;
        }

        final var output = new StringBuilder();

        for (final var invokerService : getServices()) {
            final var serviceName = invokerService.getService().getName();
            for (final var invoke : invokerService.getService().getInvokes()) {
                if ("status".equals(invoke.getMethod())) {
                    try {
                        Object result = invoke(invoke, invokerService.getMbean());
                        output.append(StatusGetter.formatStatusEntry(serviceName, result.toString())).append("\n");
                    } catch (final Throwable e) {
                        System.err.println("ERROR: an error occurred while calling 'status' on " + serviceName);
                        e.printStackTrace();
                        output.append(StatusGetter.formatStatusEntry(serviceName, "STATUS_CHECK_ERROR")).append("\n");
                        output.append(serviceName).append("=").append("STATUS_CHECK_ERROR").append("\n");
                    }
                }
            }
        }

        try {
            Files.writeString(m_statusPath, output.toString(), Charset.defaultCharset(), CREATE, TRUNCATE_EXISTING);
        } catch (final IOException e) {
            System.err.println("ERROR: failed to write current status to " + m_statusPath);
            e.printStackTrace();
        }
    }

    public void getObjectInstances() {
        for (InvokerService invokerService : getServices()) {
            Service service = invokerService.getService();
            try {
                // find the mbean
            	LOG.debug("finding mbean instance {}", service.getName());

                ObjectName name = new ObjectName(service.getName());
                invokerService.setMbean(getServer().getObjectInstance(name));
            } catch (Throwable t) {
		LOG.error("An error occurred loading the mbean {} of type {} it will be skipped", service.getName(), service.getClassName(), t);
                invokerService.setBadThrowable(t);
            }
        }
    }

    /**
     * <p>invokeMethods</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<InvokerResult> invokeMethods() {
        int count = 0;
        for (int pass = 0, end = getLastPass(); pass <= end; pass++) {
            for (InvokerService invokerService : getServices()) {
                for (final Invoke invoke : invokerService.getService().getInvokes()) {
                    if (invoke.getPass() != pass || !getAtType().equals(invoke.getAt())) {
                        continue;
                    }

                    count++;
                }
            }
        }

        Optional<Duration> startingElapsed = getETA();
        var startTime = System.currentTimeMillis();

        String taskName = getAtType().getPresentParticiple() + " " + m_daemonName + ":";
        ProgressBarBuilder pbb = new ProgressBarBuilder()
                    .setConsumer(new DelegatingProgressBarConsumer(a -> logProgressUpdate(a + '\r'), m_terminalColumns))
                    .setTaskName(taskName)
                    .setInitialMax(count)
                    .setStyle(ProgressBarStyle.ASCII)
                    .continuousUpdate(); // ensures the duration increases at least 1/sec

        if (startingElapsed.isPresent()) {
            pbb.startsFrom(0, startingElapsed.get());

            var estimatedCompletion = System.currentTimeMillis() + startingElapsed.get().toMillis();
            pbb.setEtaFunction(a -> Optional.of(Duration.ofMillis(Math.max(estimatedCompletion - System.currentTimeMillis(), 0))));
        } else {
            pbb.hideEta();
        }

        logProgressUpdate("\r");
        try (var pb = pbb.build()) {
            var resultInfo = invokeMethods(pb);
            var elapsed = System.currentTimeMillis() - startTime;

            // We only want to log start and stop invoke types so the log file doesn't get filled with status entries.
            if (getAtType() == InvokeAtType.START || getAtType() == InvokeAtType.STOP) {
                Logging.withPrefix(LOG4J_CATEGORY_MANAGER_JSON, () -> {
                    var elapsedLog = new JSONStringer()
                            .object()
                            .key("time").value(StringUtils.iso8601OffsetString(new Date(), ZoneId.of("Z"), null))
                            .key("elapsed").value(Duration.ofMillis(elapsed).toString())
                            .key("type").value(getAtType().name())
                            .endObject()
                            .toString();
                    LOG_MANAGER_JSON.info(elapsedLog);
                });
            }

            return resultInfo;
        } finally {
            // Advance to next line, so we don't erase the progress bar
            Invoker.logProgressUpdate("\n");
        }
    }

    private Optional<Duration> getETA() {
        try (var reader = new BufferedReader(new FileReader(m_managerJsonPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                var o = new JSONObject(line);
                if (o.has("type")
                        && o.getString("type").equals(getAtType().name())
                        && o.has("elapsed")) {
                    return Optional.of(Duration.parse(o.getString("elapsed")));
                }
            }
        } catch (FileNotFoundException e) {
            // Ignore since it might have not been created yet
        } catch (IOException e) {
            LOG.info("Received unexpected exception when trying to read manager JSON file: " + e, e);
        }
        return Optional.empty();
    }

    /**
     * <p>invokeMethods</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<InvokerResult> invokeMethods(ProgressBar pb) {
        List<InvokerService> invokerServicesOrdered;
        if (isReverse()) {
            invokerServicesOrdered = new ArrayList<>(getServices());
            Collections.reverse(invokerServicesOrdered);
        } else {
            // We can  use the original list
            invokerServicesOrdered = getServices();
        }
        
        List<InvokerResult> resultInfo = new ArrayList<>(invokerServicesOrdered.size());
        for (int pass = 0, end = getLastPass(); pass <= end; pass++) {
        	LOG.debug("starting pass {}", pass);
            

            for (InvokerService invokerService : invokerServicesOrdered) {
                Service service = invokerService.getService();
                String name = invokerService.getService().getName();
                ObjectInstance mbean = invokerService.getMbean();
                String nameShort = name.replaceFirst("^OpenNMS:Name=", "");

                if (invokerService.isBadService()) {
                    resultInfo.add(new InvokerResult(service, mbean, null, invokerService.getBadThrowable()));
                    if (isFailFast()) {
                        return resultInfo;
                    }
                }
                
                for (final Invoke invoke : invokerService.getService().getInvokes()) {
                    if (invoke.getPass() != pass || !getAtType().equals(invoke.getAt())) {
                        continue;
                    }

                    LOG.debug("pass {} on service {} will invoke method \"{}\" as step {}",
                            pass, name, invoke.getMethod(), pb.getCurrent());
                    pb.setExtraMessage("Pass " + pass + ": " + nameShort);
                    pb.refresh(); // make sure we output that the service changed so we see updates for quick services

                    try {
                        Object result = invoke(invoke, mbean);
                        resultInfo.add(new InvokerResult(service, mbean, result, null));
                    } catch (Throwable t) {
                        resultInfo.add(new InvokerResult(service, mbean, null, t));
                        if (isFailFast()) {
                            return resultInfo;
                        }
                    }

                    pb.step();
                }
            }
            
            LOG.debug("completed pass {}", pass);
            pb.setExtraMessage("Pass " + pass + ": Complete");
        }
        pb.setExtraMessage("All Passes Complete");

        return resultInfo;
    }

    public static void logProgressUpdate(String message) {
        Logging.withPrefix(LOG4J_CATEGORY_PROGRESS, () -> LOG_PROGRESS.info(message));
    }

    /**
     * Get the last pass for a set of InvokerServices.
     * 
     * @return highest pass value found for all Invoke objects in the
     *      invokerServices list
     */
    private int getLastPass() {
        List<InvokerService> invokerServices = getServices();
        
        int end = 0;
        
        for (final InvokerService invokerService : invokerServices) {
            final List<Invoke> invokes = invokerService.getService().getInvokes();
            if (invokes == null) {
                continue;
            }
            
            for (final Invoke invoke : invokes) {
                if (invoke.getPass() > end) {
                    end = invoke.getPass();
                }
            }
        }
        
        return end;
    }

    private Object invoke(final Invoke invoke, final ObjectInstance mbean) throws Throwable {
        List<Argument> args = invoke.getArguments();
        Object[] parms = new Object[0];
        String[] sig = new String[0];
        if (args != null && args.size() > 0) {
            parms = new Object[args.size()];
            sig = new String[args.size()];
            for (int k = 0; k < parms.length; k++) {
                try {
                    parms[k] = getArgument(args.get(k));
                } catch (Throwable t) {
			LOG.error("An error occurred building argument {} for operation {} on MBean {}", k, invoke.getMethod(), mbean.getObjectName(), t);
                  throw t;
                }
                sig[k] = parms[k].getClass().getName();
            }
        }

        if ("status".equals(invoke.getMethod())) {
            LOG.debug("Invoking {} on object {}", invoke.getMethod(), mbean.getObjectName());
        } else {
            LOG.info("Invoking {} on object {}", invoke.getMethod(), mbean.getObjectName());
        }
        

        Object object;
        try {
        	Map<String,String> mdc = Logging.getCopyOfContextMap();
            try {
                object = getServer().invoke(mbean.getObjectName(), invoke.getMethod(), parms, sig);
            } finally {
            	Logging.setContextMap(mdc);
            }
        } catch (Throwable t) {
            LOG.error("An error occurred invoking operation {} on MBean {}", invoke.getMethod(), mbean.getObjectName(), t);
            throw t;
        }

        if ("status".equals(invoke.getMethod())) {
            LOG.debug("Invocation {} successful for MBean {}", invoke.getMethod(), mbean.getObjectName());
        } else {
            LOG.info("Invocation {} successful for MBean {}", invoke.getMethod(), mbean.getObjectName());
        }

        return object;
    }

    private Attribute getAttribute(org.opennms.netmgt.config.service.Attribute attrib) throws Exception {
        Class<?> attribClass = Class.forName(attrib.getValue().getType());
        Constructor<?> construct = attribClass.getConstructor(String.class);

        Object value;
        Map<String,String> mdc = Logging.getCopyOfContextMap();
        try {
            value = construct.newInstance(attrib.getValue().getContent());
        } finally {
            Logging.setContextMap(mdc);
        }

        return new Attribute(attrib.getName(), value);
    }

    private Object getArgument(Argument arg) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> argClass = Class.forName(arg.getType());
        Constructor<?> construct = argClass.getConstructor(String.class);

        Map<String,String> mdc = Logging.getCopyOfContextMap();
        try {
            return construct.newInstance(arg.getValue().orElse(null));
        } finally {
            Logging.setContextMap(mdc);
        }
    }

    /**
     * <p>getAtType</p>
     *
     * @return a {@link org.opennms.netmgt.config.service.InvokeAtType} object.
     */
    public InvokeAtType getAtType() {
        return m_atType;
    }

    /**
     * <p>setAtType</p>
     *
     * @param atType a {@link org.opennms.netmgt.config.service.InvokeAtType} object.
     */
    public void setAtType(InvokeAtType atType) {
        m_atType = atType;
    }

    /**
     * <p>isFailFast</p>
     *
     * @return a boolean.
     */
    public boolean isFailFast() {
        return m_failFast;
    }

    /**
     * <p>setFailFast</p>
     *
     * @param failFast a boolean.
     */
    public void setFailFast(boolean failFast) {
        m_failFast = failFast;
    }

    /**
     * <p>isReverse</p>
     *
     * @return a boolean.
     */
    public boolean isReverse() {
        return m_reverse;
    }

    /**
     * <p>setReverse</p>
     *
     * @param reverse a boolean.
     */
    public void setReverse(boolean reverse) {
        m_reverse = reverse;
    }

    /**
     * <p>getServer</p>
     *
     * @return a {@link javax.management.MBeanServer} object.
     */
    public MBeanServer getServer() {
        return m_server;
    }

    /**
     * <p>setServer</p>
     *
     * @param server a {@link javax.management.MBeanServer} object.
     */
    public void setServer(MBeanServer server) {
        m_server = server;
    }

    /**
     * <p>getServices</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<InvokerService> getServices() {
        return m_services;
    }

    /**
     * <p>setServices</p>
     *
     * @param services a {@link java.util.List} object.
     */
    public void setServices(List<InvokerService> services) {
        m_services = services;
    }
}
