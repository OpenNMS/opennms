package org.opennms.features.topology.plugins.topo.onmsdao.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.onmsdao.internal.OnmsTopologyProvider;

public class ConnectOperation implements Operation {

    OnmsTopologyProvider m_topologyProvider;
    
    public ConnectOperation(OnmsTopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }
    
    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        
        List<Object> endPoints = targets;
        
        m_topologyProvider.connectVertices((String)endPoints.get(0), (String)endPoints.get(1));
        return null;
    }

    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return false;
    }

    @Override
    public boolean enabled(List<Object> targets, OperationContext operationContext) {
        return targets.size() == 2;
    }

    @Override
    public String getId() {
        return null;
    }
}