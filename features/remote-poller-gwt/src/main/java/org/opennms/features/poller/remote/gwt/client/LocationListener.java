
/**
 * <p>LocationListener interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.features.poller.remote.gwt.client;

import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;
public interface LocationListener extends RemoteEventListener
{
	/**
	 * <p>onEvent</p>
	 *
	 * @param event a {@link de.novanic.eventservice.client.event.Event} object.
	 */
	void onEvent(Event event);
}
