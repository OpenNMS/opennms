package org.opennms.features.topology.api;

import java.util.List;

public interface Operation {
    
    public final static String OPERATION_MENU_LOCATION = "operation.menuLocation";
    public final static String OPERATION_CONTEXT_LOCATION = "operation.contextMenuLocation";
    public final static String OPERATION_LABEL = "operation.label";
    
    public interface Undoer {
        public void undo(OperationContext operationContext);
    }
    
    public Undoer execute(List<Object> targets, OperationContext operationContext);
    public boolean display(List<Object> targets, OperationContext operationContext);
    public boolean enabled(List<Object> targets, OperationContext operationContext);
    public String getId();
}
