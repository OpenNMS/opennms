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
package org.opennms.netmgt.jmx.impl.connection.connectors;

import javax.management.MBeanServer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * <p>MBeanServerProxy class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MBeanServerProxy implements InvocationHandler {

    private Object remoteServer;

    private static final Class<?>[] INTERFACES = { MBeanServer.class };


    /**
     * Creates a new instance of Proxy
     *
     * @param remoteServer a {@link Object} object.
     */
    public MBeanServerProxy(Object remoteServer) {
        this.remoteServer = remoteServer;
    }


    /**
     * <p>invoke</p>
     *
     * @param proxy a {@link Object} object.
     * @param m a {@link java.lang.reflect.Method} object.
     * @param args an array of {@link Object} objects.
     * @return a {@link Object} object.
     * @throws Throwable if any.
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
     * @param server a {@link Object} object.
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
