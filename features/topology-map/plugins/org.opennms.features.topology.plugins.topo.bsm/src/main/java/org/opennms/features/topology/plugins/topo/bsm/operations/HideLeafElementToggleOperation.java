/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
