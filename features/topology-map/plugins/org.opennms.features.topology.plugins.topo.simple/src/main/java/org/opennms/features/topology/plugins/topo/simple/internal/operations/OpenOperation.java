package org.opennms.features.topology.plugins.topo.simple.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.simple.internal.SimpleTopologyProvider;


public class OpenOperation implements Operation {
    
    SimpleTopologyProvider m_topologyProvider;
    
    public OpenOperation(SimpleTopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }
    
	@Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        
        m_topologyProvider.load("graph.xml");
        //graphContainer.load("graph.xml");
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