package org.opennms.features.poller.remote.gwt.client.events;

import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;

import com.google.gwt.event.shared.GwtEvent;

public class ApplicationDetailsRetrievedEvent extends
        GwtEvent<ApplicationDetailsRetrievedEventHandler> {

    public final static Type<ApplicationDetailsRetrievedEventHandler> TYPE = new Type<ApplicationDetailsRetrievedEventHandler>();
    private ApplicationDetails m_applicationDetails;

    public ApplicationDetailsRetrievedEvent(ApplicationDetails details) {
        setApplicationDetails(details);
    }

    @Override
    protected void dispatch(ApplicationDetailsRetrievedEventHandler handler) {
        handler.onApplicationDetailsRetrieved(this);

    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<ApplicationDetailsRetrievedEventHandler> getAssociatedType() {
        return TYPE;
    }

    private void setApplicationDetails(ApplicationDetails applicationDetails) {
        m_applicationDetails = applicationDetails;
    }

    public ApplicationDetails getApplicationDetails() {
        return m_applicationDetails;
    }

}
