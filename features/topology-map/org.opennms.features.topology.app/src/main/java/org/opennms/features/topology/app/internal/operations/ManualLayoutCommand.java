package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;
import org.opennms.features.topology.app.internal.ManualLayoutAlgorithm;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;

import com.vaadin.ui.Window;

public class ManualLayoutCommand extends Command {


    public ManualLayoutCommand(String caption, String menuLocation,
			String contextMenuLocation) {
		super(caption, menuLocation, contextMenuLocation);
	}

	@Override
    public boolean appliesToTarget(Object target, SimpleGraphContainer graphContainer) {
    	return true;
    }

    @Override
    public void doCommand(Object target, SimpleGraphContainer graphContainer, Window mainWindow, CommandManager commandManager) {
    	graphContainer.setLayoutAlgorithm(new ManualLayoutAlgorithm());
    }
}