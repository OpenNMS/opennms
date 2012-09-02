package org.opennms.features.topology.app.internal.operations;

import java.util.List;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.app.internal.Command;
import org.opennms.features.topology.app.internal.CommandManager;

import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

public class HistoryOperation implements Operation {
    
    private CommandManager m_commandManager;

    public HistoryOperation(CommandManager commandManager) {
        m_commandManager = commandManager;
    }
    
    @Override
    public Undoer execute(List<Object> targets,
            OperationContext operationContext) {
        Window mainWindow = operationContext.getMainWindow();
        CommandManager commandManager = m_commandManager;
        
        Window window = new Window();
        window.setModal(true);
        
        for(Command command : commandManager.getHistoryList()) {
            window.addComponent(new Label(command.toString()));
        }
        
        mainWindow.addWindow(window);
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