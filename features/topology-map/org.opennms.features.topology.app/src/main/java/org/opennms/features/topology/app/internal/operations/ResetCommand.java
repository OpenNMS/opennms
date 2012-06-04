package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;
import org.opennms.features.topology.app.internal.Constants;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;

import com.vaadin.ui.Window;

public class ResetCommand extends Command implements Constants{
    

    public ResetCommand(String caption, String menuLocation,
			String contextMenuLocation) {
		super(caption, menuLocation, contextMenuLocation);
		
	}

	@Override
    public boolean appliesToTarget(Object target, SimpleGraphContainer graphContainer) {
        return true;
    }

    @Override
    public void doCommand(Object target, SimpleGraphContainer graphContainer, Window mainWindow, CommandManager commandManager) {
    	
        graphContainer.resetContainer();
        graphContainer.addGroup(ROOT_GROUP_ID, GROUP_ICON);
        graphContainer.addVertex(CENTER_VERTEX_ID, 50, 50, SERVER_ICON);
        graphContainer.getVertexContainer().setParent(CENTER_VERTEX_ID, ROOT_GROUP_ID);
    }
}