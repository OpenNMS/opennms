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
 * The writeable version of {@link ReadableDao}.
 */
public interface WriteableDao<T> extends ReadableDao<T> {
    /**
     * This method returns the mutable configuration object for which any changes made to it will be persisted and
     * reflected after a call to {@link #saveConfig()}.
     */
    T getWriteableConfig();

    /**
     * Save the current configuration held by the DAO.
     * <p>
     * The configuration held by the DAO may not necessarily be the same configuration reference as returned by
     * {@link #getWriteableConfig()} if the config has been reloaded in between calls. This implies any changes to the
     * reference
     * returned by {@link #getWriteableConfig()} will not be reflected when saved in this case.
     */
    void saveConfig();

    /**
     * Notify the DAO that the configuration it tracks has changed.
     * <p>
     * Call this method after updating the filesystem configuration directly or after providing a config extension.
     */
    void onConfigChanged();
}
