package org.opennms.features.poller.remote.gwt.client.data;

import java.util.HashSet;
import java.util.Set;

import org.opennms.features.poller.remote.gwt.client.Status;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

public class StatusFilter implements LocationFilter {
    
    private final Set<Status> m_selectedStatuses = new HashSet<Status>();

    public Set<Status> getSelectedStatuses() {
        return m_selectedStatuses;
    }

    public boolean matches(final LocationInfo location) {
        return getSelectedStatuses().contains(location.getStatus());
    }

    public boolean addStatus(Status status) {
        return getSelectedStatuses().add(status);
    }

    public boolean removeStatus(Status status) {
        return getSelectedStatuses().remove(status);
    }
}
