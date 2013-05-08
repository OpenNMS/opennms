package org.opennms.features.topology.api;

import com.vaadin.ui.Window;

public interface WidgetContext {
    public Window getMainWindow();
    public GraphContainer getGraphContainer();
}
