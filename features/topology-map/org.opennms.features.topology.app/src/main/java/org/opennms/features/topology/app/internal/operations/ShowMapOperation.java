package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;

import com.vaadin.ui.Window;

public class ShowMapOperation implements Operation {
    
	public void doCommand(Object unused, OperationContext operationContext) {
        Window mainWindow = operationContext.getMainWindow();
        
	    mainWindow.showNotification("This has not been implemented yet");
        
    }

    @Override
    public Undoer execute(List<Object> targets, OperationContext operationContext) {
        doCommand(targets, operationContext);
        return null;
    }

    @Override
    public boolean display(List<Object> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public boolean enabled(List<Object> targets, OperationContext operationContext) {
        return true;
    }

    @Override
    public String getId() {
        return null;
    }
}