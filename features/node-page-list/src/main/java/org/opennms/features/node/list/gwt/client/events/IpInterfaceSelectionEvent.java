package org.opennms.features.node.list.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class IpInterfaceSelectionEvent extends GwtEvent<IpInterfaceSelectionHandler> {

    public static final Type<IpInterfaceSelectionHandler> TYPE = new Type<IpInterfaceSelectionHandler>();
    private String m_ipInterfaceId;

    public IpInterfaceSelectionEvent(String id) {
        setIpInterfaceId(id);
    }

    @Override
    public Type<IpInterfaceSelectionHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(IpInterfaceSelectionHandler handler) {
        handler.onIpInterfaceSelection(this);
    }

    public void setIpInterfaceId(String ipInterfaceId) {
        m_ipInterfaceId = ipInterfaceId;
    }

    public String getIpInterfaceId() {
        return m_ipInterfaceId;
    }

}
