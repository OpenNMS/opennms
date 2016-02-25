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

package org.opennms.features.topology.api;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;

// This class is abstract only because the history is applied based on the class name
public abstract class AbstractEdgeStatusToggleOperation extends AbstractCheckedOperation {

    private EdgeStatusProvider m_edgeStatusProvider;

    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        GraphContainer container = operationContext.getGraphContainer();

        // if already selected, deselect
        if (container.getEdgeStatusProvider() == m_edgeStatusProvider) {
            container.setEdgeStatusProvider(null);
        } else { // otherwise select
            container.setEdgeStatusProvider(m_edgeStatusProvider);
        }
        container.redoLayout();
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    protected boolean isChecked(GraphContainer container) {
        return container.getEdgeStatusProvider() != null && container.getEdgeStatusProvider() == m_edgeStatusProvider;
    }

    @Override
    public Map<String, String> createHistory(GraphContainer container){
        return Collections.singletonMap(getClass().getName(), Boolean.toString(isChecked(container)));
    }

    @Override
    public void applyHistory(GraphContainer container, Map<String, String> settings) {
        String historyValue = settings.get(getClass().getName());
        boolean statusEnabled = Boolean.TRUE.toString().equals(historyValue);
        if (statusEnabled) {
            container.setEdgeStatusProvider(m_edgeStatusProvider);
        } else {
            container.setEdgeStatusProvider(null);
        }
    }

    public void setEdgeStatusProvider(EdgeStatusProvider edgeStatusProvider) {
        m_edgeStatusProvider = edgeStatusProvider;
    }

    private void toggle(GraphContainer container) {
        if (container.getEdgeStatusProvider() == null) {
            container.setEdgeStatusProvider(m_edgeStatusProvider);
        } else {
            container.setEdgeStatusProvider(null);
        }
        container.redoLayout();
    }
}
