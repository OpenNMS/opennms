/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
