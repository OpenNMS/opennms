package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;

import com.vaadin.data.Item;
import com.vaadin.ui.Window;

class LockCommand extends Command {
    

    public LockCommand(String caption, String menuLocation,
			String contextMenuLocation) {
		super(caption, menuLocation, contextMenuLocation);
	}

	@Override
    public boolean appliesToTarget(Object itemId, SimpleGraphContainer graphContainer) {
    	if (graphContainer.getVertexContainer().containsId(itemId)) {
    		Item v = graphContainer.getVertexContainer().getItem(itemId);
    		return !(Boolean)v.getItemProperty("locked").getValue();
    	}
    	return false;
    }

    @Override
    public void doCommand(Object vertexId, SimpleGraphContainer graphContainer, Window mainWindow, CommandManager commandManager) {
    	Item v = graphContainer.getVertexContainer().getItem(vertexId);
    	v.getItemProperty("locked").setValue(true);
    }
}