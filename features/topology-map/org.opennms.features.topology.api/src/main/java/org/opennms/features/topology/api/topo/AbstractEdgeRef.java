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


public class AbstractEdgeRef extends AbstractRef implements EdgeRef {

	public AbstractEdgeRef(EdgeRef ref) {
		super(ref);
	}

	public AbstractEdgeRef(String namespace, String id, String label) {
		super(namespace, id, label);
	}

	/**
	 * @deprecated Specify a useful label for the object
	 */
	public AbstractEdgeRef(String namespace, String id) {
		super(namespace, id, namespace + ":" + id);
	}

	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof EdgeRef) {
			return super.equals(obj);
		}
		return false;
	}

	@Override
	public String toString() { return "EdgeRef:"+getNamespace()+":"+getId(); } 
}
