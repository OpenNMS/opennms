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
package org.opennms.netmgt.provision.service;

import java.util.Collection;

import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;

/*
 * PluginRegistry
 * @author brozow
 */
/**
 * <p>PluginRegistry interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface PluginRegistry {

    /**
     * <p>getAllPlugins</p>
     *
     * @param pluginClass a {@link java.lang.Class} object.
     * @param <T> a T object.
     * @return a {@link java.util.Collection} object.
     */
    public abstract <T> Collection<T> getAllPlugins(Class<T> pluginClass);

    /**
     * <p>getPluginInstance</p>
     *
     * @param pluginClass a {@link java.lang.Class} object.
     * @param pluginConfig a {@link org.opennms.netmgt.provision.persist.foreignsource.PluginConfig} object.
     * @param <T> a T object.
     * @return a T object.
     */
    public abstract <T> T getPluginInstance(Class<T> pluginClass,
            PluginConfig pluginConfig);
    

}
