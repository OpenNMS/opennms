package org.opennms.features.poller.remote.gwt.client.remoteevents;


/**
 * <p>ApplicationRemovedRemoteEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationRemovedRemoteEvent implements MapRemoteEvent {
    private static final long serialVersionUID = 1L;
    private String m_applicationName;

    /**
     * <p>Constructor for ApplicationRemovedRemoteEvent.</p>
     */
    public ApplicationRemovedRemoteEvent() {}

    /**
     * <p>Constructor for ApplicationRemovedRemoteEvent.</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     */
    public ApplicationRemovedRemoteEvent(final String applicationName) {
        m_applicationName = applicationName;
    }

    /** {@inheritDoc} */
    public void dispatch(final MapRemoteEventHandler locationManager) {
        locationManager.removeApplication(m_applicationName);
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return "ApplicationRemovedRemoteEvent[applicationName=" + m_applicationName + "]";
    }
}
