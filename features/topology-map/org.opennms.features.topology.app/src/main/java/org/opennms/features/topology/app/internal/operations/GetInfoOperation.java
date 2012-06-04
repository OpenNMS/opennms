package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import com.vaadin.ui.Window;

public class GetInfoOperation implements Operation {

    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        Window mainWindow = operationContext.getMainWindow();
        
        mainWindow.showNotification("This has not been implemented yet");
        return null;
    }

    @Override
    public boolean display(List<Object> targets,
            OperationContext operationContext) {
        return false;
    }

    @Override
    public boolean enabled(List<Object> targets,
            OperationContext operationContext) {
        return targets == null || operationContext.getGraphContainer().getEdgeContainer().containsId(targets);
    }

    @Override
    public String getId() {
        return null;
    }
}