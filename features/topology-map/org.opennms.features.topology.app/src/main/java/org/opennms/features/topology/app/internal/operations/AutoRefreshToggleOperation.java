/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.operations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;

public class AutoRefreshToggleOperation extends AbstractCheckedOperation {

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return operationContext.getGraphContainer().hasAutoRefreshSupport();
    }

    @Override
    protected boolean enabled(GraphContainer container) {
        return true;
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    protected boolean isChecked(GraphContainer container) {
        if (container.hasAutoRefreshSupport()) {
            return container.getAutoRefreshSupport().isEnabled();
        }
        return false;
    }

    @Override
    public void execute(final List<VertexRef> targets, final OperationContext operationContext) {
       toggle(operationContext.getGraphContainer());
    }

    @Override
    public Map<String, String> createHistory(GraphContainer container){
        return Collections.singletonMap(getClass().getName(), Boolean.toString(isChecked(container)));
    }

    @Override
    public void applyHistory(GraphContainer container, Map<String, String> settings) {
        if (container.hasAutoRefreshSupport()) {
            boolean autoRefreshEnabled = Boolean.TRUE.toString().equals(settings.get(getClass().getName()));
            container.getAutoRefreshSupport().setEnabled(autoRefreshEnabled);
        }
    }

    private static void toggle(final GraphContainer container) {
        if (container.hasAutoRefreshSupport()) {
            container.getAutoRefreshSupport().toggle();
        }
    }
}
