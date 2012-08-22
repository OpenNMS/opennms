package org.opennms.features.topology.plugins.topo.simple.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.simple.internal.SimpleTopologyProvider;

public class ConnectOperation implements Operation {

    SimpleTopologyProvider m_topologyProvider;
    
    public ConnectOperation(SimpleTopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }
    
    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        if(targets != null && targets.size() > 1) {
            Object sourceVertexId = operationContext.getGraphContainer().getVertexItemIdForVertexKey(targets.get(0));//(String)endPoints.get(0);
            Object targetVertextId = operationContext.getGraphContainer().getVertexItemIdForVertexKey(targets.get(1)); //(String)endPoints.get(1);
            m_topologyProvider.connectVertices(sourceVertexId, targetVertextId);
        }
        return null;
    }

    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return true;
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