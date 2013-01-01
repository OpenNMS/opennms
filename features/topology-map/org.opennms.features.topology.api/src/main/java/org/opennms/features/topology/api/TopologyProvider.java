/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.api;

import java.util.Collection;

import com.vaadin.data.util.BeanContainer;


public interface TopologyProvider {

	/**
	 * Set the parent ID of a vertex. If the ID is null,
	 * then the vertex is detached from its parent.
	 */
    void setParent(Object vertexId, Object parentId);

    Object addGroup(String groupLabel, String groupIcon);

    boolean containsVertexId(Object vertexId);

    void save(String filename);

    void load(String filename);

    VertexContainer<?, ?> getVertexContainer();

    BeanContainer<?, ?> getEdgeContainer();

    Collection<?> getVertexIds();

    Collection<?> getEdgeIds();

    Collection<?> getEdgeIdsForVertex(Object vertexId);

    Collection<?> getEndPointIdsForEdge(Object edgeId);

	String getNamespace();

}
