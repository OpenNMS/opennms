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
package org.opennms.core.config.api;

/**
 * This interface allows extensions to provide configuration objects of a given type.
 *
 * See {@link ConfigReloadContainer}
 *
 * @author jwhite
 */
public interface ConfigurationProvider {

    /**
     * Retrieve a class reference to the type of object returned by {@link #getObject()}.
     *
     * This is used instead of generics to be OSGi friendly.
     *
     * @return the type of object returned by {@link #getObject()}
     */
    Class<?> getType();

    /**
     * Retrieve the actual configuration bean.
     *
     * @return the configuration bean, must be non-null
     */
    Object getObject();

    /**
     * @return the last time (in ms) at which the configuration bean was updated
     */
    long getLastUpdate();

    /**
     * Notifies this configuration provider that it was registered with the {@link ConfigReloadContainer}.
     */
    void registeredToConfigReloadContainer();

    /**
     * Notifies this configuration provider that it was unregistered from the {@link ConfigReloadContainer}.
     */
    void deregisteredFromConfigReloadContainer();

}
