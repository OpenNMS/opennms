package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>LocationManagerInitializationCompleteEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class LocationManagerInitializationCompleteEvent extends GwtEvent<LocationManagerInitializationCompleteEventHander> {
    
    /** Constant <code>TYPE</code> */
    public static Type<LocationManagerInitializationCompleteEventHander> TYPE = new Type<LocationManagerInitializationCompleteEventHander>();
    
    /** {@inheritDoc} */
    @Override
    protected void dispatch(LocationManagerInitializationCompleteEventHander handler) {
        handler.onInitializationComplete(this);
    }

    /** {@inheritDoc} */
    @Override
    public Type<LocationManagerInitializationCompleteEventHander> getAssociatedType() {
        return TYPE;
    }

}
