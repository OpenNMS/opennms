package org.opennms.features.poller.remote.gwt.client.data;

import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

public class AndFilter implements LocationFilter {

    private LocationFilter[] m_filters; 
    public AndFilter(LocationFilter... filters) {
        m_filters = filters;
    }

    public boolean matches(LocationInfo location) {
        for(LocationFilter filter : m_filters) {
            if(!filter.matches(location)) {
                return false;
            }
        }
        return true;
    }

}
