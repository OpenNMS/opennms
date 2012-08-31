package org.opennms.features.topology.plugins.topo.linkd.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider;

public class HideNodesWithoutLinksOperation implements CheckedOperation {

    LinkdTopologyProvider m_linkdTopologyProvider;
    public HideNodesWithoutLinksOperation(LinkdTopologyProvider topologyProveder) {
        m_linkdTopologyProvider=topologyProveder;
    }

    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        m_linkdTopologyProvider.setAddNodeWithoutLink(!m_linkdTopologyProvider.isAddNodeWithoutLink());
        return null;
    }

    @Override
    public boolean display(List<Object> targets,
            OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<Object> targets,
            OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return "LinkdHidesNodesWithoutLinks";
    }

    @Override
    public boolean isChecked(List<Object> targets,
            OperationContext operationContext) {
        return !m_linkdTopologyProvider.isAddNodeWithoutLink();
    }

}
