package org.opennms.features.poller.remote.gwt.client.events;

import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>ApplicationDetailsRetrievedEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationDetailsRetrievedEvent extends GwtEvent<ApplicationDetailsRetrievedEventHandler> {

    /** Constant <code>TYPE</code> */
    public final static Type<ApplicationDetailsRetrievedEventHandler> TYPE = new Type<ApplicationDetailsRetrievedEventHandler>();
    private ApplicationDetails m_applicationDetails;

    /**
     * <p>Constructor for ApplicationDetailsRetrievedEvent.</p>
     *
     * @param details a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
     */
    public ApplicationDetailsRetrievedEvent(final ApplicationDetails details) {
        setApplicationDetails(details);
    }

    /** {@inheritDoc} */
    @Override
    protected void dispatch(final ApplicationDetailsRetrievedEventHandler handler) {
        handler.onApplicationDetailsRetrieved(this);

    }

    /** {@inheritDoc} */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ApplicationDetailsRetrievedEventHandler> getAssociatedType() {
        return TYPE;
    }

    private void setApplicationDetails(final ApplicationDetails applicationDetails) {
        m_applicationDetails = applicationDetails;
    }

    /**
     * <p>getApplicationDetails</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
     */
    public ApplicationDetails getApplicationDetails() {
        return m_applicationDetails;
    }

}
