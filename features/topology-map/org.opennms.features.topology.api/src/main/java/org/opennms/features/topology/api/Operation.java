package org.opennms.features.topology.api;

import java.util.List;

public interface Operation {
    
    public interface Undoer {
        public void undo();
    }
    
    public Undoer execute(List<Object> targets);
    public boolean display(List<Object> targets);
    public boolean enabled(List<Object> targets);
    public String getLabel();
    public String getId();
}
