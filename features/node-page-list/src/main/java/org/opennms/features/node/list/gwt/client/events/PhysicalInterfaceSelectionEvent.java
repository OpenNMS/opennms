package org.opennms.features.node.list.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class PhysicalInterfaceSelectionEvent extends GwtEvent<PhysicalInterfaceSelectionHandler> {

    
    public static Type<PhysicalInterfaceSelectionHandler> TYPE = new Type<PhysicalInterfaceSelectionHandler>();
    private String m_ifIndex;
    
    public PhysicalInterfaceSelectionEvent(String ifIndex) {
        setIfIndex(ifIndex);
    }
    
    @Override
    public Type<PhysicalInterfaceSelectionHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PhysicalInterfaceSelectionHandler handler) {
        handler.onPhysicalInterfaceSelected(this);
    }

    public String getIfIndex() {
        return m_ifIndex;
    }

    public void setIfIndex(String ifIndex) {
        m_ifIndex = ifIndex;
    }

}
