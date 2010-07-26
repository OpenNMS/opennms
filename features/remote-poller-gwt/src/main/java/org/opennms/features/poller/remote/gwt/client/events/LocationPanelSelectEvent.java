package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>LocationPanelSelectEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationPanelSelectEvent extends GwtEvent<LocationPanelSelectEventHandler> {
    
    /** Constant <code>TYPE</code> */
    public static Type<LocationPanelSelectEventHandler> TYPE = new Type<LocationPanelSelectEventHandler>();
    private String m_locationName;
    
    /**
     * <p>Constructor for LocationPanelSelectEvent.</p>
     *
     * @param locationName a {@link java.lang.String} object.
     */
    public LocationPanelSelectEvent(String locationName) {
        m_locationName = locationName;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void dispatch(LocationPanelSelectEventHandler handler) {
        handler.onLocationSelected(this);
        
    }

    /** {@inheritDoc} */
    @Override
    public Type<LocationPanelSelectEventHandler> getAssociatedType() {
        return TYPE;
    }


    /**
     * <p>getLocationName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLocationName() {
        return m_locationName;
    }
}
