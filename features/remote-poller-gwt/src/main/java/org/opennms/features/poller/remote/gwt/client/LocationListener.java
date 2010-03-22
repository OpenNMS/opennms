/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.listener.RemoteEventListener;

public interface LocationListener extends RemoteEventListener
{
	void onLocationUpdate(UpdateLocation event);
	void onLocationDelete(DeleteLocation event);
	void onUpdateComplete(UpdateComplete event);
	void onEvent(Event event);
}