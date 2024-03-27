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
package org.opennms.osgi;


import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.opennms.vaadin.extender.SessionListener;

/**
 * The {@linkplain org.opennms.osgi.OnmsServiceManager} is a abstraction layer above the {@link org.osgi.framework.BundleContext}.
 * Its intention is to provide a so called session-scope for services.
 * The session-scope is realized by the {@link VaadinApplicationContext}-object.<br/><br>/
 * <p/>
 * Therefore you should always use {@linkplain org.opennms.osgi.OnmsServiceManager} instead of the {@link org.osgi.framework.BundleContext}.<br/><br/>
 * <p/>
 * A {@linkplain org.opennms.osgi.OnmsServiceManager} also listens to session events (e.g. to remove all registered services
 * from the OSGi-container for a session when the session has already been destroyed).
 *
 * @author Markus von Rüden
 */
public interface OnmsServiceManager extends SessionListener {

    /**
     * Register a service with session scope.
     *
     * @param serviceBean        The service to be registered. Must not be null.
     * @param applicationContext The session scope. Must not be null.
     */
    <T> void registerAsService(Class<T> serviceClass, T serviceBean, VaadinApplicationContext applicationContext);

    /**
     * Registers a service with session scope but allows to set additional Properties.
     *
     * @param serviceBean          The service to be registered. Must not be null.
     * @param applicationContext   the session scope. Must not be null.
     * @param additionalProperties Additional Properties. Must not be null.
     */
    <T> void registerAsService(Class<T> serviceClass, T serviceBean, VaadinApplicationContext applicationContext, Dictionary<String,Object> additionalProperties);

    /**
     * Returns a service in session-scope. Be aware that if there are multiple services registered
     * for the given class, only the first one is returned.
     *
     * @param clazz              The type of the service. Must not be null.
     * @param applicationContext The session-scope. Must not be null.
     */
    <T> T getService(Class<T> clazz, VaadinApplicationContext applicationContext);

    /**
     * Returns all registered services within session-scope and may be consider additional Properties.
     *
     * @param clazz                the type of the service. Must not be null.
     * @param applicationContext   The session scope. Must not be null.
     * @param additionalProperties optional additional propeties. Must not be null.
     */
    <T> List<T> getServices(Class<T> clazz, VaadinApplicationContext applicationContext, Hashtable<String,Object> additionalProperties);

    VaadinApplicationContext createApplicationContext(VaadinApplicationContextCreator creator);

    EventRegistry getEventRegistry();
}
