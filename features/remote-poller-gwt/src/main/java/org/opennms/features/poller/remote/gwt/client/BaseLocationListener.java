/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import de.novanic.eventservice.client.event.Event;

public class BaseLocationListener implements LocationListener {
	public void apply(final Event event) {
		if (event instanceof UpdateLocation) {
			onLocationUpdate((UpdateLocation)event);
		} else if (event instanceof DeleteLocation) {
			onLocationDelete((DeleteLocation)event);
		} else if (event instanceof UpdateComplete) {
			onUpdateComplete((UpdateComplete)event);
		} else {
			onEvent((Event)event);
		}
	}

	/**
	 * Called when a location update comes from the backend.
	 * @param event the event
	 */
	public void onLocationUpdate(final UpdateLocation event) {}

	/**
	 * Called when a location delete comes from the backend.
	 * @param event the event
	 */
	public void onLocationDelete(final DeleteLocation event) {}

	/**
	 * Called when an update is completed.
	 * @param event the event
	 */
	public void onUpdateComplete(final UpdateComplete event) {}

	/**
	 * Called when any other unhandled event is sent.
	 * @param event the event
	 */
	public void onEvent(final Event event) {}
}