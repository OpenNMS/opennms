package org.opennms.features.poller.remote.gwt.client.events;

import org.opennms.features.poller.remote.gwt.client.GWTBounds;

import com.google.gwt.event.shared.GwtEvent;

public class MapPanelBoundsChangedEvent extends GwtEvent<MapPanelBoundsChangedEventHandler> {
    
    public static Type<MapPanelBoundsChangedEventHandler> TYPE = new Type<MapPanelBoundsChangedEventHandler>();
    private GWTBounds m_newBounds;
    
    public MapPanelBoundsChangedEvent(GWTBounds bounds) {
        setBounds(bounds);
    }
    
    @Override
    protected void dispatch(MapPanelBoundsChangedEventHandler handler) {
        handler.onBoundsChanged(this);
        
    }

    @Override
    public Type<MapPanelBoundsChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    private void setBounds(GWTBounds bounds) {
        m_newBounds = bounds;
    }

    public GWTBounds getBounds() {
        return m_newBounds;
    }

}
