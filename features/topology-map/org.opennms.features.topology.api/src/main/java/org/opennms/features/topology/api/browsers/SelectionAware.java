/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
