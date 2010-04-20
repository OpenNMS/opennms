package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class SortOrderUpdateEvent extends GwtEvent<SortOrderUpdateHandler> {
    
    public static Type<SortOrderUpdateHandler> TYPE = new Type<SortOrderUpdateHandler>();
    private String m_sortOrder;
    
    public SortOrderUpdateEvent(String sortOrder) {
        setSortOrder(sortOrder);
    }
    
    @Override
    protected void dispatch(SortOrderUpdateHandler handler) {
        handler.onSortOrderUpdated(this);
        
    }

    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<SortOrderUpdateHandler> getAssociatedType() {
        return TYPE;
    }

    public void setSortOrder(String sortOrder) {
        m_sortOrder = sortOrder;
    }

    public String getSortOrder() {
        return m_sortOrder;
    }

}
