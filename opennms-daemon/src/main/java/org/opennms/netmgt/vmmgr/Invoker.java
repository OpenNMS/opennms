/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights
 * reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included
 * code and modified
 * code that was published under the GNU General Public License. Copyrights
 * for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 May 22: Wrap all invocations of service methods in a try/finally block
 *              that saves the log4j category prefix before calling the method
 *              and restores it upon exit.  Caught by david@. - dj@opennms.org
 * 2003 Jan 31: Cleaned up some unused imports.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
 * reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.vmmgr;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.config.service.Argument;
import org.opennms.netmgt.config.service.Invoke;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.types.InvokeAtType;

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
 * @author <a href="http://www.opennms.org">OpenNMS.org</a>
 */
public class Invoker {
    private MBeanServer m_server;
    private InvokeAtType m_atType;
    private boolean m_reverse = false;
    private boolean m_failFast = true;
    private List<InvokerService> m_services;
    
    public Invoker() {
        
    }
    
    public static ServiceConfigFactory getDefaultServiceConfigFactory() {
        try {
            ServiceConfigFactory.init();
            return ServiceConfigFactory.getInstance();
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
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
                if (log().isDebugEnabled()) {
                    log().debug("loading class " + service.getClassName());
                }

                Class<?> clazz = Class.forName(service.getClassName());

                // Get a new instance of the class
                if (log().isDebugEnabled()) {
                    log().debug("create new instance of "
                            + service.getClassName());
                }

                String log4jPrefix = ThreadCategory.getPrefix();
                Object bean;
                try {
                    bean = clazz.newInstance();
                } finally {
                    ThreadCategory.setPrefix(log4jPrefix);
                }

                // Register the mbean
                if (log().isDebugEnabled()) {
                    log().debug("registering mbean instance "
                            + service.getName());
                }
                ObjectName name = new ObjectName(service.getName());
                invokerService.setMbean(getServer().registerMBean(bean, name));

                // Set attributes
                org.opennms.netmgt.config.service.Attribute[] attribs =
                    service.getAttribute();
                if (attribs != null) {
                    for (org.opennms.netmgt.config.service.Attribute attrib : attribs) {
                        if (log().isDebugEnabled()) {
                            log().debug("setting attribute "
                                    + attrib.getName());
                        }

                        getServer().setAttribute(name, getAttribute(attrib));
                    }
                }
            } catch (Throwable t) {
                log().error("An error occurred loading the mbean "
                          + service.getName() + " of type "
                          + service.getClassName() + ": " + t,
                          t);
                invokerService.setBadThrowable(t);
            }
        }
    }

    public void getObjectInstances() {
        for (InvokerService invokerService : getServices()) {
            Service service = invokerService.getService();
            try {
                // find the mbean
                if (log().isDebugEnabled()) {
                    log().debug("finding mbean instance " + service.getName());
                }

                ObjectName name = new ObjectName(service.getName());
                invokerService.setMbean(getServer().getObjectInstance(name));
            } catch (Throwable t) {
                log().error("An error occurred loading the mbean "
                          + service.getName() + " of type "
                          + service.getClassName() + " it will be skipped",
                          t);
                invokerService.setBadThrowable(t);
            }
        }
    }

    public List<InvokerResult> invokeMethods() {
        List<InvokerService> invokerServicesOrdered;
        if (isReverse()) {
            invokerServicesOrdered = new ArrayList<InvokerService>(getServices());
            Collections.reverse(invokerServicesOrdered);
        } else {
            // We can  use the original list
            invokerServicesOrdered = getServices();
        }
        
        List<InvokerResult> resultInfo = new ArrayList<InvokerResult>(invokerServicesOrdered.size());
        for (int pass = 0, end = getLastPass(); pass <= end; pass++) {
            if (log().isDebugEnabled()) {
                log().debug("starting pass " + pass);
            }

            for (InvokerService invokerService : invokerServicesOrdered) {
                Service service = invokerService.getService();
                String name = invokerService.getService().getName();
                ObjectInstance mbean = invokerService.getMbean();

                if (invokerService.isBadService()) {
                    resultInfo.add(new InvokerResult(service, mbean, null, invokerService.getBadThrowable()));
                    if (isFailFast()) {
                        return resultInfo;
                    }
                }
                
                for (Invoke invoke : invokerService.getService().getInvoke()) {
                    if (invoke.getPass() != pass || !getAtType().equals(invoke.getAt())) {
                        continue;
                    }

                    if (log().isDebugEnabled()) {
                        log().debug("pass " + pass + " on service " + name
                                + " will invoke method \""
                                + invoke.getMethod() + "\""); 
                    }

                    try {
                        Object result = invoke(invoke, mbean);
                        resultInfo.add(new InvokerResult(service, mbean, result, null));
                    } catch (Throwable t) {
                        resultInfo.add(new InvokerResult(service, mbean, null, t));
                        if (isFailFast()) {
                            return resultInfo;
                        }
                    }
                }
            }
            
            if (log().isDebugEnabled()) {
                log().debug("completed pass " + pass);
            }
        }

        return resultInfo;
    }

    /**
     * Get the last pass for a set of InvokerServices.
     * 
     * @param invokerServices list to look at
     * @return highest pass value found for all Invoke objects in the
     *      invokerServices list
     */
    private int getLastPass() {
        List<InvokerService> invokerServices = getServices();
        
        int end = 0;
        
        for (InvokerService invokerService : invokerServices) {
            Invoke[] invokes = invokerService.getService().getInvoke();
            if (invokes == null) {
                continue;
            }
            
            for (Invoke invoke : invokes) {
                if (invoke.getPass() > end) {
                    end = invoke.getPass();
                }
            }
        }
        
        return end;
    }

    private Object invoke(Invoke invoke, ObjectInstance mbean) throws Throwable {
        Argument[] args = invoke.getArgument();
        Object[] parms = new Object[0];
        String[] sig = new String[0];
        if (args != null && args.length > 0) {
            parms = new Object[args.length];
            sig = new String[args.length];
            for (int k = 0; k < parms.length; k++) {
                try {
                    parms[k] = getArgument(args[k]);
                } catch (Throwable t) {
                    log().error("An error occurred building argument "
                            + k + " for operation "+ invoke.getMethod()
                            + " on MBean " + mbean.getObjectName() + ": " + t,
                            t);
                  throw t;
                }
                sig[k] = parms[k].getClass().getName();
            }
        }

        if (log().isDebugEnabled()) {
            log().debug("Invoking " + invoke.getMethod()
                      + " on object " + mbean.getObjectName());
        }

        Object object;
        try {
            String log4jPrefix = ThreadCategory.getPrefix(); 
            try {
                object = getServer().invoke(mbean.getObjectName(), invoke.getMethod(), parms, sig);
            } finally {
                ThreadCategory.setPrefix(log4jPrefix);
            }
        } catch (Throwable t) {
            log().error("An error occurred invoking operation "
                      + invoke.getMethod() + " on MBean "
                      + mbean.getObjectName() + ": " + t, t);
            throw t;
        }

        log().debug("Invocation successful.");

        return object;
    }

    private Attribute getAttribute(org.opennms.netmgt.config.service.Attribute attrib) throws Exception {
        Class<?> attribClass = Class.forName(attrib.getValue().getType());
        Constructor<?> construct = attribClass.getConstructor(new Class[] { String.class });

        Object value;
        String log4jPrefix = ThreadCategory.getPrefix(); 
        try {
            value = construct.newInstance(new Object[] { attrib.getValue().getContent() });
        } finally {
            ThreadCategory.setPrefix(log4jPrefix);
        }

        return new Attribute(attrib.getName(), value);
    }

    private Object getArgument(Argument arg) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> argClass = Class.forName(arg.getType());
        Constructor<?> construct = argClass.getConstructor(new Class[] { String.class });

        String log4jPrefix = ThreadCategory.getPrefix(); 
        try {
            return construct.newInstance(new Object[] { arg.getContent() });
        } finally {
            ThreadCategory.setPrefix(log4jPrefix);
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public InvokeAtType getAtType() {
        return m_atType;
    }

    public void setAtType(InvokeAtType atType) {
        m_atType = atType;
    }

    public boolean isFailFast() {
        return m_failFast;
    }

    public void setFailFast(boolean failFast) {
        m_failFast = failFast;
    }

    public boolean isReverse() {
        return m_reverse;
    }

    public void setReverse(boolean reverse) {
        m_reverse = reverse;
    }

    public MBeanServer getServer() {
        return m_server;
    }

    public void setServer(MBeanServer server) {
        m_server = server;
    }

    public List<InvokerService> getServices() {
        return m_services;
    }

    public void setServices(List<InvokerService> services) {
        m_services = services;
    }
}
