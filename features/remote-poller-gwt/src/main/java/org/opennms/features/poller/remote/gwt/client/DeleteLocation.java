/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import de.novanic.eventservice.client.event.Event;

public class DeleteLocation extends BaseLocation implements Event {
	private static final long serialVersionUID = 1L;

	public DeleteLocation() {}
	public DeleteLocation(String name, String pollingPackageName, String area, String geolocation) {
		super(name, pollingPackageName, area, geolocation);
	}
	
	public String toString() {
		return "DeleteLocation["+getAttributeText()+"]";
	}
}