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

    /**
     * @param clazz
     */
    void unregisterAll(Class<?> clazz);
}
