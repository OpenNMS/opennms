package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.UpdateEvent;

public class SearchStringSetEvent extends UpdateEvent {
    public static final String TYPE = "searchStringSet";

    protected SearchStringSetEvent() {}

    public static final SearchStringSetEvent createEvent(final String searchString)  {
        final SearchStringSetEvent event = UpdateEvent.createUpdateEvent(TYPE).cast();
        event.setSearchString(searchString);
        return event;
    }

    public native final String getSearchString() /*-{
        return this.searchString;
    }-*/;

    protected native final void setSearchString(final String searchString) /*-{
        this.searchString = searchString;
    }-*/;
}
