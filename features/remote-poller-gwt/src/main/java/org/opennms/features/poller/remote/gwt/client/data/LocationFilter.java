package org.opennms.features.poller.remote.gwt.client.data;

import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;


public interface LocationFilter {
    
    public abstract boolean matches(LocationInfo location);
    
}
