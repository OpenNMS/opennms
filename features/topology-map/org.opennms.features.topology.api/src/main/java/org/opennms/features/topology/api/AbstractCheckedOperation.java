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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.topo.VertexRef;

public abstract class AbstractCheckedOperation implements CheckedOperation {

	/**
	 * Return true by default.
	 */
	protected boolean enabled(GraphContainer container) {
		return true;
	}

	protected boolean isChecked(GraphContainer container) {
		return false;
	}

	/**
	 * By default, call {@link #enabled(OperationContext)}
	 */
	@Override
	public boolean enabled(List<VertexRef> vertices, OperationContext context) {
		return enabled(context.getGraphContainer());
	}

	/**
	 * By default, call {@link #isChecked(OperationContext)}
	 */
	@Override
	public boolean isChecked(List<VertexRef> vertices, OperationContext context) {
		return isChecked(context.getGraphContainer());
	}

	/**
	 * By default, save the state based on the checked status of the operation,
	 * independent of any currently-selected vertices.
	 */
	@Override
	public Map<String, String> createHistory(GraphContainer container) {
		return Collections.singletonMap(this.getClass().getName(), Boolean.toString(isChecked(container)));
	}
}
