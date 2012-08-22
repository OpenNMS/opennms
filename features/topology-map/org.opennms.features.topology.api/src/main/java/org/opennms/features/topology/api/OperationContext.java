package org.opennms.features.topology.api;


import com.vaadin.ui.Window;

public interface OperationContext {

    public Window getMainWindow();
    public GraphContainer getGraphContainer();
    public boolean isChecked();
}
