package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.app.internal.SimpleGraphContainer;


public class RemoveVertexOperation implements Operation {

    @Override
    public Undoer execute(List<Object> targets,OperationContext operationContext) {
        SimpleGraphContainer graphContainer = operationContext.getGraphContainer();
        
        if (targets == null) {
        	System.err.println("need to handle selection!!!");
        } else {
        	graphContainer.removeVertex(targets.toString());
        	graphContainer.redoLayout();
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
        return targets == null || operationContext.getGraphContainer().getVertexContainer().containsId(targets);
    }

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }
}