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
 * Each {@link IconRepository} stores a mapping from icon keys to icon ids.
 * The icon key is Graph Provider specific and is defined by it (e.g. "sfree:group")
 * The icon id should match with an id element in all existing SVGs (not only this {@link IconRepository}.
 */
public interface IconRepository {

    /**
     * Verifies if a mapping for the provided icon Key is defined.
     * @param iconKey the icon key
     * @return true if a mapping is defined, false otherwise.
     */
    boolean contains(String iconKey);

    /**
     * Maps the provided <code>iconKey</code> to an SVG id element.
     * If no mapping is defined, <code>null</code> is returned.
     *
     * @param iconKey The icon key to look up
     * @return The icon id, or null if no mapping is defined.
     */
    String getSVGIconId(String iconKey);
}
