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

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider;
import org.slf4j.LoggerFactory;

public class HideNodesWithoutLinksOperation implements CheckedOperation {

    LinkdTopologyProvider m_topologyProvider;
    public HideNodesWithoutLinksOperation(LinkdTopologyProvider topologyProvider) {
        m_topologyProvider=topologyProvider;
    }

    @Override
    public Undoer execute(List<VertexRef> targets,
            OperationContext operationContext) {
        log("executing Hide Nodes Without Link Checked Operation");
        log("found addNodeWithoutLinks: " + m_topologyProvider.isAddNodeWithoutLink());
        m_topologyProvider.setAddNodeWithoutLink(!m_topologyProvider.isAddNodeWithoutLink());
        log("switched addNodeWithoutLinks to: " + m_topologyProvider.isAddNodeWithoutLink());
        m_topologyProvider.load(null);
        if (operationContext != null && operationContext.getGraphContainer() != null) {
            log("operationcontext and GraphContainer not null: executing redoLayout");
            operationContext.getGraphContainer().redoLayout();
        }
        return null;
    }

    @Override
    public boolean display(List<VertexRef> targets,
            OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<VertexRef> targets,
            OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return "LinkdTopologyProviderHidesNodesWithoutLinksOperation";
    }

    @Override
    public boolean isChecked(List<VertexRef> targets,
            OperationContext operationContext) {
        return !m_topologyProvider.isAddNodeWithoutLink();
    }

    private void log(final String string) {
        LoggerFactory.getLogger(getClass()).debug("{}: {}", getId(), string);
    }
}
