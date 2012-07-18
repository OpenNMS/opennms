package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;


public class RedoLayoutOperation implements Operation {

	@Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        DisplayState graphContainer = operationContext.getGraphContainer();
        
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
        return targets == null || targets.isEmpty();
    }

    @Override
    public String getId() {
        return null;
    }
}