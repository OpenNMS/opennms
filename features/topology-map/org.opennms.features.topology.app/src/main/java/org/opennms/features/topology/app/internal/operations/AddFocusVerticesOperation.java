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

import java.util.List;

import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.VertexRef;

public class AddFocusVerticesOperation implements Constants, Operation {

	@Override
	public void execute(List<VertexRef> targets, final OperationContext operationContext) {
		if (targets == null || targets.isEmpty()) {
			return;
		}
		final GraphContainer graphContainer = operationContext.getGraphContainer();
		for (VertexRef target : targets) {
			graphContainer.addCriteria(new DefaultVertexHopCriteria(target));
		}
		graphContainer.redoLayout();
	}

	@Override
	public boolean display(List<VertexRef> targets, OperationContext operationContext) {
		return enabled(targets, operationContext);
	}

	@Override
	public boolean enabled(List<VertexRef> targets, OperationContext operationContext) {
		if (targets == null || targets.isEmpty()) return false;
		final GraphContainer graphContainer = operationContext.getGraphContainer();
        for (Criteria crit : graphContainer.getCriteria()) {
            if(crit instanceof VertexHopCriteria) {
                if (((VertexHopCriteria) crit).getVertices().containsAll(targets)) {
                    return false;
                }
            }
        }
		return true;
	}

	@Override
	public String getId() {
		return "AddFocusVertices";
	}
}
