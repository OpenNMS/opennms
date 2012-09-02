package org.opennms.features.topology.plugins.topo.onmsdao.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.onmsdao.internal.OnmsTopologyProvider;


public class RemoveVertexOperation implements Operation {

    OnmsTopologyProvider m_topologyProvider;
    
    public RemoveVertexOperation(OnmsTopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }
    
    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        DisplayState graphContainer = operationContext.getGraphContainer();
        
        if (targets == null) {
        	System.err.println("need to handle selection!!!");
        } else {
            for(Object target : targets) {
                m_topologyProvider.removeVertex(target);
            }
            
            
        	graphContainer.redoLayout();
        }
        return null;
    }

    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return false;
    }

    @Override
    public boolean enabled(List<Object> targets, OperationContext operationContext) {
        if(targets != null) {
            for(Object target : targets) {
                if(!m_topologyProvider.containsVertexId(target)) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }
}