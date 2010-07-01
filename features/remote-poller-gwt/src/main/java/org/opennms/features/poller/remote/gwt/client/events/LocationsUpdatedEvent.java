package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>LocationsUpdatedEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationsUpdatedEvent extends GwtEvent<LocationsUpdatedEventHandler> {
    
    /** Constant <code>TYPE</code> */
    public static Type<LocationsUpdatedEventHandler> TYPE = new Type<LocationsUpdatedEventHandler>();

    private String m_eventString = "You have got the event String";
    
    /**
     * <p>Constructor for LocationsUpdatedEvent.</p>
     */
    public LocationsUpdatedEvent() {
    }
    
	/** {@inheritDoc} */
	@Override
	protected void dispatch(LocationsUpdatedEventHandler handler) {
		handler.onLocationsUpdated(this);
	}

	/** {@inheritDoc} */
	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<LocationsUpdatedEventHandler> getAssociatedType() {
		return TYPE;
	}

    /**
     * <p>setEventString</p>
     *
     * @param eventString a {@link java.lang.String} object.
     */
    public void setEventString(final String eventString) {
        m_eventString = eventString;
    }

    /**
     * <p>getEventString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getEventString() {
        return m_eventString;
    }
}
