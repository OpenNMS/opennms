package org.opennms.features.topology.app.internal;

import org.opennms.features.topology.api.Operation;
import org.opennms.features.topology.api.OperationContext;

import com.vaadin.event.Action;

public interface Command {

    public abstract boolean appliesToTarget(Object target, OperationContext operationContext);

    public abstract void doCommand(Object target, OperationContext operationContext);

    public abstract void undoCommand();

    public abstract String getMenuPosition();
    
    public abstract String getContextMenuPosition();

    public abstract boolean isAction();
    
    public abstract Action getAction();
    
    public abstract Operation getOperation();

}