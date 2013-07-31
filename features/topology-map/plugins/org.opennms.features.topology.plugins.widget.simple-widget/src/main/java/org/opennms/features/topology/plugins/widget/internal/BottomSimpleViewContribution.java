package org.opennms.features.topology.plugins.widget.internal;

import org.opennms.features.topology.api.IViewContribution;
import org.opennms.features.topology.api.WidgetContext;


import com.vaadin.server.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class BottomSimpleViewContribution implements IViewContribution {

    @Override
    public Component getView(WidgetContext widgetContext) {
        
        return new Label("This is a test for the bottom view");
    }

    @Override
    public String getTitle() {
        return "Test Widget";
    }

    @Override
    public Resource getIcon() {
        return null;
    }

}
