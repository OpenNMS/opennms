package org.opennms.features.poller.remote.gwt.client.remoteevents;

import java.io.Serializable;

import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;

import com.google.gwt.user.client.rpc.IsSerializable;

import de.novanic.eventservice.client.event.Event;

public interface MapRemoteEvent extends Event, Serializable, IsSerializable {
	public void dispatch(final RemotePollerPresenter locationManager);
}
