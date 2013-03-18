package org.opennms.features.topology.app.internal.gwt.client;

import org.opennms.features.topology.app.internal.TopologyComponent;

import com.vaadin.shared.ui.Connect;

@Connect(TopologyComponent.class)
public class TopologyComponentConnector extends LegacyConnector{
    
    @Override
    public VTopologyComponent getWidget() {
        return (VTopologyComponent) super.getWidget();
    }
    
}
