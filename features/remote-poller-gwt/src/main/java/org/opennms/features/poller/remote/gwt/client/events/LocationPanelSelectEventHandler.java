package org.opennms.features.poller.remote.gwt.client.events;

import com.google.gwt.event.shared.EventHandler;

/**
 * <p>LocationPanelSelectEventHandler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface LocationPanelSelectEventHandler extends EventHandler {
   /**
    * <p>onLocationSelected</p>
    *
    * @param event a {@link org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent} object.
    */
   public void onLocationSelected(LocationPanelSelectEvent event);

}
