package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.app.internal.Constants;
import org.opennms.features.topology.app.internal.topr.SimpleTopologyProvider;


public class ResetOperation implements Constants, Operation{
    
    SimpleTopologyProvider m_topologyProvider = new SimpleTopologyProvider();
    
    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        DisplayState graphContainer = operationContext.getGraphContainer();
        
        m_topologyProvider.resetContainer();
        Object groupId = m_topologyProvider.addGroup(GROUP_ICON);
        Object vertexId = m_topologyProvider.addVertex(50, 50, SERVER_ICON);
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