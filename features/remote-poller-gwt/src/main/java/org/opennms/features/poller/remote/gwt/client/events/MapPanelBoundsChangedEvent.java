package org.opennms.features.poller.remote.gwt.client.events;

import org.opennms.features.poller.remote.gwt.client.GWTBounds;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>MapPanelBoundsChangedEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class MapPanelBoundsChangedEvent extends GwtEvent<MapPanelBoundsChangedEventHandler> {
    
    /** Constant <code>TYPE</code> */
    public static Type<MapPanelBoundsChangedEventHandler> TYPE = new Type<MapPanelBoundsChangedEventHandler>();
    private GWTBounds m_newBounds;
    
    /**
     * <p>Constructor for MapPanelBoundsChangedEvent.</p>
     *
     * @param bounds a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    public MapPanelBoundsChangedEvent(GWTBounds bounds) {
        setBounds(bounds);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void dispatch(MapPanelBoundsChangedEventHandler handler) {
        handler.onBoundsChanged(this);
        
    }

    /** {@inheritDoc} */
    @Override
    public Type<MapPanelBoundsChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    private void setBounds(GWTBounds bounds) {
        m_newBounds = bounds;
    }

    /**
     * <p>getBounds</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     */
    public GWTBounds getBounds() {
        return m_newBounds;
    }

}
