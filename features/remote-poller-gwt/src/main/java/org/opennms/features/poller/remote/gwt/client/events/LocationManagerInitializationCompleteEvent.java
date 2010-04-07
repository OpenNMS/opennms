package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class LocationManagerInitializationCompleteEvent extends GwtEvent<LocationManagerInitializationCompleteEventHander> {
    
    public static Type<LocationManagerInitializationCompleteEventHander> TYPE = new Type<LocationManagerInitializationCompleteEventHander>();
    
    @Override
    protected void dispatch(LocationManagerInitializationCompleteEventHander handler) {
        handler.onInitializationComplete(this);
    }

    @Override
    public Type<LocationManagerInitializationCompleteEventHander> getAssociatedType() {
        return TYPE;
    }

}
