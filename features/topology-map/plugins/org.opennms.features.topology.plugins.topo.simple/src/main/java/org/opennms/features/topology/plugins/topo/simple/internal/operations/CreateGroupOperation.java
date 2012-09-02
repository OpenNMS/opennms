package org.opennms.features.topology.plugins.topo.simple.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.TopologyProvider;


public class CreateGroupOperation implements Constants, Operation{
    
    
	TopologyProvider m_topologyProvider;
    
    public CreateGroupOperation(TopologyProvider topologyProvider) {
        m_topologyProvider = topologyProvider;
    }
    
    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        
        GraphContainer graphContainer = operationContext.getGraphContainer();
        
        Object groupId = m_topologyProvider.addGroup(GROUP_ICON_KEY);
        
        
//        for(Object itemId : targets) {
//            m_topologyProvider.setParent(itemId, groupId);
//        }
        
        
        Object parentGroup = null;
        for(Object key : targets) {
            Object vertexId = graphContainer.getVertexItemIdForVertexKey(key);
            Object parent = m_topologyProvider.getVertexContainer().getParent(vertexId);
            if (parentGroup == null) {
            	parentGroup = parent;
            } else if (parentGroup != parent) {
            	parentGroup = ROOT_GROUP_ID;
            }
            m_topologyProvider.setParent(vertexId, groupId);
        }

        
        m_topologyProvider.setParent(groupId, parentGroup == null ? ROOT_GROUP_ID : parentGroup);
        
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