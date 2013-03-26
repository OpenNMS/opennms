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

package org.opennms.features.topology.api.topo;

import java.util.Collection;
import java.util.List;

public interface EdgeProvider {

	/**
	 * A string used to identify references belonging to this provider
	 * 
	 * May only container characters that make for a reasonable java identifier
	 * such as letters digits and underscore (no colons, periods, commans etc.)
	 * 
	 */
	public String getEdgeNamespace();
	
	/**
	 * This boolean returns true if the edges in this provider are intended
	 * to contribute to or overlay another namespace 

	 * @param namespace the namespace of a provider
	 * @return true if this provider contributes the the given namespace, false other.  Should 
	 * return false for passing its own namepace. A provider doesn't contribute to itself    
	 */
	public boolean contributesTo(String namespace);

	public Edge getEdge(String namespace, String id);
	
	public Edge getEdge(EdgeRef reference);
	
	public boolean matches(EdgeRef edgeRef, Criteria criteria);
	
	/**
	 * Return an immutable list of edges that match the criteria.
	 */
	public List<Edge> getEdges(Criteria criteria);
	
	/**
	 * Return an immutable list of all edges.
	 */
	public List<Edge> getEdges();
	
	/**
	 * Return an immutable list of all edges that match this set of references.
	 */
	public List<Edge> getEdges(Collection<? extends EdgeRef> references);
	
	public void addEdgeListener(EdgeListener listener);
	
	public void removeEdgeListener(EdgeListener listener);

	void clearEdges();

}
