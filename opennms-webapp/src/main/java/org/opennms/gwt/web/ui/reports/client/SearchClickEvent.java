package org.opennms.gwt.web.ui.reports.client;

import com.google.gwt.event.shared.GwtEvent;

public class SearchClickEvent extends GwtEvent<SearchClickEventHandler> {
    
    public static Type<SearchClickEventHandler> TYPE = new Type<SearchClickEventHandler>();
    private String m_searchTerm;

    public SearchClickEvent(String searchTerm) {
        m_searchTerm = searchTerm;
    }
    
    public static Type<SearchClickEventHandler> getType(){
        return TYPE;
    }
    
    @Override
    public Type<SearchClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SearchClickEventHandler handler) {
        handler.onSearchClickEvent(m_searchTerm);
    }

}
