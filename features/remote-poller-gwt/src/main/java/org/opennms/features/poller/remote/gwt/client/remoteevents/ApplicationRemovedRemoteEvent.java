package org.opennms.features.poller.remote.gwt.client.remoteevents;

import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;

public class ApplicationRemovedRemoteEvent implements MapRemoteEvent {
    private static final long serialVersionUID = 1L;
    private String m_applicationName;

    public ApplicationRemovedRemoteEvent() {}

    public ApplicationRemovedRemoteEvent(final String applicationName) {
        m_applicationName = applicationName;
    }

    public void dispatch(final RemotePollerPresenter locationManager) {
        locationManager.removeApplication(m_applicationName);
    }

    public String toString() {
        return "ApplicationRemovedRemoteEvent[applicationName=" + m_applicationName + "]";
    }
}
