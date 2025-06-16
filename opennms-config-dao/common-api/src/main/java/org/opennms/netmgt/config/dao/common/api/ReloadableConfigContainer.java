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
package org.opennms.netmgt.config.dao.common.api;

/**
 * A container that provides access to configuration that is backed by some persistent source.
 * 
 * @param <T> the configuration entity being provided
 */
public interface ReloadableConfigContainer<T> {
    /**
     * Instruct the container to reload the in-memory configuration from the backing configuration. This may block if
     * the backing configuration is not immediately available.
     */
    void reload();

    /**
     * @return the in-memory copy of the configuration which may be reloaded as part of this request depending on the
     * implementation
     */
    T getConfig();
}
