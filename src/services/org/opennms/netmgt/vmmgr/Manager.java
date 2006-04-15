//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.vmmgr;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.Authenticator;
import java.net.ConnectException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.config.service.Argument;
import org.opennms.netmgt.config.service.Invoke;
import org.opennms.netmgt.config.service.types.InvokeAtType;
import org.opennms.netmgt.config.service.Service;

/**
 * <p>
 * The Manager is reponsible for launching/starting all services in the VM
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
 * its VM and a stop control messge for itself. When a start for a service is
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
public class Manager implements ManagerMBean {
    private static class InvokerService {
        private Service m_service;
        private ObjectInstance m_mbean;
        private Throwable m_badThrowable;
        
        private InvokerService() {
            // doesn't do anything
        }
        
        private static InvokerService[] createServiceArray(Service[] services) {
            InvokerService[] invokerServices =
                new InvokerService[services.length];
            
            for (int i = 0; i < services.length; i++) {
                invokerServices[i] = new InvokerService();
                invokerServices[i].setService(services[i]);
            }
            
            return invokerServices;
        }
        
        private Throwable getBadThrowable() {
            return m_badThrowable;
        }
        
        private void setBadThrowable(Throwable badThrowable) {
            m_badThrowable = badThrowable;
        }
        
        private ObjectInstance getMbean() {
            return m_mbean;
        }
        
        private void setMbean(ObjectInstance mbean) {
            m_mbean = mbean;
        }
        
        private Service getService() {
            return m_service;
        }
        
        private void setService(Service service) {
            m_service = service;
        }

        public boolean isBadService() {
            return (m_badThrowable != null);
        }
    }

    private static class InvokerResult {
        private Service m_service;
        private ObjectInstance m_mbean;
        private Object m_result;
        private Throwable m_throwable;
        
        private InvokerResult(Service service, ObjectInstance mbean,
                              Object result, Throwable throwable) {
            m_service = service;
            m_mbean = mbean;
            m_result = result;
            m_throwable = throwable;
        }
        
        private ObjectInstance getMbean() {
            return m_mbean;
        }
        
        private Object getResult() {
            return m_result;
        }
        
        private Throwable getThrowable() {
            return m_throwable;
        }

        private Service getService() {
            return m_service;
        }

        private void setService(Service service) {
            m_service = service;
        }
    }

    /**
     * Default invoker URL. This is used for getting status information from a
     * running OpenNMS instance.
     */
    public static final String s_defaultInvokeUrl =
        "http://127.0.0.1:8181/invoke?objectname=OpenNMS%3AName=Manager";

    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Manager";

    public static Attribute getAttribute(
            org.opennms.netmgt.config.service.Attribute attrib)
            throws Exception {
        Class attribClass = Class.forName(attrib.getValue().getType());
        Constructor construct =
            attribClass.getConstructor(new Class[] { String.class });

        Object value =
            construct.newInstance(new Object[] { attrib.getValue().getContent() });

        return new Attribute(attrib.getName(), value);
    }

    public static Object getArgument(Argument arg) throws Exception {
        Class argClass = Class.forName(arg.getType());
        Constructor construct =
            argClass.getConstructor(new Class[] { String.class });

        return construct.newInstance(new Object[] { arg.getContent() });
    }

    public static void start(MBeanServer server) {
        Category log = ThreadCategory.getInstance(Manager.class);
        
        log.debug("Beginning startup");
        List resultInfo = getInstancesAndInvoke(server, true,
                                                InvokeAtType.START, false,
                                                true);
        InvokerResult result =
            (InvokerResult) resultInfo.get(resultInfo.size() - 1);
        if (result != null && result.getThrowable() != null) {
            Service service = result.getService();
            String name = service.getName();
            String className = service.getClassName();
            
            String message =
                "An error occurred while attempting to start the \"" +
                name + "\" service (class " + className + ").  "
                + "Shutting down and exiting.";
            log.fatal(message, result.getThrowable());
            System.err.println(message);
            result.getThrowable().printStackTrace();
            stop(server);
            new Manager().doSystemExit();
            // Shouldn't get here
            return;
        }
        log.debug("Startup complete");
    }

    public void stop() {
        List servers = MBeanServerFactory.findMBeanServer(null);
        for (Iterator i = servers.iterator(); i.hasNext(); ) {
            MBeanServer server = (MBeanServer) i.next();
            stop(server);
        }
    }

    public static void stop(MBeanServer server) {
        Category log = ThreadCategory.getInstance(Manager.class);
        
        log.debug("Beginning shutdown");
        getInstancesAndInvoke(server, false, InvokeAtType.STOP, true, false);

        log.debug("Shutdown complete");
    }
    
    public List status() {
        List servers = MBeanServerFactory.findMBeanServer(null);
        List result = new ArrayList();
        for (Iterator i = servers.iterator(); i.hasNext(); ) {
            MBeanServer server = (MBeanServer) i.next();
            result.addAll(status(server));
        }
        return result;
    }
    
    public static List status(MBeanServer server) {
        Category log = ThreadCategory.getInstance(Manager.class);
        
        log.debug("Beginning status check");
        List results = getInstancesAndInvoke(server, false,
                                             InvokeAtType.STATUS, false,
                                             false);
        
        List statusInfo = new ArrayList(results.size());
        for (Iterator i = results.iterator(); i.hasNext(); ) {
            InvokerResult invokerResult = (InvokerResult) i.next();
            if (invokerResult.getThrowable() == null) {
                statusInfo.add("Status: "
                               + invokerResult.getMbean().getObjectName()
                               + " = " + invokerResult.getResult().toString());
            } else {
                statusInfo.add("Status: "
                               + invokerResult.getMbean().getObjectName()
                               + " = STATUS_CHECK_ERROR");
            }
        }
        log.debug("Status check complete");
        
        return statusInfo;
    }

    public static List getInstancesAndInvoke(MBeanServer server,
                                             boolean instantiate,
                                             InvokeAtType at,
                                             boolean reverse,
                                             boolean failFast) {
        ServiceConfigFactory sfact = null;
        try {
            ServiceConfigFactory.init();
            sfact = ServiceConfigFactory.getInstance();
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }

        InvokerService[] services =
            InvokerService.createServiceArray(sfact.getServices());

        if (instantiate) {
            instantiateClasses(server, services);
        } else {
            getObjectInstances(server, services);
        }

        return invokeMethods(server, services, at, reverse, failFast);
    }
    
    private static void instantiateClasses(MBeanServer server,
                                           InvokerService[] invokerServices) {
        Category log = ThreadCategory.getInstance(Manager.class);

        /*
         * Preload the classes and register a new instance with the
         * MBeanServer.
         */
        for (int i = 0; i < invokerServices.length; i++) {
            Service service = invokerServices[i].getService();
            try {
                // preload the class
                if (log.isDebugEnabled()) {
                    log.debug("loading class " + service.getClassName());
                }

                Class cinst = Class.forName(service.getClassName());

                // Get a new instance of the class
                if (log.isDebugEnabled()) {
                    log.debug("create new instance of "
                            + service.getClassName());
                }
                Object bean = cinst.newInstance();

                // Register the mbean
                if (log.isDebugEnabled()) {
                    log.debug("registering mbean instance "
                            + service.getName());
                }
                ObjectName name = new ObjectName(service.getName());
                invokerServices[i].setMbean(server.registerMBean(bean, name));

                // Set attributes
                org.opennms.netmgt.config.service.Attribute[] attribs =
                    service.getAttribute();
                if (attribs != null) {
                    for (int j = 0; j < attribs.length; j++) {
                        if (log.isDebugEnabled()) {
                            log.debug("setting attribute "
                                    + attribs[j].getName());
                        }

                        server.setAttribute(name, getAttribute(attribs[j]));
                    }
                }
            } catch (Throwable t) {
                log.error("An error occured loading the mbean "
                          + service.getName() + " of type "
                          + service.getClassName() + " it will be skipped",
                          t);
                invokerServices[i].setBadThrowable(t);
            }
        }
    }

    public static void getObjectInstances(MBeanServer server,
                                          InvokerService[] invokerServices) {
        Category log = ThreadCategory.getInstance(Manager.class);

        for (int i = 0; i < invokerServices.length; i++) {
            Service service = invokerServices[i].getService();
            try {
                // find the mbean
                if (log.isDebugEnabled()) {
                    log.debug("finding mbean instance "
                              + service.getName());
                }

                ObjectName name = new ObjectName(service.getName());
                invokerServices[i].setMbean(server.getObjectInstance(name));
            } catch (Throwable t) {
                log.error("An error occured loading the mbean "
                          + service.getName() + " of type "
                          + service.getClassName() + " it will be skipped",
                          t);
                invokerServices[i].setBadThrowable(t);
            }
        }
    }

    private static List invokeMethods(MBeanServer server,
                                      InvokerService[] invokerServices,
                                      InvokeAtType at, boolean reverse,
                                      boolean failFast) {
        Category log = ThreadCategory.getInstance(Manager.class);

        Integer[] serviceIndexes = new Integer[invokerServices.length];
        for (int i = 0; i < invokerServices.length; i++) {
            if (!reverse) {
                serviceIndexes[i] = new Integer(i);
            } else {
                serviceIndexes[i] = new Integer(invokerServices.length - 1 - i);
            }
        }

        List resultInfo = new ArrayList(invokerServices.length);
        for (int pass = 0, end = getEndPass(invokerServices); pass <= end;
             pass++) {
            if (log.isDebugEnabled()) {
                log.debug("starting pass " + pass);
            }
            
            for (int i = 0; i < serviceIndexes.length; i++) {
                int j = serviceIndexes[i].intValue();
                String name = invokerServices[j].getService().getName();
                if (invokerServices[j].isBadService()) {
                    if (log.isDebugEnabled()) {
                        log.debug("pass " + pass + " on service " + name
                                  + " is bad: not invoking any more methods"); 
                    }
                    break;
                }
                Invoke[] todo = invokerServices[j].getService().getInvoke();
                for (int k = 0; todo != null && k < todo.length; k++) {
                    if (todo[k].getPass() != pass
                            || !at.equals(todo[k].getAt())) {
                        continue;
                    }
                    
                    Service service = invokerServices[j].getService();
                    ObjectInstance mbean = invokerServices[j].getMbean();

                    if (log.isDebugEnabled()) {
                        log.debug("pass " + pass + " on service " + name
                                  + " will invoke method \""
                                  + todo[k].getMethod() + "\""); 
                    }

                    try {
                        Object result = invoke(server, todo[k], mbean);
                        resultInfo.add(new InvokerResult(service, mbean, result,
                                                         null));
                    } catch (Throwable t) {
                        resultInfo.add(new InvokerResult(service, mbean, null,
                                                         t));
                        if (failFast) {
                            return resultInfo;
                        }
                    }
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("completed pass " + pass);
            }
        }
        
        return resultInfo;
    }

    private static int getEndPass(InvokerService[] invokerServices) {
        int end = 0;
        for (int i = 0; i < invokerServices.length; i++) {
            Invoke[] todo = invokerServices[i].getService().getInvoke();
            for (int j = 0; todo != null && j < todo.length; j++) {
                if (end < todo[j].getPass()) {
                    end = todo[j].getPass();
                }
            }
        }
        
        return end;
    }

    private static Object invoke(MBeanServer server, Invoke invoke,
                                 ObjectInstance mbean) throws Throwable {
        Category log = ThreadCategory.getInstance(Manager.class);

        // invoke!
        try {
            // get the arguments
            Argument[] args = invoke.getArgument();
            Object[] parms = new Object[0];
            String[] sig = new String[0];
            if (args != null && args.length > 0) {
                parms = new Object[args.length];
                sig = new String[args.length];
                for (int k = 0; k < parms.length; k++) {
                    parms[k] = getArgument(args[k]);
                    sig[k] = parms[k].getClass().getName();
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Invoking " + invoke.getMethod()
                          + " on object "
                          + mbean.getObjectName());
            }

            return server.invoke(mbean.getObjectName(),
                                 invoke.getMethod(), parms, sig);
        } catch (Throwable t) {
            log.error("An error occured invoking operation "
                      + invoke.getMethod() + " on MBean "
                      + mbean.getObjectName(), t);
            throw t;
        }
    }

    /**
     * Uncleanly shutdown OpenNMS.  This method calls
     * {@see java.lang.System.exit(int)}, which causes the JVM to
     * exit immediately.  This method is usually invoked via JMX from
     * another process as the last stage of shutting down OpenNMS.
     * 
     * @return does not return
     */
    public void doSystemExit() {
        Category log = ThreadCategory.getInstance(Manager.class);

        log.debug("doSystemExit called... exiting.");
        System.exit(1);
    }

    public static void main(String[] argv) {
        String invokeUrl = s_defaultInvokeUrl;
        boolean verbose = false;

        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        /*
         * Setup Authenticator so that we can provide authentication, if
         * needed, when go to connect to the URL
         */
        Authenticator.setDefault(getAuthenticator());

        // set up the JMX logging
        mx4j.log.Log.redirectTo(new mx4j.log.Log4JLogger());

        for (int i = 0; i < argv.length; i++) {
            if (argv[i].equals("-h")) {
                System.out.println("Usage: java org.opennms.netmgt.vmmgr.Manager "
                                   + "[<options>] <command>");
                System.out.println("Accepted options:");
                System.out.println("        -v              Verbose mode.");
                System.out.println("        -u <URL>        Alternate invoker URL.");
                System.out.println("");
                System.out.println("Accepted commands: start, stop, status");
                System.out.println("");
                System.out.println("The default invoker URL is: "
                        + s_defaultInvokeUrl);
                System.exit(0);
            } else if (argv[i].equals("-v")) {
                verbose = true;
            } else if (argv[i].equals("-u")) {
                invokeUrl = argv[i + 1];
                i++;
            } else if (i != (argv.length - 1)) {
                System.err.println("Invalid command-line option: \""
                        + argv[i] + "\".  Use \"-h\" option for help.");
                System.exit(1);
            } else {
                break;
            }
        }

        if (argv.length == 0) {
            System.err.println("You must specify a command.  Use \"-h\""
                               + " option for help");
            System.exit(1);
        }

        String command = argv[argv.length - 1];

        if ("start".equals(command)) {
            doStartCommand();
        } else if ("stop".equals(command)) {
            doStopCommand(verbose, invokeUrl);
        } else if ("status".equals(command)) {
            doStatusCommand(verbose, invokeUrl);
        } else if ("check".equals(command)) {
            doCheckCommand();
        } else if ("exit".equals(command)) {
            doExitCommand(verbose, invokeUrl);
        } else {
            System.err.println("Invalid command \"" + command + "\".");
            System.err.println("Use \"-h\" option for help.");
            System.exit(1);
        }
    }
    
    private static void doStartCommand() {
        MBeanServer server = MBeanServerFactory.createMBeanServer("OpenNMS");
        start(server);
    }

    private static void doStopCommand(boolean verbose, String invokeUrl) {
        invokeOperation(verbose, invokeUrl, "stop");
    }
    
    private static void doStatusCommand(boolean verbose, String invokeUrl) {
        Category log = ThreadCategory.getInstance(Manager.class);

        try {
            StatusGetter statusGetter = new StatusGetter();

            statusGetter.setVerbose(verbose);
            statusGetter.setInvokeURL(new URL(invokeUrl
                    + "&operation=status"));

            statusGetter.queryStatus();

            if (statusGetter.getStatus() == StatusGetter.STATUS_NOT_RUNNING
                    || statusGetter.getStatus() == StatusGetter.STATUS_CONNECTION_REFUSED) {
                System.exit(3); // According to LSB: 3 - service not running
            } else if (statusGetter.getStatus() == StatusGetter.STATUS_PARTIALLY_RUNNING) {
                /*
                 * According to LSB: reserved for application So, I say
                 * 160 - partially running
                 */
                System.exit(160);
            } else if (statusGetter.getStatus() == StatusGetter.STATUS_RUNNING) {
                System.exit(0); // everything should be good and running
            } else {
                String message = "Unknown status returned from "
                    + "statusGetter.getStatus(): "
                    + statusGetter.getStatus();
                System.err.println(message);
                log.error(message);
                System.exit(1);
            }
        } catch (Throwable t) {
            log.error("error invoking status command", t);
            System.exit(1);
        }
    }

    private static void doCheckCommand() {
        Category log = ThreadCategory.getInstance(Manager.class);

        try {
            DatabaseChecker checker = new DatabaseChecker();
            checker.check();
        } catch (Throwable t) {
            log.error("error invoking check command", t);
            System.err.println(t);
            System.exit(1);
        }
        System.exit(0);
    }

    private static void doExitCommand(boolean verbose, String invokeUrl) {
        invokeOperation(verbose, invokeUrl, "doSystemExit");
    }
    
    private static void invokeOperation(boolean verbose, String invokeUrl,
                                        String operation) {
        Category log = ThreadCategory.getInstance(Manager.class);

        String urlString = invokeUrl + "&operation=" + operation;
        try {
            URL invoke = new URL(urlString);
            InputStream in = invoke.openStream();
            int ch;
            while ((ch = in.read()) != -1) {
                System.out.write((char) ch);
            }
            in.close();
            System.out.println("");
            System.out.flush();
            System.exit(0);
        } catch (ConnectException e) {
            log.error(e.getMessage() + " when attempting to fetch URL \""
                      + urlString + "\"");
            if (verbose) {
                System.out.println(e.getMessage()
                                   + " when attempting to fetch URL \""
                                   + urlString + "\"");
            }
            System.exit(1);
        } catch (Throwable t) {
            log.error("error invoking " + operation + " operation", t);
            System.out.println("error invoking " + operation + " operation");
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static Authenticator getAuthenticator() {
        ServiceConfigFactory sfact = null;
        try {
            ServiceConfigFactory.init();
            sfact = ServiceConfigFactory.getInstance();
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }

        // allocate some storage locations
        Service[] services = sfact.getServices();

        Category log = ThreadCategory.getInstance(Manager.class);

        Service service = null;
        for (int i = 0; i < services.length; i++) {
            if (services[i].getName().equals(":Name=HttpAdaptorMgmt")) {
                service = services[i];
                break;
            }
        }
        
        if (service == null) {
            // Didn't find the service we were looking for
            return null;
        }

        org.opennms.netmgt.config.service.Attribute[] attribs =
            service.getAttribute();

        if (attribs == null) {
            // the AuthenticationMethod is not set, so no authentication
            return null;
        }

        boolean usingBasic = false;
        for (int j = 0; j < attribs.length; j++) {
            if (attribs[j].getName().equals("AuthenticationMethod")) {
                if (!attribs[j].getValue().getContent().equals("basic")) {
                    log.error("AuthenticationMethod is \""
                              + attribs[j].getValue()
                              + "\", but only \"basic\" is supported");
                    return null;
                }
                usingBasic = true;
                break;
            }
        }
            
        if (!usingBasic) {
            // AuthenticationMethod is not set to basic, so no authentication
            return null;
        }

        String username = null;
        String password = null;
        Invoke[] invokes = service.getInvoke();
        for (int j = 0; invokes != null && j < invokes.length; j++) {
            if (invokes[j].getMethod().equals("addAuthorization")) {
                Argument[] args = invokes[j].getArgument();
                if (args != null && args.length == 2
                        && args[0].getContent().equals("manager")) {
                    username = args[0].getContent();
                    password = args[1].getContent();
                    break;
                }
            }
        }
            
        if (username == null || password == null) {
            // Didn't find a username or password
            return null;
        }
            
        final String username_f = username;
        final String password_f = password;
        return new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username_f,
                                                  password_f.toCharArray());
            }
        };
    }
}
