/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.bsm.operations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServicesHideLeafsCriteria;
import org.opennms.features.topology.plugins.topo.bsm.BusinessServicesTopologyProvider;

public class HideLeafElementToggleOperation extends AbstractCheckedOperation {


    @Override
    public void execute(List<VertexRef> targets, OperationContext operationContext) {
        toggle(operationContext.getGraphContainer());
    }

    @Override
    protected boolean enabled(GraphContainer container) {
        return BusinessServicesTopologyProvider.TOPOLOGY_NAMESPACE.equals(container.getTopologyServiceClient().getNamespace());
    }

    @Override
    public boolean display(List<VertexRef> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return getClass().getCanonicalName();
    }

    @Override
    protected boolean isChecked(GraphContainer container) {
        return findShowLeafCriteria(container) != null;
    }

    @Override
    public Map<String, String> createHistory(GraphContainer container) {
        return Collections.singletonMap(getClass().getName(), Boolean.toString(isChecked(container)));
    }

    @Override
    public void applyHistory(GraphContainer container, Map<String, String> settings) {
        // If the setting for this operation is set to true, it was enabled before
        if ("true".equals(settings.get(this.getClass().getName()))) {
            toggle(container);
        }
    }

    private BusinessServicesHideLeafsCriteria findShowLeafCriteria(GraphContainer container) {
        return Criteria.getSingleCriteriaForGraphContainer(container, BusinessServicesHideLeafsCriteria.class, false);
    }

    private void toggle(GraphContainer container) {
        BusinessServicesHideLeafsCriteria showLeafCriteria = findShowLeafCriteria(container);
        if (showLeafCriteria == null) {
            container.addCriteria(new BusinessServicesHideLeafsCriteria());
        } else {
            container.removeCriteria(showLeafCriteria);
        }
        container.redoLayout();

    }
}
