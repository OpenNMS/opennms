package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.app.internal.SimpleGraphContainer;


public class RedoLayoutOperation implements Operation {

	@Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        SimpleGraphContainer graphContainer = operationContext.getGraphContainer();
        
        graphContainer.redoLayout();
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
        //Applies to background as a whole
        return targets == null;
    }

    @Override
    public String getId() {
        return null;
    }
}