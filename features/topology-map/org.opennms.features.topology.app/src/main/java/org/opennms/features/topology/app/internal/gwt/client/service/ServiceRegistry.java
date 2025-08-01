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
