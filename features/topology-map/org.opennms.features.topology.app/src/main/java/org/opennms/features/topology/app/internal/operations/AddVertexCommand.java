package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;
import org.opennms.features.topology.app.internal.Constants;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;

import com.vaadin.ui.Window;

public class AddVertexCommand extends Command implements Constants {

    private String m_icon;

    public AddVertexCommand(String caption, String menuLocation, String contextMenuLocation, String icon) {
        super(caption, menuLocation, contextMenuLocation);
        m_icon = icon;
    }

    @Override
    public boolean appliesToTarget(Object itemId, SimpleGraphContainer graphContainer) {
        return itemId == null || graphContainer.getVertexContainer().containsId(itemId);
    }

    @Override
    public void doCommand(Object vertexId, SimpleGraphContainer graphContainer, Window mainWindow, CommandManager commandManager) {
        if (vertexId == null) {
            if (graphContainer.getVertexContainer().containsId(CENTER_VERTEX_ID)) {
            	connectNewVertex(CENTER_VERTEX_ID, SERVER_ICON, graphContainer);
            }
            else {
            	graphContainer.addVertex(CENTER_VERTEX_ID, 50, 50, SERVER_ICON);
            	graphContainer.getVertexContainer().setParent(CENTER_VERTEX_ID, ROOT_GROUP_ID);
            }
        } else {
            connectNewVertex(vertexId.toString(), m_icon, graphContainer);
        }
        graphContainer.redoLayout();
    }
    
    private void connectNewVertex(String vertexId, String icon, SimpleGraphContainer graphContainer) {
        String vertexId1 = graphContainer.getNextVertexId();
        graphContainer.addVertex(vertexId1, 0, 0, icon);
        graphContainer.getVertexContainer().setParent(vertexId1, ROOT_GROUP_ID);
        
        //Right now we are connecting all new vertices to v0
        graphContainer.connectVertices(graphContainer.getNextEdgeId(), vertexId, vertexId1);
    }
}