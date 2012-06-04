package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;
import org.opennms.features.topology.app.internal.Constants;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;

import com.vaadin.ui.Window;

public class CreateGroupCommand extends Command implements Constants{
    

    public CreateGroupCommand(String caption, String menuLocation,
			String contextMenuLocation) {
		super(caption, menuLocation, contextMenuLocation);
	}

	@Override
    public boolean appliesToTarget(Object itemId, SimpleGraphContainer graphContainer) {
    	return graphContainer.getSelectedVertexIds().size() > 0;
    }

    @Override
    public void doCommand(Object vertexId, SimpleGraphContainer graphContainer, Window mainWindow, CommandManager commandManager) {
    	String groupId = graphContainer.getNextGroupId();
    	graphContainer.addGroup(groupId, GROUP_ICON);
    	graphContainer.getVertexContainer().setParent(groupId, ROOT_GROUP_ID);
    	
    	for(Object itemId : graphContainer.getSelectedVertexIds()) {
    		graphContainer.getVertexContainer().setParent(itemId, groupId);
    	}
    }
}