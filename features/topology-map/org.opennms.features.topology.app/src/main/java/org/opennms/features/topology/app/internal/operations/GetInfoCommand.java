package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;

import com.vaadin.ui.Window;

public class GetInfoCommand extends Command {
    

    public  GetInfoCommand(String caption, String menuLocation, String contextMenuLocation) {
		super(caption, menuLocation, contextMenuLocation);
	}

	@Override
    public boolean appliesToTarget(Object itemId, SimpleGraphContainer graphContainer) {
        return itemId == null || graphContainer.getEdgeContainer().containsId(itemId);
    }

    @Override
    public void doCommand(Object target, SimpleGraphContainer graphContainer, Window mainWindow, CommandManager commandManager) {
        mainWindow.showNotification("This has not been implemented yet");
    }
}