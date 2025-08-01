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
    public final void execute(List<VertexRef> targets, OperationContext operationContext) {
        execute(operationContext.getGraphContainer());
    }

    /**
     * Set the layout algorithm.
     */
    private void execute(GraphContainer container) {
        container.setLayoutAlgorithm(m_factory.getLayoutAlgorithm());
        container.redoLayout();
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

    public LayoutAlgorithm getLayoutAlgorithm() {
        return m_factory.getLayoutAlgorithm();
    }
}
