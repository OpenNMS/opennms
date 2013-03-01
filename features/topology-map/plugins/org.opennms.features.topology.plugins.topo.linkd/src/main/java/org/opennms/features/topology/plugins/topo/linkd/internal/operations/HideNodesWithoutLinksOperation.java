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

package org.opennms.features.topology.plugins.topo.linkd.internal.operations;

import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.AbstractCheckedOperation;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider;
import org.slf4j.LoggerFactory;

public class HideNodesWithoutLinksOperation extends AbstractCheckedOperation {

	private final LinkdTopologyProvider m_topologyProvider;

	public HideNodesWithoutLinksOperation(LinkdTopologyProvider topologyProvider) {
		m_topologyProvider = topologyProvider;
	}

	@Override
	public Undoer execute(List<VertexRef> targets, OperationContext operationContext) {
		if (enabled(targets, operationContext)) {
			execute(operationContext.getGraphContainer());
		}
		return null;
	}
	
	private void execute(GraphContainer container) {
		LoggerFactory.getLogger(this.getClass()).debug("switched addNodeWithoutLinks to: " + !m_topologyProvider.isAddNodeWithoutLink());
		m_topologyProvider.setAddNodeWithoutLink(!m_topologyProvider.isAddNodeWithoutLink());
		m_topologyProvider.refresh();
		container.redoLayout();
	}

	@Override
	public boolean display(List<VertexRef> targets, OperationContext operationContext) {
		return true;
	}

	/**
	 * This is kinda unreliable because we are just matching on namespace... but that's all we can do with
	 * the API as it is now.
	 */
	@Override
	protected boolean enabled(GraphContainer container) {
		GraphProvider activeGraphProvider = container.getBaseTopology();
		return m_topologyProvider.getVertexNamespace().equals(activeGraphProvider.getVertexNamespace());
	}

	@Override
	public String getId() {
		return "LinkdTopologyProviderHidesNodesWithoutLinksOperation";
	}

	@Override
	protected boolean isChecked(GraphContainer container) {
		if (enabled(container)) {
			return !m_topologyProvider.isAddNodeWithoutLink();
		} else {
			return false;
		}
	}

	@Override
	public void applyHistory(GraphContainer container, Map<String, String> settings) {
		if ("true".equals(settings.get(this.getClass().getName()))) {
			if (m_topologyProvider.isAddNodeWithoutLink()) {
				execute(container);
			} else {
				// Hiding is enabled and isAddNodeWithoutLink() is already false
			}
		} else {
			if (m_topologyProvider.isAddNodeWithoutLink()) {
				// Adding is enabled and isAddNodeWithoutLink() is already true
			} else {
				execute(container);
			}
		}
	}
}
