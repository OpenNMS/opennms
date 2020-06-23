/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.gwt.client.service;

import java.util.Collection;
import java.util.Map;

public interface ServiceRegistry {
    
    public Registration register(Object serviceProvider, Class<?>... services);
    
    public Registration register(Object serviceProvider, Map<String, String> properties, Class<?>... services);
    
    public <T> T findProvider(Class<T> serviceInterface);
    
    public <T> T findProvider(Class<T> serviceInterface, String filter);
    
    public <T> Collection<T> findProviders(Class<T> service);
    
    public <T> Collection<T> findProviders(Class<T> service, String filter);
    
    public <T> void addListener(Class<T> service, RegistrationListener<T> listener);
    
    public <T> void addListener(Class<T> service, RegistrationListener<T> listener, boolean notifyForExistingProviders);
    
    public <T> void removeListener(Class<T> service, RegistrationListener<T> listener);
    
    public void addRegistrationHook(RegistrationHook hook, boolean notifyForExistingProviders);
    
    public void removeRegistrationHook(RegistrationHook hook);

    public <T> T cast(Object vertexClickHandler, Class<T> class1);
    
}
