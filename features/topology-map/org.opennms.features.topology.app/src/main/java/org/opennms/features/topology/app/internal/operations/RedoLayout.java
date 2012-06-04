package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;

import com.vaadin.ui.Window;

class RedoLayout extends Command {

	
    public RedoLayout(String caption, String menuLocation,
			String contextMenuLocation) {
		super(caption, menuLocation, contextMenuLocation);
		// TODO Auto-generated constructor stub
	}

	@Override
    public void doCommand(Object target, SimpleGraphContainer graphContainer, Window mainWindow, CommandManager commandManager) {
    	graphContainer.redoLayout();
    }

    @Override
    public boolean appliesToTarget(Object target, SimpleGraphContainer graphContainer) {
        //Applies to background as a whole
        return target == null;
    }
}