package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.app.internal.ManualLayoutAlgorithm;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;


public class ManualLayoutOperation implements Operation {

    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        SimpleGraphContainer graphContainer = operationContext.getGraphContainer();
        
        graphContainer.setLayoutAlgorithm(new ManualLayoutAlgorithm());
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