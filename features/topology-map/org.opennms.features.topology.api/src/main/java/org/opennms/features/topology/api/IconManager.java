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

import java.util.List;

import org.opennms.features.topology.api.topo.Vertex;

public interface IconManager {

    /**
     * Returns the first {@link IconRepository} which has the provided <code>iconKey</code> mapping defined.
     *
     * @param iconKey the <code>iconKey</code> to look up
     * @return the first {@link IconRepository} which has the provided <code>iconKey</code> mapping defined, or <code>null</code> if no {@link IconRepository} exists with the provided <code>iconKey</code>
     */
    IconRepository findRepositoryByIconKey(String iconKey);

    /**
     * Returns the list of available svg-files, e.g. 'theme://svg/file.svg'.
     *
     * @return the list of available svg-files, e.g. 'theme://svg/file.svg'
     */
    List<String> getSVGIconFiles();

    /**
     * Returns the icon id assigned to the provided <code>iconKey</code>.
     *
     * @param iconKey the <code>iconKey</code> to look up
     * @return the icon id assigned to the provided <code>iconKey</code>
     */
    String getSVGIconId(String iconKey);

    /**
     * Returns the icon id assigned to the provided {@link Vertex}.
     *
     * @param vertex the vertex to get the icon id for
     * @return the icon id assigned to the provided {@link Vertex}
     */
    String getSVGIconId(Vertex vertex);

    /**
     * Sets a new icon mapping from the {@link Vertex} to the <code>newIconId</code>.
     *
     * @param vertex the vertex to map
     * @param newIconId the icon id to map the vertex to
     * @return the icon key of the vertex if this {@link IconManager} was able to save the mapping, null otherwise
     */
    String setIconMapping(Vertex vertex, String newIconId);

    /**
     * Removes the icon mapping for the {@link Vertex} if defined.
     *
     * @param vertex the {@link Vertex} to remove the icon mapping for
     * @return <code>true</code> if the mapping was removed, <code>false</code> if no icon mapping was found for the provided {@link Vertex} and therefore could not be removed
     */
    boolean removeIconMapping(Vertex vertex);
}
