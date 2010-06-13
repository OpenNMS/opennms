package org.opennms.features.poller.remote.gwt.client.remoteevents;

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;

public class ApplicationUpdatedRemoteEvent implements MapRemoteEvent {
	private static final long serialVersionUID = 1L;
	private ApplicationInfo m_applicationInfo;

	public ApplicationUpdatedRemoteEvent() {}

	public ApplicationUpdatedRemoteEvent(final ApplicationInfo item) {
		m_applicationInfo = item;
	}

	public void dispatch(final RemotePollerPresenter presenter) {
		presenter.updateApplication(m_applicationInfo);
	}
	
	public String toString() {
	    return "ApplicationUpdatedRemoteEvent[applicationInfo=" + m_applicationInfo + "]";
	}
}
