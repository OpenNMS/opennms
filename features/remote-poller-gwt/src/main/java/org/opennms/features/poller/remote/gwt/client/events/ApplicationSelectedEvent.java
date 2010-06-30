package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>ApplicationSelectedEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationSelectedEvent extends GwtEvent<ApplicationSelectedEventHandler> {
    
    /** Constant <code>TYPE</code> */
    public static Type<ApplicationSelectedEventHandler> TYPE = new Type<ApplicationSelectedEventHandler>();
    private String m_applicationName;
    
    /**
     * <p>Constructor for ApplicationSelectedEvent.</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     */
    public ApplicationSelectedEvent(final String applicationName) {
        m_applicationName = applicationName;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void dispatch(ApplicationSelectedEventHandler handler) {
        handler.onApplicationSelected(this);
    }

    /** {@inheritDoc} */
    @Override
    public Type<ApplicationSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    /**
     * <p>getApplicationname</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getApplicationname() {
        return m_applicationName;
    }

}
