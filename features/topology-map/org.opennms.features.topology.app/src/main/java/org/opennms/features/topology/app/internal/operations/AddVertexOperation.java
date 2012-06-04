package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.app.internal.Constants;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;

public class AddVertexOperation implements Operation{
    private String m_icon;
    public AddVertexOperation(String icon) {
        m_icon = icon;
    }
    
    @Override
    public boolean display(List<Object> targets,
            OperationContext operationContext) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean enabled(List<Object> targets,OperationContext operationContext) {
        if(targets.size() > 1) return false;
        
        Object itemId = targets.size() == 1 ? targets.get(0) : null;
        
        return itemId == null || operationContext.getGraphContainer().getVertexContainer().containsId(itemId);
    }

    @Override
    public String getId() {
        return null;
    }

    void connectNewVertex(String vertexId, String icon, SimpleGraphContainer graphContainer) {
        String vertexId1 = graphContainer.getNextVertexId();
        graphContainer.addVertex(vertexId1, 0, 0, icon);
        graphContainer.getVertexContainer().setParent(vertexId1, Constants.ROOT_GROUP_ID);
        
        //Right now we are connecting all new vertices to v0
        graphContainer.connectVertices(graphContainer.getNextEdgeId(), vertexId, vertexId1);
    }

    public String getIcon() {
        return m_icon;
    }

    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        Object vertexId = targets.isEmpty() ? null : targets.get(0);
        String icon = getIcon();
        if (vertexId == null) {
            if (operationContext.getGraphContainer().getVertexContainer().containsId(Constants.CENTER_VERTEX_ID)) {
            	connectNewVertex(Constants.CENTER_VERTEX_ID, Constants.SERVER_ICON, operationContext.getGraphContainer());
            }
            else {
            	operationContext.getGraphContainer().addVertex(Constants.CENTER_VERTEX_ID, 50, 50, Constants.SERVER_ICON);
            	operationContext.getGraphContainer().getVertexContainer().setParent(Constants.CENTER_VERTEX_ID, Constants.ROOT_GROUP_ID);
            }
        } else {
            
            connectNewVertex(vertexId.toString(), icon, operationContext.getGraphContainer());
        }
        operationContext.getGraphContainer().redoLayout();
        
        return null;
    }
    
}