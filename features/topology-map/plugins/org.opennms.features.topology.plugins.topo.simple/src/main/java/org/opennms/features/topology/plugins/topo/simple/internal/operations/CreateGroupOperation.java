package org.opennms.features.topology.plugins.topo.simple.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.simple.internal.SimpleTopologyProvider;


public class CreateGroupOperation implements Constants, Operation{
    
    
    SimpleTopologyProvider m_topologyProvider;
    
    public CreateGroupOperation(SimpleTopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }
    
    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        
        GraphContainer graphContainer = operationContext.getGraphContainer();
        
        Object groupId = m_topologyProvider.addGroup(GROUP_ICON);
        
        m_topologyProvider.setParent(groupId, ROOT_GROUP_ID);
        
//        for(Object itemId : targets) {
//            m_topologyProvider.setParent(itemId, groupId);
//        }
        
        for(Object key : targets) {
            Object vertexId = graphContainer.getVertexItemIdForVertexKey(key);
            m_topologyProvider.setParent(vertexId, groupId);
        }
        return null;
    }

    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<Object> targets, OperationContext operationContext) {
        return targets.size() > 0;
    }

    @Override
    public String getId() {
        return null;
    }
}