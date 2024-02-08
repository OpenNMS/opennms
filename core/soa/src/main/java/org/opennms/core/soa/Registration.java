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

import java.util.Map;

/**
 * Registration
 *
 * @author brozow
 * @version $Id: $
 */
public interface Registration {
    
    /**
     * <p>getRegistry</p>
     *
     * @return a {@link org.opennms.core.soa.ServiceRegistry} object.
     */
    public ServiceRegistry getRegistry();
    
    /**
     * <p>getProvidedInterfaces</p>
     *
     * @return an array of {@link java.lang.Class} objects.
     */
    public Class<?>[] getProvidedInterfaces();
    
    /**
     * <p>getProvider</p>
     *
     * @param service a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a T object.
     */
    public <T> T getProvider(Class<T> service);
    
    public Object getProvider();
    
    /**
     * <p>getProperties</p>
     *
     * @return a {@link java.util.Map} object.
     */
    public Map<String, String> getProperties();
    
    /**
     * <p>isUnregistered</p>
     *
     * @return a boolean.
     */
    public boolean isUnregistered();
    
    /**
     * <p>unregister</p>
     */
    public void unregister();
    
}
