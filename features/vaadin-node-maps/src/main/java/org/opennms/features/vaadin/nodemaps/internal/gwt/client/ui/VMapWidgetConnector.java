package org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui;

import org.opennms.features.vaadin.nodemaps.internal.MapWidgetComponent;

import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@Connect(MapWidgetComponent.class)
public class VMapWidgetConnector extends AbstractComponentConnector {

    @Override
    public VMapWidget getWidget() {
        // TODO Auto-generated method stub
        return (VMapWidget) super.getWidget();
    }

    @Override
    public MapWidgetState getState() {
        // TODO Auto-generated method stub
        return (MapWidgetState) super.getState();
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);
    }

}
