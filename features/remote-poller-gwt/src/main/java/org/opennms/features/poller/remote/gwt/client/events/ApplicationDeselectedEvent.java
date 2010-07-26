package org.opennms.features.poller.remote.gwt.client.events;

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>ApplicationDeselectedEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class ApplicationDeselectedEvent extends GwtEvent<ApplicationDeselectedEventHandler> {
    
    /** Constant <code>TYPE</code> */
    public static Type<ApplicationDeselectedEventHandler> TYPE = new Type<ApplicationDeselectedEventHandler>();
    private ApplicationInfo m_appInfo;
    
    /**
     * <p>Constructor for ApplicationDeselectedEvent.</p>
     *
     * @param appInfo a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    public ApplicationDeselectedEvent(ApplicationInfo appInfo) {
        setAppInfo(appInfo);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void dispatch(ApplicationDeselectedEventHandler handler) {
        handler.onApplicationDeselected(this);
    }

    /** {@inheritDoc} */
    @Override
    public Type<ApplicationDeselectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    /**
     * <p>setAppInfo</p>
     *
     * @param appInfo a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    protected void setAppInfo(ApplicationInfo appInfo) {
        m_appInfo = appInfo;
    }

    /**
     * <p>getAppInfo</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    public ApplicationInfo getAppInfo() {
        return m_appInfo;
    }

}
