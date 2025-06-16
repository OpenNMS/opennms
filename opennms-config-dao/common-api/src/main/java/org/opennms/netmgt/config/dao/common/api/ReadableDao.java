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
 * A config DAO that supports only reading and reloading.
 *
 * @param <T> the entity type this DAO manages
 */
public interface ReadableDao<T> {
    /**
     * Get the current configuration. Whether or not this configuration is in sync with the backing data source is up to
     * the implementation, see {@link #reload()}.
     * 
     * The value returned by this method may be the result of merging multiple objects together. As a result, mutations
     * to this object may not be visible to other readers and are not permitted.
     * 
     * For cases where mutation and/or persistence of changes is required see {@link WriteableDao#getWriteableConfig()}.
     */
    T getReadOnlyConfig();

    /**
     * Instructs the DAO to reload the configuration from the backing data source immediately. Whether or not the config
     * actually gets reloaded is up to the implementation as it may already be up to date. However invoking this method
     * should guarantee that a subsequent call to {@link #getReadOnlyConfig()} yields the up to date configuration.
     */
    void reload();
}
