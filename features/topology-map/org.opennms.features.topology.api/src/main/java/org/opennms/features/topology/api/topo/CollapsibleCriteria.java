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
