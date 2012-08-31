package org.opennms.features.topology.plugins.widget.internal;

import org.opennms.features.topology.api.IViewContribution;

import com.vaadin.terminal.Resource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

public class SimpleViewContribution implements IViewContribution {

    @Override
    public Component getView() {
        Label label = new Label("This is a simple widget component");
        label.setHeight("50px");
        return label;
    }

    @Override
    public String getTitle() {
        return "Simple View";
    }

    @Override
    public Resource getIcon() {
        return null;
    }

}
