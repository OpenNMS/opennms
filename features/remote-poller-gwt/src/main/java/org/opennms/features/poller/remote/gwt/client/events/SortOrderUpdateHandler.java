package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

public interface SortOrderUpdateHandler extends EventHandler {
    public void onSortOrderUpdated(SortOrderUpdateEvent e);
}
