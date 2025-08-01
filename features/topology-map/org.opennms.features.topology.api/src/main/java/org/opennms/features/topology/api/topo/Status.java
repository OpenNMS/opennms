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
package org.opennms.features.topology.api.topo;

import java.util.Map;

public interface Status {

    String computeStatus();
    Map<String, String> getStatusProperties();

    /**
     * Defines a CSS-Property to Value mapping, where the key defines the CSS-Property and the value the according value.
     * Each entry is applied to the GWTEdge style attribute.
     * If no style should be applied in addition, return an empty map.
     *
     * @return CSS-Property to Value-Mapping for each GWTEdge. Must NOT be null
     */
    Map<String, String> getStyleProperties();

}
