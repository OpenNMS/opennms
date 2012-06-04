package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;
import org.opennms.features.topology.app.internal.jung.CircleLayoutAlgorithm;

import com.vaadin.ui.Window;

public class CircleLayoutCommand extends Command {


    public CircleLayoutCommand(String caption, String menuLocation,
			String contextMenuLocation) {
		super(caption, menuLocation, contextMenuLocation);
	}

	@Override
    public boolean appliesToTarget(Object target, SimpleGraphContainer graphContainer) {
    	return true;
    }

    @Override
    public void doCommand(Object target, SimpleGraphContainer graphContainer, Window mainWindow, CommandManager commandManager) {
    	graphContainer.setLayoutAlgorithm(new CircleLayoutAlgorithm());
    }
}