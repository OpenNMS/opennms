package org.opennms.features.poller.remote.gwt.client.events;

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;

import com.google.gwt.event.shared.GwtEvent;

public class ApplicationSelectedEvent extends GwtEvent<ApplicationSelectedEventHandler> {
    
    public static Type<ApplicationSelectedEventHandler> TYPE = new Type<ApplicationSelectedEventHandler>();
    private ApplicationInfo m_appInfo;
    
    public ApplicationSelectedEvent(ApplicationInfo appInfo) {
        setAppInfo(appInfo);
    }
    
    @Override
    protected void dispatch(ApplicationSelectedEventHandler handler) {
        handler.onApplicationSelected(this);
    }

    @Override
    public Type<ApplicationSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void setAppInfo(ApplicationInfo appInfo) {
        m_appInfo = appInfo;
    }

    public ApplicationInfo getAppInfo() {
        return m_appInfo;
    }

}
