package org.opennms.features.topology.plugins.topo.onmsdao.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.onmsdao.internal.OnmsTopologyProvider;


public class ResetOperation implements Constants, Operation{
    
    OnmsTopologyProvider m_topologyProvider;
    
    public ResetOperation(OnmsTopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }

    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        
        m_topologyProvider.resetContainer();
        Object groupId = m_topologyProvider.addGroup(GROUP_ICON);
        Object vertexId = m_topologyProvider.addVertex(-1,50, 50, SERVER_ICON);
        m_topologyProvider.setParent(vertexId, groupId);
        return null;
    }

    @Override
    public boolean display(List<Object> targets,
            OperationContext operationContext) {
        return false;
    }

    @Override
    public boolean enabled(List<Object> targets,
            OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return null;
    }
}