/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

import java.util.Set;

/**
 * This interface indicates that a criteria can be used to "collapse" elements of the graph.
 * When collapsed, all of the elements of the graph will be replaced with a single vertex that
 * represents all of the members. Edges that point to any member will be attached to the single
 * vertex.
 */
public interface CollapsibleCriteria {

	String getId();

	/**
	 * Check to see whether the Criteria is collapsed or not.
	 */
	boolean isCollapsed();

	/**
	 * Set the collapsed state of the criteria.
	 * @param collapsed Whether the criteria should be collapsed or not.
	 */
	void setCollapsed(boolean collapsed);

	/**
	 * Fetch the list of child vertices that should be collapsed
	 */
	Set<VertexRef> getVertices();

	/**
	 * This function returns the vertex that will be used to represent the group when the state is
	 * set to collapsed.
	 */
	Vertex getCollapsedRepresentation();

	String getLabel();

	String getNamespace();
}
