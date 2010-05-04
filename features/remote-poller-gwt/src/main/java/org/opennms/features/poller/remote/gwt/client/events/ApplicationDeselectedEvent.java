package org.opennms.features.poller.remote.gwt.client.events;

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;

import com.google.gwt.event.shared.GwtEvent;

public class ApplicationDeselectedEvent extends GwtEvent<ApplicationDeselectedEventHandler> {
    
    public static Type<ApplicationDeselectedEventHandler> TYPE = new Type<ApplicationDeselectedEventHandler>();
    private ApplicationInfo m_appInfo;
    
    public ApplicationDeselectedEvent(ApplicationInfo appInfo) {
        setAppInfo(appInfo);
    }
    
    @Override
    protected void dispatch(ApplicationDeselectedEventHandler handler) {
        handler.onApplicationDeselected(this);
    }

    @Override
    public Type<ApplicationDeselectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void setAppInfo(ApplicationInfo appInfo) {
        m_appInfo = appInfo;
    }

    public ApplicationInfo getAppInfo() {
        return m_appInfo;
    }

}
