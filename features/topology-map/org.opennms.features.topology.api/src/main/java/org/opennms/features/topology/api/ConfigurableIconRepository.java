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
package org.opennms.features.topology.api;

/**
 * An {@link IconRepository} which can be updated.
 */
public interface ConfigurableIconRepository extends IconRepository {

    /**
     * Adds a custom icon mapping. The <code>iconKey</code> must be unique in this {@link IconRepository}.
     *
     * @param iconKey The icon key
     * @param iconId The icon id for the icon key
     */
    void addIconMapping(String iconKey, String iconId);

    /**
     * Removes the given <code>iconKey</code> from this {@link IconRepository}.
     *
     * @param iconKey The icon key to remove
     */
    void removeIconMapping(String iconKey);

    /**
     * Persists this {@link IconRepository}.
     * Should be invoked if changes to this {@link IconRepository} should be persisted permanently (e.g. on disk)
     */
    void save();
}
