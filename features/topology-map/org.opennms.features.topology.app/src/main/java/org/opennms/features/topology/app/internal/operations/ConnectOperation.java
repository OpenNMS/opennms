package org.opennms.features.topology.app.internal.operations;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.app.internal.SimpleGraphContainer;

public class ConnectOperation implements Operation {


    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        SimpleGraphContainer graphContainer = operationContext.getGraphContainer();
        
        List<Object> endPoints = new ArrayList<Object>(graphContainer.getSelectedVertexIds());
        
        graphContainer.connectVertices(graphContainer.getNextEdgeId(), (String)endPoints.get(0), (String)endPoints.get(1));
        return null;
    }

    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return false;
    }

    @Override
    public boolean enabled(List<Object> targets, OperationContext operationContext) {
        return operationContext.getGraphContainer().getSelectedVertexIds().size() == 2;
    }

    @Override
    public String getId() {
        return null;
    }
}