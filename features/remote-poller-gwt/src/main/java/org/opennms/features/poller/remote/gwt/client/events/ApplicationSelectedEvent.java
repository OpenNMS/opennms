package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class ApplicationSelectedEvent extends GwtEvent<ApplicationSelectedEventHandler> {
    
    public static Type<ApplicationSelectedEventHandler> TYPE = new Type<ApplicationSelectedEventHandler>();
    private String m_applicationName;
    
    public ApplicationSelectedEvent(final String applicationName) {
        m_applicationName = applicationName;
    }
    
    @Override
    protected void dispatch(ApplicationSelectedEventHandler handler) {
        handler.onApplicationSelected(this);
    }

    @Override
    public Type<ApplicationSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    public String getApplicationname() {
        return m_applicationName;
    }

}
