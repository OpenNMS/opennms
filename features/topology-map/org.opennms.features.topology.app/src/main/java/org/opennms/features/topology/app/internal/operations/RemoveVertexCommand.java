package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;

import com.vaadin.ui.Window;

public class RemoveVertexCommand extends Command {

    public RemoveVertexCommand(String caption, String menuLocation,
			String contextMenuLocation) {
		super(caption, menuLocation, contextMenuLocation);
	}

	@Override
    public boolean appliesToTarget(Object itemId, SimpleGraphContainer graphContainer) {
    	return itemId == null || graphContainer.getVertexContainer().containsId(itemId);
    }

    @Override
    public void doCommand(Object vertexId, SimpleGraphContainer graphContainer, Window mainWindow, CommandManager commandManager) {
    	if (vertexId == null) {
    		System.err.println("need to handle selection!!!");
    	} else {
    		graphContainer.removeVertex(vertexId.toString());
    		graphContainer.redoLayout();
    	}
    }
}