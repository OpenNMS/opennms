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

import java.util.List;

import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.LoggerFactory;

public abstract class AbstractOperation implements Operation {

    @Override
    public boolean display(final List<VertexRef> targets, final OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(final List<VertexRef> targets, final OperationContext operationContext) {
        if (targets == null || targets.size() < 2) {
            for (final VertexRef target : targets) {
                final Integer nodeValue = getNodeIdValue(operationContext, target);
                if (nodeValue != null && nodeValue > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    protected static String getLabelValue(final OperationContext operationContext, final VertexRef target) {
        Vertex vertex = getVertexItem(operationContext, target);
        return vertex == null ? null : vertex.getLabel();
    }

    protected static String getIpAddrValue(final OperationContext operationContext, final VertexRef target) {
        Vertex vertex = getVertexItem(operationContext, target);
        return vertex == null ? null : vertex.getIpAddress();
    }

    protected static Integer getNodeIdValue(final OperationContext operationContext, final VertexRef target) {
        Vertex vertex = getVertexItem(operationContext, target);
        return vertex == null ? null : vertex.getNodeID();
    }

	protected static Vertex getVertexItem(final OperationContext operationContext, final VertexRef target) {
		Vertex vertex = operationContext.getGraphContainer().getBaseTopology().getVertex(target);
		if (vertex == null) {
			LoggerFactory.getLogger(AbstractOperation.class).debug("Null vertex found for vertex reference: {}:{}", target.getNamespace(), target.getId());
			return null;
		} else {
			return vertex;
		}
	}
}
