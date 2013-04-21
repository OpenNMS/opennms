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

package org.opennms.features.topology.app.internal.operations;

import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;

public abstract class LayoutOperation extends AbstractCheckedOperation {

	private final LayoutFactory m_factory;

	protected static interface LayoutFactory {
		LayoutAlgorithm getLayoutAlgorithm();
	}

	public LayoutOperation(LayoutFactory factory) {
		m_factory = factory;
	}

    @Override
    public final Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
        execute(operationContext.getGraphContainer());
        return null;
    }

    /**
     * Set the layout algorithm.
     */
    private void execute(GraphContainer container) {
        container.setLayoutAlgorithm(m_factory.getLayoutAlgorithm());
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    protected final boolean isChecked(GraphContainer container) {
        if(container.getLayoutAlgorithm().getClass().getName().equals(m_factory.getLayoutAlgorithm().getClass().getName())) {
            return true;
        }
        return false;
    }

	@Override
	public void applyHistory(GraphContainer container, Map<String, String> settings) {
		// If the setting for this operation is set to true, then set the layout algorithm
		if ("true".equals(settings.get(this.getClass().getName()))) {
			execute(container);
		}
	}
}
