package org.opennms.features.poller.remote.gwt.client;

import de.novanic.eventservice.client.event.Event;
import de.novanic.eventservice.client.event.domain.Domain;
import de.novanic.eventservice.client.event.domain.DomainFactory;

public interface Location extends Event {

	public static final Domain LOCATION_EVENT_DOMAIN = DomainFactory.getDomain("location_event");

	public abstract String getName();

	public abstract String getPollingPackageName();

	public abstract String getArea();

	public abstract String getGeolocation();

	public abstract LocationMonitorState getLocationMonitorState();

	public abstract String getImageURL();

}