package org.opennms.features.topology.app.internal.operations;

import org.opennms.features.topology.app.internal.SimpleGraphContainer;

import com.vaadin.ui.Window;

public interface OperationContext {

    public Window getMainWindow();
    public SimpleGraphContainer getGraphContainer();
}
