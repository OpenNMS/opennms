package org.opennms.ipv6.summary.gui.client;

import com.google.gwt.event.shared.GwtEvent;

public class HostUpdateEvent extends GwtEvent<HostUpdateEventHandler> {

    public static Type<HostUpdateEventHandler> TYPE = new Type<HostUpdateEventHandler>();
    
    private final String m_host;
    
    public HostUpdateEvent(String host) {
        m_host = host;
    }
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<HostUpdateEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HostUpdateEventHandler handler) {
        handler.onHostUpdate(this);
    }
    
    public String getHost() {
        return m_host;
    }

}
