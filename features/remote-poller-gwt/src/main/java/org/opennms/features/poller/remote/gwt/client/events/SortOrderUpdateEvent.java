package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * <p>SortOrderUpdateEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class SortOrderUpdateEvent extends GwtEvent<SortOrderUpdateHandler> {
    
    /** Constant <code>TYPE</code> */
    public static Type<SortOrderUpdateHandler> TYPE = new Type<SortOrderUpdateHandler>();
    private String m_sortOrder;
    
    /**
     * <p>Constructor for SortOrderUpdateEvent.</p>
     *
     * @param sortOrder a {@link java.lang.String} object.
     */
    public SortOrderUpdateEvent(String sortOrder) {
        setSortOrder(sortOrder);
    }
    
    /** {@inheritDoc} */
    @Override
    protected void dispatch(SortOrderUpdateHandler handler) {
        handler.onSortOrderUpdated(this);
        
    }

    /** {@inheritDoc} */
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<SortOrderUpdateHandler> getAssociatedType() {
        return TYPE;
    }

    /**
     * <p>setSortOrder</p>
     *
     * @param sortOrder a {@link java.lang.String} object.
     */
    public void setSortOrder(String sortOrder) {
        m_sortOrder = sortOrder;
    }

    /**
     * <p>getSortOrder</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSortOrder() {
        return m_sortOrder;
    }

}
