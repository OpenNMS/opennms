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
package org.opennms.features.topology.api.browsers;

import static org.opennms.features.topology.api.browsers.SelectionChangedListener.*;

import java.util.List;

import org.opennms.features.topology.api.topo.VertexRef;

/**
 * Interface marking if a {@link org.opennms.features.topology.api.topo.GraphProvider} is "selection aware".
 * This allows {@link org.opennms.features.topology.api.topo.GraphProvider}s to filter the browser tables
 * (e.g. alarm, node, etc) based on the current selection.
 *
 * In order to achieve that all selected Vertices must be converted to a list of Restrictions.
 * The list of Restriction is represented by {@link Selection}.
 *
 */
public interface SelectionAware {

    /**
     * Converts the provided <code>selectedVertices</code> to a Selection.
     * The provided <code>type</code> represents the according browser table.
     * This method is only invoked if {@link #contributesTo(ContentType)} returns to for the provided <code>type</code>
     *
     * @param selectedVertices The vertices currently selected in the Topology UI.
     * @param type The type to filter for. Represents the according browser table.
     * @return The selection containing the List of Restrictions. Must NOT be null.
     *
     * @see Selection
     */
    Selection getSelection(List<VertexRef> selectedVertices, ContentType type);

    /**
     * Allows the {@link org.opennms.features.topology.api.topo.GraphProvider} to define if it
     * contributes to a certain {@link ContentType}.
     * If <code>false</code> it is not shown in the browsers tab at all
     *
     * @param type The type to check if <code>this</code> contribute to
     * @return true if <code>this</code> contributes to the provided <code>type</code>
     */
    boolean contributesTo(ContentType type);

}
