package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.app.internal.Constants;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;


public class CreateGroupOperation implements Constants, Operation{
    

    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        SimpleGraphContainer graphContainer = operationContext.getGraphContainer();
        
        String groupId = graphContainer.getNextGroupId();
        graphContainer.addGroup(groupId, GROUP_ICON);
        graphContainer.getVertexContainer().setParent(groupId, ROOT_GROUP_ID);
        
        for(Object itemId : graphContainer.getSelectedVertexIds()) {
        	graphContainer.getVertexContainer().setParent(itemId, groupId);
        }
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
        return operationContext.getGraphContainer().getSelectedVertexIds().size() > 0;
    }

    @Override
    public String getId() {
        return null;
    }
}