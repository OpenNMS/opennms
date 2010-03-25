package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.user.client.rpc.IsSerializable;

import de.novanic.eventservice.client.event.Event;

public class UpdateLocations implements Event, IsSerializable {
	private static final long serialVersionUID = 1L;
	private Collection<Location> m_locations;

	public UpdateLocations() {
		m_locations = new ArrayList<Location>();
	}

	public UpdateLocations(Collection<Location> locations) {
		m_locations = locations;
	}

	public Collection<Location> getLocations() {
		return m_locations;
	}
}
