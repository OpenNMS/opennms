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
package org.opennms.netmgt.dao.api;

import java.util.List;

import org.opennms.netmgt.model.AdhocGraphType;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.PrefabGraphType;

/**
 * <p>GraphDao interface.</p>
 */
public interface GraphDao {
    /**
     * <p>findByName</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PrefabGraphType} object.
     */
    public PrefabGraphType findPrefabGraphTypeByName(String name);

    /**
     * <p>findAdhocByName</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.AdhocGraphType} object.
     */
    public AdhocGraphType findAdhocGraphTypeByName(String name);
    
    /**
     * <p>getAllPrefabGraphs</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<PrefabGraph> getAllPrefabGraphs();

    /**
     * <p>getPrefabGraph</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.PrefabGraph} object.
     */
    public PrefabGraph getPrefabGraph(String name);
    
    /**
     * <p>getPrefabGraphsForResource</p>
     *
     * @param resource a {@link org.opennms.netmgt.model.OnmsResource} object.
     * @return an array of {@link org.opennms.netmgt.model.PrefabGraph} objects.
     */
    public PrefabGraph[] getPrefabGraphsForResource(OnmsResource resource);
}
