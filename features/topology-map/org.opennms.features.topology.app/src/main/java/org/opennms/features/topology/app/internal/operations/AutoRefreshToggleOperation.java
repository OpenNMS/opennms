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
