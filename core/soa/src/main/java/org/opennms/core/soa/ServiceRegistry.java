/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.core.soa;

import java.util.Collection;
import java.util.Map;


/**
 * ServiceRegistry
 *
 * @author brozow
 * @version $Id: $
 */
public interface ServiceRegistry {
    
    /**
     * <p>register</p>
     *
     * @param serviceProvider a {@link java.lang.Object} object.
     * @param services a {@link java.lang.Class} object.
     * @return a {@link org.opennms.core.soa.Registration} object.
     */
    public Registration register(Object serviceProvider, Class<?>... services);
    
    /**
     * <p>register</p>
     *
     * @param serviceProvider a {@link java.lang.Object} object.
     * @param properties a {@link java.util.Map} object.
     * @param services a {@link java.lang.Class} object.
     * @return a {@link org.opennms.core.soa.Registration} object.
     */
    public Registration register(Object serviceProvider, Map<String, String> properties, Class<?>... services);
    
    /**
     * <p>findProvider</p>
     *
     * @param seviceInterface a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     */
    public <T> T findProvider(Class<T> seviceInterface);
    
    /**
     * <p>findProvider</p>
     *
     * @param serviceInterface a {@link java.lang.Class} object.
     * @param filter a {@link java.lang.String} object.
     * @param <T> a T object.
     * @return a T object.
     */
    public <T> T findProvider(Class<T> serviceInterface, String filter);
    
    /**
     * <p>findProviders</p>
     *
     * @param service a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a {@link java.util.Collection} object.
     */
    public <T> Collection<T> findProviders(Class<T> service);

    /**
     * <p>findProviders</p>
     *
     * @param service a {@link java.lang.Class} object.
     * @param filter a {@link java.lang.String} object.
     * @param <T> a T object.
     * @return a {@link java.util.Collection} object.
     */
    public <T> Collection<T> findProviders(Class<T> service, String filter);
    
    /**
     * <p>addListener</p>
     *
     * @param service a {@link java.lang.Class} object.
     * @param listener a {@link org.opennms.core.soa.RegistrationListener} object.
     * @param <T> a T object.
     */
    public <T> void addListener(Class<T> service, RegistrationListener<T> listener);
    
    /**
     * <p>addListener</p>
     *
     * @param service a {@link java.lang.Class} object.
     * @param listener a {@link org.opennms.core.soa.RegistrationListener} object.
     * @param notifyForExistingProviders a boolean.
     * @param <T> a T object.
     */
    public <T> void addListener(Class<T> service, RegistrationListener<T> listener, boolean notifyForExistingProviders);
    
    /**
     * <p>removeListener</p>
     *
     * @param service a {@link java.lang.Class} object.
     * @param listener a {@link org.opennms.core.soa.RegistrationListener} object.
     * @param <T> a T object.
     */
    public <T> void removeListener(Class<T> service, RegistrationListener<T> listener);
    

    /**
     * <p>addRegistrationHook</p>
     *
     * @param hook a {@link org.opennms.core.soa.RegistrationHook} object.
     * @param notifyForExistingProviders a boolean.
     */
    public void addRegistrationHook(RegistrationHook hook, boolean notifyForExistingProviders);
    
    /**
     * <p>removeRegistrationHook</p>
     *
     * @param hook a {@link org.opennms.core.soa.RegistrationHook} object.
     */
    public void removeRegistrationHook(RegistrationHook hook);
    
}
