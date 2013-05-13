/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.support.protocol.jmx;

import javax.management.MBeanServer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison</A>
 */

public class MBeanServerProxy implements InvocationHandler {

    private Object remoteServer;

    private static final Class<?>[] INTERFACES = { MBeanServer.class };


    /**
     * Creates a new instance of Proxy
     *
     * @param remoteServer a {@link java.lang.Object} object.
     */
    public MBeanServerProxy(Object remoteServer) {
        this.remoteServer = remoteServer;
    }


    /**
     * <p>invoke</p>
     *
     * @param proxy a {@link java.lang.Object} object.
     * @param m a {@link java.lang.reflect.Method} object.
     * @param args an array of {@link java.lang.Object} objects.
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Throwable if any.
     */
    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {

        Class<?> serverClass = this.remoteServer.getClass();
        Method method = serverClass.getMethod(m.getName(),m.getParameterTypes());
       try {
           return method.invoke(this.remoteServer, args);
       } catch (Throwable e) {
           throw e;
       }
    }

    /**
     * <p>buildServerProxy</p>
     *
     * @param server a {@link java.lang.Object} object.
     * @return a {@link javax.management.MBeanServer} object.
     */
    public static MBeanServer buildServerProxy(Object server) {

        Object proxy =
            Proxy.newProxyInstance(
                MBeanServerProxy.class.getClassLoader(),
                MBeanServerProxy.INTERFACES,
                new MBeanServerProxy(server));

        return (MBeanServer) proxy;
    }


}
