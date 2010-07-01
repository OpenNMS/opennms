package org.opennms.features.poller.remote.gwt.client.events;


import org.opennms.features.poller.remote.gwt.client.GWTMarkerState;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>GWTMarkerClickedEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GWTMarkerClickedEvent extends GwtEvent<GWTMarkerClickedEventHandler> {
    
    /** Constant <code>TYPE</code> */
    public final static Type<GWTMarkerClickedEventHandler> TYPE = new Type<GWTMarkerClickedEventHandler>();
    
    private GWTMarkerState m_marker;
    
    /**
     * <p>Constructor for GWTMarkerClickedEvent.</p>
     *
     * @param marker a {@link org.opennms.features.poller.remote.gwt.client.GWTMarkerState} object.
     */
    public GWTMarkerClickedEvent(GWTMarkerState marker) {
        setMarker(marker);
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(GWTMarkerClickedEventHandler handler) {
        handler.onGWTMarkerClicked(this);
        
    }

    /** {@inheritDoc} */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<GWTMarkerClickedEventHandler> getAssociatedType() {
        return TYPE;
    }

    private void setMarker(GWTMarkerState marker) {
        m_marker = marker;
    }

    /**
     * <p>getMarkerState</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTMarkerState} object.
     */
    public GWTMarkerState getMarkerState() {
        return m_marker;
    }

}
