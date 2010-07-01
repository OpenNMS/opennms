package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * <p>SortOrderUpdateHandler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface SortOrderUpdateHandler extends EventHandler {
    /**
     * <p>onSortOrderUpdated</p>
     *
     * @param e a {@link org.opennms.features.poller.remote.gwt.client.events.SortOrderUpdateEvent} object.
     */
    public void onSortOrderUpdated(SortOrderUpdateEvent e);
}
