/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public interface LocationListener extends RemoteEventListener
{
	void onEvent(Event event);
}