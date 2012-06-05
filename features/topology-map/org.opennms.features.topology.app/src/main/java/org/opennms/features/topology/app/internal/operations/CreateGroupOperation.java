package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.app.internal.Constants;
import org.opennms.features.topology.app.internal.topr.SimpleTopologyProvider;


public class CreateGroupOperation implements Constants, Operation{
    
    
    TopologyProvider m_topologyProvider = new SimpleTopologyProvider();
    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        
        Object groupId = m_topologyProvider.addGroup(GROUP_ICON);
        
        m_topologyProvider.setParent(groupId, ROOT_GROUP_ID);
        
        for(Object itemId : targets) {
            m_topologyProvider.setParent(itemId, groupId);
        }
        return null;
    }

    @Override
    public boolean display(List<Object> targets,
            OperationContext operationContext) {
        return false;
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