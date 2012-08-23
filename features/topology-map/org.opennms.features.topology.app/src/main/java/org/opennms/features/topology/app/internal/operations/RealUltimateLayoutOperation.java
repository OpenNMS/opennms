package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.CheckedOperation;
import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.app.internal.jung.RealUltimateLayoutAlgorithm;


public class RealUltimateLayoutOperation implements CheckedOperation{

    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        DisplayState graphContainer = operationContext.getGraphContainer();
        
        graphContainer.setLayoutAlgorithm(new RealUltimateLayoutAlgorithm());
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

    @Override
    public boolean isChecked(List<Object> targets, OperationContext operationContext) {
        if(operationContext.getGraphContainer().getLayoutAlgorithm() instanceof RealUltimateLayoutAlgorithm) {
            return true;
        }
        return false;
    }
}