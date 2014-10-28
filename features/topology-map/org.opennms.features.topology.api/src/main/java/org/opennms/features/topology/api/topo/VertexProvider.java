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

import java.util.Collection;
import java.util.List;

public interface VertexProvider {
	
	/**
	 * A string used to identify references belonging to this provider
	 * 
	 * May only contain characters that make for a reasonable Java identifier
	 * such as letters digits and underscore (no colons, periods, commas etc.)
	 * 
	 */
	public String getVertexNamespace();
	
	/**
	 * This boolean returns true if the vertices in this provider are intended
	 * to contribute to or overlay another namespace 

	 * @param namespace the namespace of a provider
	 * @return true if this provider contributes the the given namespace, false otherwise.  Should 
	 * return false when passing in its own namepace. A provider doesn't contribute to itself.
	 */
	public boolean contributesTo(String namespace);

	/**
	 * @deprecated Use {@link #containsVertexId(VertexRef id)} instead.
	 */
	boolean containsVertexId(String id);

	boolean containsVertexId(VertexRef id, Criteria... criteria);

	public Vertex getVertex(String namespace, String id);
	
	public Vertex getVertex(VertexRef reference, Criteria... criteria);
	
	public int getSemanticZoomLevel(VertexRef vertex);
	
	/**
	 * Return an immutable list of vertices that match the criteria.
	 */
	public List<Vertex> getVertices(Criteria... criteria);
	
	public List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria);
	
	public List<Vertex> getRootGroup();
	
	public boolean hasChildren(VertexRef group);
	
	public Vertex getParent(VertexRef vertex);
	
	boolean setParent(VertexRef child, VertexRef parent);
	
	public List<Vertex> getChildren(VertexRef group, Criteria... criteria);
	
	public void addVertexListener(VertexListener vertexListener);
	
	public void removeVertexListener(VertexListener vertexListener);

	void clearVertices();

    public int getVertexTotalCount();

}
