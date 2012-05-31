package org.opennms.features.topology.api;

import java.util.List;

public interface Operation {
    
    public final static String OPERATION_MENU_LOCATION = "operation.menuLocation";
    public final static String OPERATION_CONTEXT_LOCATION = "operation.contextMenuLocation";
    
    public interface Undoer {
        public void undo();
    }
    
    public Undoer execute(List<Object> targets);
    public boolean display(List<Object> targets);
    public boolean enabled(List<Object> targets);
    public String getLabel();
    public String getId();
}
