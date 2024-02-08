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
 * A container managing a bean which can reloaded.
 *
 * @param <T> bean type
 */
public interface ReloadingContainer<T> {

    /**
     * Retrieve the object held by the container.
     *
     * May also trigger a reload if the check interval has passed.
     *
     * @return the object
     */
    T getObject();

    /**
     * Immediately reload the object.
     *
     * The next call to {@link #getObject()} will return the updated object.
     */
    void reload();

    /**
     * Set the frequency at which the object should be checked for updates.
     *
     * The check is performed when calls to {@link #getObject()} are made once the interval has passed.
     *
     * @param reloadCheckInterval interval in ms, if {@code null} the default value will be used, if <= 0 reload checks will be disabled
     */
    void setReloadCheckInterval(Long reloadCheckInterval);

    /**
     * Retrieve the time at which the object was last updated.
     *
     * @return timestamp in ms
     */
    Long getLastUpdate();

}
