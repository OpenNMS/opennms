/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;


import de.novanic.eventservice.client.event.Event;

public class UpdateLocation extends BaseLocation implements Event {
	private static final long serialVersionUID = 1L;
	public UpdateLocation() {
		super();
	}
	public UpdateLocation(final String name, final String pollingPackageName, final String area, final String geolocation) {
		super(name, pollingPackageName, area, geolocation);
	}
	public UpdateLocation(final String name, final String pollingPackageName, final String area, final String geolocation, final GWTLatLng latLng, final LocationMonitorState lms) {
		super(name, pollingPackageName, area, geolocation, latLng, lms);
	}
	
	public String toString() {
		return "UpdateLocation["+getAttributeText()+"]";
	}
}