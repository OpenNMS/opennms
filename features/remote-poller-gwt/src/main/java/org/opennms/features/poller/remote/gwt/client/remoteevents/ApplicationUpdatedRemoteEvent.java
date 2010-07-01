package org.opennms.features.poller.remote.gwt.client.remoteevents;

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;

/**
 * <p>ApplicationUpdatedRemoteEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationUpdatedRemoteEvent implements MapRemoteEvent {
	private static final long serialVersionUID = 1L;
	private ApplicationInfo m_applicationInfo;

	/**
	 * <p>Constructor for ApplicationUpdatedRemoteEvent.</p>
	 */
	public ApplicationUpdatedRemoteEvent() {}

	/**
	 * <p>Constructor for ApplicationUpdatedRemoteEvent.</p>
	 *
	 * @param item a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
	 */
	public ApplicationUpdatedRemoteEvent(final ApplicationInfo item) {
		m_applicationInfo = item;
	}

	/** {@inheritDoc} */
	public void dispatch(final RemotePollerPresenter presenter) {
		presenter.updateApplication(m_applicationInfo);
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
	    return "ApplicationUpdatedRemoteEvent[applicationInfo=" + m_applicationInfo + "]";
	}
}
