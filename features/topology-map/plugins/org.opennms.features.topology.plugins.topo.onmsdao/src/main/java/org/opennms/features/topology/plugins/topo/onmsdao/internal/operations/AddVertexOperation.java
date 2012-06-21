package org.opennms.features.topology.plugins.topo.onmsdao.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.DisplayState;
import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.onmsdao.internal.OnmsTopologyProvider;

public class AddVertexOperation implements Operation{
    
    private OnmsTopologyProvider m_topologyProvider;
    
    private String m_icon;
    public AddVertexOperation(String icon, OnmsTopologyProvider topologyProvider) {
        m_icon = icon;
        m_topologyProvider = topologyProvider;
    }
    
    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
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

    void connectNewVertex(String vertexId, String icon, DisplayState graphContainer) {
        Object vertId1 = m_topologyProvider.addVertex(-1, 0, 0, icon);
        m_topologyProvider.setParent(vertId1, Constants.ROOT_GROUP_ID);
        m_topologyProvider.connectVertices(vertexId, vertId1);
        
    }

    public String getIcon() {
        return m_icon;
    }

    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        System.err.println("/*** Executing add Vertex in AddVertexOperation ***/");
        Object vertexId = targets.isEmpty() ? null : targets.get(0);
        String icon = getIcon();
        if (vertexId == null) {
            if (operationContext.getGraphContainer().getVertexContainer().containsId(Constants.CENTER_VERTEX_ID)) {
            	connectNewVertex(Constants.CENTER_VERTEX_ID, Constants.SERVER_ICON, operationContext.getGraphContainer());
            }
            else {
                Object vertId = m_topologyProvider.addVertex(-1,50, 50, Constants.SERVER_ICON);
                m_topologyProvider.setParent(vertId, Constants.ROOT_GROUP_ID);
                
            }
        } else {
            
            connectNewVertex(vertexId.toString(), icon, operationContext.getGraphContainer());
        }
        operationContext.getGraphContainer().redoLayout();
        
        return null;
    }
    
}