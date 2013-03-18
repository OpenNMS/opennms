package org.opennms.features.topology.api;

import com.vaadin.ui.LegacyWindow;

public interface WidgetContext {
    public LegacyWindow getMainWindow();
    public GraphContainer getGraphContainer();
}
