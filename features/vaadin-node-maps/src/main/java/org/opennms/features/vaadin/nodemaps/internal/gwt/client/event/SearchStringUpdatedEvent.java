package org.opennms.features.vaadin.nodemaps.internal.gwt.client.event;

import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.controls.search.UpdateEvent;

public class SearchStringUpdatedEvent extends UpdateEvent {
    public static final String TYPE = "searchStringUpdated";

    protected SearchStringUpdatedEvent() {}

    public static final SearchStringUpdatedEvent createEvent(final String searchString)  {
        final SearchStringUpdatedEvent event = UpdateEvent.createUpdateEvent(TYPE).cast();
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
