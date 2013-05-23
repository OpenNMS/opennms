/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.core.concurrent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;

import org.opennms.core.fiber.Fiber;

/**
 * <p>VMTaskFiber class.</p>
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 */
public class VMTaskFiber implements Fiber, Runnable {
    /**
     * The name of the entry method. This is the same as it is for the JVM,
     * which is <code>main</code>.
     */
    private static final String MAIN_METHOD_NAME = "main";

    /**
     * The list of classes that are passed as entry arguments.
     */
    private static final String MAIN_PARAMETER_TYPES[] = { "[Ljava.lang.String;" };

    /**
     * The return type for the entry method.
     */
    private static final String MAIN_RETURN_TYPE = "void";

    /**
     * The name prefixed to the task name to form the name for the thread group.
     */
    private static final String THREADGROUP_NAME_PREFIX = "TaskGroup:";

    /**
     * The name of the VM task.
     */
    private String m_taskName;

    /**
     * The thread group for the task.
     */
    private ThreadGroup m_thrGroup;

    /**
     * The class loader used to resolve classes for the thread group.
     */
    private ClassLoader m_classLoader;

    /**
     * The entry class.
     */
    private Class<?> m_entryClass;

    /**
     * The entry method.
     */
    private Method m_entryMethod;

    /**
     * The entry arguments.
     */
    private String[] m_mainArgs;

    /**
     * The fiber's status.
     */
    private int m_fiberStatus;

    /**
     * <P>
     * This method attempts to find the method with the signature <EM>public
     * static void main(String[])</EM> if it is part of the passed class. The
     * first matching method is returned to the caller.
     * </P>
     * 
     * @param c
     *            The class to search for the main method.
     * 
     * @return The matching method if one is found. If one is not found then a
     *         null is returned.
     * 
     */
    private static Method findMain(Class<?> c) {

        Method[] methods = c.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Class<?>[] args = methods[i].getParameterTypes();
            Class<?> retType = methods[i].getReturnType();
            int modifiers = methods[i].getModifiers();
            boolean validModifiers = Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers);

            if (validModifiers && methods[i].getName().equals(MAIN_METHOD_NAME) && args.length == MAIN_PARAMETER_TYPES.length && retType.getName().equals(MAIN_RETURN_TYPE)) {
                // do a looping check to figure out if it
                // is the correct ordering of parameters.
                //
                boolean isOK = true;
                for (int x = 0; isOK && x < args.length; x++) {
                    if (args[x].getName().equals(MAIN_PARAMETER_TYPES[x]) == false)
                        isOK = false;
                }

                // it has all the qualifications of being
                //
                // public static void main(String[] args)
                //
                if (isOK)
                    return methods[i];
            }
        }
        return null;
    }

    /**
     * Constructs a new Virtual Macine Task Fiber. The task has a name and is
     * passed all the information to invoke the class' main method. When the
     * class is loaded it is allocated a new class loader used to locate all of
     * it's resources.
     *
     * @param taskName
     *            The name of the task
     * @param entryClassName
     *            The name of the entry class.
     * @param entryArguments
     *            The String array passed to main.
     * @param searchPaths
     *            The URL's used to locate resources and classes.
     * @throws java.lang.ClassNotFoundException
     *             Thrown if the entry class is not found.
     * @throws java.lang.NoSuchMethodException
     *             Thrown if the <code>main</code> is not found on the entry
     *             class.
     * @see java.net.URLClassLoader
     */
    public VMTaskFiber(String taskName, String entryClassName, String[] entryArguments, URL[] searchPaths) throws ClassNotFoundException, NoSuchMethodException

    {
        m_taskName = taskName;
        m_mainArgs = entryArguments;

        m_thrGroup = new ThreadGroup(THREADGROUP_NAME_PREFIX + m_taskName);
        m_thrGroup.setDaemon(false);

        m_classLoader = new URLClassLoader(searchPaths);

        m_entryClass = m_classLoader.loadClass(entryClassName);
        m_entryMethod = findMain(m_entryClass);
        if (m_entryMethod == null)
            throw new NoSuchMethodException("main() method not found for class " + entryClassName);

        m_fiberStatus = START_PENDING;
    }

    /**
     * This method invokes the entry method on the main class. The method is
     * called after the internal thread starts up, and returns when the entry
     * method's thread exits.
     */
    @Override
    public void run() {
        Object[] passedArgs = new Object[1];
        passedArgs[0] = m_mainArgs;

        // Now actually call the entry point method with the
        // correct arguments. This should kick start the service
        // it may or may not return before the service exits.
        //
        synchronized (this) {
            m_fiberStatus = RUNNING;
        }

        try {
            m_entryMethod.invoke(null, passedArgs);
        } catch (Throwable t) {
            // do nothing
        } finally {
            synchronized (this) {
                m_fiberStatus = STOPPED;
            }
        }
    }

    /**
     * Starts the current fiber running.
     */
    @Override
    public synchronized void start() {
        m_fiberStatus = STARTING;
        Thread t = new Thread(m_thrGroup, this, m_taskName + "-main");
        t.setDaemon(false);
        t.setContextClassLoader(m_classLoader);
        t.start();
    }

    /**
     * Stops the current fiber. Since the JVM does not provide way to kill
     * threads, the thread group is interrupted and the status is set to
     * <code>STOP_PENDING</code>. When the main thread exits then the service
     * is considered stopped!
     */
    @Override
    public synchronized void stop() {
        if (m_fiberStatus != STOPPED)
            m_fiberStatus = STOP_PENDING;
        m_thrGroup.interrupt();
    }

    /**
     * Returns the current status of the fiber.
     *
     * @return The current status of the fiber.
     */
    @Override
    public synchronized int getStatus() {
        return m_fiberStatus;
    }

    /**
     * Returns the name for the virtual machine task.
     *
     * @return The VM Task's name.
     */
    @Override
    public String getName() {
        return m_taskName;
    }
}
