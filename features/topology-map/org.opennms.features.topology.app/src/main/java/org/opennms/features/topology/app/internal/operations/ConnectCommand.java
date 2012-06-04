package org.opennms.features.topology.app.internal.operations;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;

import com.vaadin.ui.Window;

public class ConnectCommand extends Command {

    public ConnectCommand(String caption, String menuLocation,
			String contextMenuLocation) {
		super(caption, menuLocation, contextMenuLocation);
	}

	@Override
    public boolean appliesToTarget(Object itemId, SimpleGraphContainer graphContainer) {
    	return graphContainer.getSelectedVertexIds().size() == 2;
    }

    @Override
    public void doCommand(Object unused, SimpleGraphContainer graphContainer, Window mainWindow, CommandManager commandManager) {
    	List<Object> endPoints = new ArrayList<Object>(graphContainer.getSelectedVertexIds());
    	
    	graphContainer.connectVertices(graphContainer.getNextEdgeId(), (String)endPoints.get(0), (String)endPoints.get(1));
    }
}