package org.opennms.features.topology.plugins.topo.simple.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.EditableTopologyProvider;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;


public class ResetOperation implements Constants, Operation{
    
    EditableTopologyProvider m_topologyProvider;
    
    public ResetOperation(EditableTopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }

    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        
        m_topologyProvider.resetContainer();
        Object groupId = m_topologyProvider.addGroup(GROUP_ICON_KEY);
        Object vertexId = m_topologyProvider.addVertex(50, 50);
        m_topologyProvider.setParent(vertexId, groupId);
        return null;
    }

    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<Object> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return null;
    }
}