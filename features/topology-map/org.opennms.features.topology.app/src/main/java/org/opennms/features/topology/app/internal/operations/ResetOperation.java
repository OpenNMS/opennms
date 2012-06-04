package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.app.internal.Constants;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;


public class ResetOperation implements Constants, Operation{
    

    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        SimpleGraphContainer graphContainer = operationContext.getGraphContainer();
        
        graphContainer.resetContainer();
        graphContainer.addGroup(ROOT_GROUP_ID, GROUP_ICON);
        graphContainer.addVertex(CENTER_VERTEX_ID, 50, 50, SERVER_ICON);
        graphContainer.getVertexContainer().setParent(CENTER_VERTEX_ID, ROOT_GROUP_ID);
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